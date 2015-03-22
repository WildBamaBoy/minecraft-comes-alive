/*******************************************************************************
 * RenderHuman.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.client.render;

import java.util.ArrayList;
import java.util.List;

import mca.ai.AIConverse;
import mca.ai.AISleep;
import mca.client.gui.GuiInteraction;
import mca.client.gui.GuiVillagerEditor;
import mca.client.model.ModelHuman;
import mca.core.Constants;
import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

import radixcore.client.render.RenderHelper;
import radixcore.util.RadixMath;

/**
 * Determines how a Human is rendered.
 */
public class RenderHuman extends RenderBiped
{
	private static final ResourceLocation gui = new ResourceLocation("mca:textures/gui.png");
	private static final float LABEL_SCALE = 0.027F;
	private final ModelBiped modelArmorPlate;
	private final ModelBiped modelArmor;

	public RenderHuman()
	{
		super(new ModelHuman(0.0F), 0.5F);

		modelBipedMain = (ModelBiped) mainModel;
		modelArmorPlate = new ModelBiped(1.0F);
		modelArmor = new ModelBiped(0.5F);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entityLivingBase, float partialTickTime)
	{
		final EntityHuman entity = (EntityHuman) entityLivingBase;
		final AISleep sleepAI = entity.getAI(AISleep.class);
		float scale = entity.getIsMale() ? Constants.SCALE_M_ADULT : Constants.SCALE_F_ADULT;

		if (entity.getIsChild())
		{
			final boolean doGradualGrowth = MCA.getConfig().isAgingEnabled;
			final float growthFactor = (entity.getIsMale() ? 0.39F : 0.37F) / MCA.getConfig().childGrowUpTime * entity.getAge();

			scale = 0.55F + growthFactor;

			if (entityLivingBase.ridingEntity != null)
			{
				GL11.glTranslated(0.0D, (1.0D + growthFactor) + growthFactor, 0.2D);
			}
		}

		GL11.glScalef(scale, scale + entity.getHeight(), scale);
		GL11.glScalef(scale + entity.getGirth(), scale, scale + entity.getGirth());

		if (sleepAI.getIsInBed())
		{
			renderHumanSleeping(entity, partialTickTime);
		}

		else if (entityLivingBase.ridingEntity != null)
		{
			GL11.glTranslated(0.0D, 0.55D, 0.1D);
		}
	}

	@Override
	protected void passSpecialRender(EntityLivingBase entityLivingBase, double posX, double posY, double posZ)
	{
		super.passSpecialRender(entityLivingBase, posX, posY, posZ);

		final EntityHuman human = (EntityHuman)entityLivingBase;
		final AIConverse converseAI = human.getAI(AIConverse.class);
		final int currentHealth = (int) human.getHealth();
		final int maxHealth = (int) human.getMaxHealth();
		final double distanceFromPlayer = RadixMath.getDistanceToEntity(human, Minecraft.getMinecraft().thePlayer);

		if (Minecraft.getMinecraft().currentScreen instanceof GuiVillagerEditor)
		{
			return;
		}
			
		else if (currentHealth < maxHealth)
		{
			renderLabel(human, posX, posY, posZ, MCA.getLanguageManager().getString("label.health") + currentHealth + "/" + maxHealth);
		}
		
		else if (canRenderNameTag(entityLivingBase) && MCA.getConfig().showNameTagOnHover)
		{
			renderLabel(human, posX, posY, posZ, human.getTitle(Minecraft.getMinecraft().thePlayer));
		}
		
		else if (human.displayNameForPlayer)
		{
			renderLabel(human, posX, posY + (distanceFromPlayer / 15.0D)  + (human.getHeight() * 1.15D), posZ, human.getTitle(Minecraft.getMinecraft().thePlayer));
			renderHearts(human, posX, posY + (distanceFromPlayer / 15.0D) + (human.getHeight() * 1.15D), posZ, human.getPlayerMemory(Minecraft.getMinecraft().thePlayer).getHearts());
		}

		else if (converseAI.getConversationActive() && distanceFromPlayer <= 6.0D && MCA.getConfig().showVillagerConversations)
		{
			String conversationString = "conversation" + converseAI.getConversationID() + ".progress" + converseAI.getConversationProgress();
			boolean elevateLabel = converseAI.getConversationProgress() % 2 == 0;
			posY = elevateLabel ? posY + 0.25D : posY;

			if (converseAI.getConversationProgress() != 0)
			{
				renderLabel(human, posX, posY, posZ, MCA.getLanguageManager().getString(conversationString));
			}
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		final String skinName = ((EntityHuman)entity).getSkin();

		if (skinName.isEmpty())
		{
			return new ResourceLocation("minecraft:textures/entity/steve.png");
		}

		else
		{
			return new ResourceLocation(((EntityHuman)entity).getSkin());
		}
	}

	private void renderHuman(EntityHuman entity, double posX, double posY, double posZ, float rotationYaw, float rotationPitch)
	{
		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		final PlayerMemory memory = entity.getPlayerMemory(player);
		
		if (!entity.getDoDisplay())
		{
			return;
		}

		if (RadixMath.getDistanceToEntity(entity, player) <= 5.0F
				&& !canRenderNameTag(entity) && !entity.getAI(AISleep.class).getIsSleeping()
				&& !entity.displayNameForPlayer && memory.getHasQuest())
		{
			GL11.glPushMatrix();
			{
				GL11.glTranslatef((float) posX, (float) posY + entity.height + 0.25F + 0.5F, (float) posZ);
				GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
				GL11.glScalef(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glTranslatef(0.0F, 0.25F / LABEL_SCALE, 0.0F);
				RenderHelper.drawTexturedRectangle(gui, (int) posX, (int) posY - 8, 55, 18, 3, 13);
			}
			GL11.glPopMatrix();

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
		
		double posYCorrection = posY - entity.yOffset;
		shadowOpaque = 1.0F;

		final ItemStack heldItem = entity.getHeldItem();
		modelArmorPlate.heldItemRight = modelArmor.heldItemRight = modelBipedMain.heldItemRight = heldItem == null ? 0 : 1;
		modelArmorPlate.isSneak = modelArmor.isSneak = modelBipedMain.isSneak = entity.isSneaking();

		if (heldItem != null)
		{
			final EnumAction useAction = heldItem.getItemUseAction();

			if (useAction == EnumAction.bow)
			{
				modelArmorPlate.aimedBow = modelArmor.aimedBow = modelBipedMain.aimedBow = true;
			}
		}

		if (entity.isSneaking())
		{
			posYCorrection -= 0.125D;
		}

		super.doRender(entity, posX, posYCorrection, posZ, rotationYaw, rotationPitch);
		modelArmorPlate.aimedBow = modelArmor.aimedBow = modelBipedMain.aimedBow = false;
		modelArmorPlate.isSneak = modelArmor.isSneak = modelBipedMain.isSneak = false;
		modelArmorPlate.heldItemRight = modelArmor.heldItemRight = modelBipedMain.heldItemRight = 0;
	}

	protected void renderHumanSleeping(EntityHuman entity, double partialTickTime)
	{
		final AISleep sleepAI = entity.getAI(AISleep.class);
		final int meta = sleepAI.getBedMeta();

		if (meta == 8)
		{
			entity.rotationYawHead = 180.0F;
			GL11.glTranslated(-0.5D, 0.0D, -1.0D);
			GL11.glRotated(90, -1, 0, 0);
			GL11.glTranslated(0.0D, 0.0D, -0.75D);
		}

		else if (meta == 11)
		{
			entity.rotationYawHead = 90.0F;
			GL11.glTranslated(0.5D, 0.0D, -1.0D);
			GL11.glRotated(90, -1, 0, 0);
			GL11.glTranslated(0.0D, 0.0D, -0.75D);
		}

		else if (meta == 10)
		{
			entity.rotationYawHead = 0.0F;
			GL11.glTranslated(0.5D, 0.0D, -2.0D);
			GL11.glRotated(90, -1, 0, 0);
			GL11.glTranslated(0.0D, 0.0D, -0.75D);
		}

		else if (meta == 9)
		{
			entity.rotationYawHead = -90.0F;
			GL11.glTranslated(-0.5D, 0.0D, -2.0D);
			GL11.glRotated(90, -1, 0, 0);
			GL11.glTranslated(0.0D, 0.0D, -0.75D);
		}
	}

	@Override
	public void doRender(Entity entity, double posX, double posY, double posZ, float rotationYaw, float rotationPitch)
	{
		renderHuman((EntityHuman) entity, posX, posY, posZ, rotationYaw, rotationPitch);
	}

	@Override
	public void doRender(EntityLiving entityLiving, double posX, double posY, double posZ, float rotationYaw, float rotationPitch)
	{
		renderHuman((EntityHuman) entityLiving, posX, posY, posZ, rotationYaw, rotationPitch);
	}

	private void renderHearts(EntityHuman human, double posX, double posY, double posZ, int heartsLevel)
	{
		try
		{
			//Clamp to 10 first to calculate gold hearts.
			int clampedHearts = RadixMath.clamp(((Math.abs(heartsLevel) + 5) / 10), 0, 10);
			final boolean isNegative = heartsLevel < 0;
			int goldHearts = isNegative ? 0 : clampedHearts - 5;

			final int heartU = 5;
			final int goldHeartU = 37;
			final int negHeartU = 21;

			//Clamp down to 5 hearts since processing has completed, needed to generate the list properly.
			clampedHearts = RadixMath.clamp(clampedHearts, 0, 5);
			final List<Integer> heartsToDraw = new ArrayList<Integer>();

			//Add the needed gold hearts.
			while (goldHearts > 0)
			{
				heartsToDraw.add(goldHeartU);
				goldHearts--;
			}

			//Add the remaining hearts.
			while (heartsToDraw.size() < clampedHearts)
			{
				if (isNegative)
				{
					heartsToDraw.add(negHeartU);
				}

				else
				{
					heartsToDraw.add(heartU);
				}
			}

			//Draw hearts.
			if (!heartsToDraw.isEmpty())
			{	
				GL11.glPushMatrix();
				{
					GL11.glTranslatef((float) posX + 0.0F, (float) posY + human.height + 0.25F, (float) posZ);
					GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
					GL11.glScalef(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glTranslatef(0.0F, 0.25F / LABEL_SCALE, 0.0F);

					switch (heartsToDraw.size())
					{
					case 1: 
						RenderHelper.drawTexturedRectangle(gui, ((int)posX + (10 * 2) - 22), (int)posY - 4, heartsToDraw.get(0), 20, 9, 9); break;
					case 2: 
						for (int i = 0; i < 2; i++){RenderHelper.drawTexturedRectangle(gui, ((int)posX + (10 * i) - 9), (int)posY - 4, heartsToDraw.get(i), 20, 9, 9); } break;
					case 3: 
						for (int i = 0; i < 3; i++){RenderHelper.drawTexturedRectangle(gui, ((int)posX + (10 * i) - 14), (int)posY - 4, heartsToDraw.get(i), 20, 9, 9); } break;
					case 4: 
						for (int i = 0; i < 4; i++){RenderHelper.drawTexturedRectangle(gui, ((int)posX + (10 * i) - 19), (int)posY - 4, heartsToDraw.get(i), 20, 9, 9); } break;
					case 5: 
						for (int i = 0; i < 5; i++){RenderHelper.drawTexturedRectangle(gui, ((int)posX + (10 * i) - 23), (int)posY - 4, heartsToDraw.get(i), 20, 9, 9); } break;
					}
				}
				GL11.glPopMatrix();

				GL11.glDepthMask(true);
				GL11.glEnable(GL11.GL_LIGHTING);
			}
		}

		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Renders a label above an entity's head.
	 * 
	 * @param human The entity that the label should be rendered on.
	 * @param labelText The text that should appear on the label.
	 */
	private void renderLabel(EntityHuman human, double posX, double posY, double posZ, String labelText)
	{
		if (human.isSneaking())
		{
			final Tessellator tessellator = Tessellator.instance;
			final FontRenderer fontRendererObj = getFontRendererFromRenderManager();
			final int stringWidth = fontRendererObj.getStringWidth(labelText) / 2;

			GL11.glPushMatrix();
			{
				GL11.glTranslatef((float) posX + 0.0F, (float) posY + 2.3F, (float) posZ);
				GL11.glNormal3f(0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
				GL11.glScalef(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glTranslatef(0.0F, 0.25F / LABEL_SCALE, 0.0F);
				GL11.glDepthMask(false);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

				GL11.glDisable(GL11.GL_TEXTURE_2D);

				tessellator.startDrawingQuads();
				tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
				tessellator.addVertex(-stringWidth - 1, -1D, 0.0D);
				tessellator.addVertex(-stringWidth - 1, 8D, 0.0D);
				tessellator.addVertex(stringWidth + 1, 8D, 0.0D);
				tessellator.addVertex(stringWidth + 1, -1D, 0.0D);
				tessellator.draw();

				GL11.glEnable(GL11.GL_TEXTURE_2D);

				GL11.glDepthMask(true);
				fontRendererObj.drawString(labelText, -fontRendererObj.getStringWidth(labelText) / 2, 0, 0x20ffffff);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			}
			GL11.glPopMatrix();
		}

		else
		{
			//RenderLivingLabel
			func_147906_a(human, labelText, posX, posY, posZ, 64);
		}
	}

	protected boolean canRenderNameTag(EntityLivingBase entityRendering)
	{
		final EntityPlayer entityPlayer = Minecraft.getMinecraft().thePlayer;
		final Vec3 entityLookVector = Vec3.createVectorHelper(entityRendering.posX - entityPlayer.posX, entityRendering.boundingBox.minY + (double) entityRendering.height / 2.0F - entityPlayer.posY + entityPlayer.getEyeHeight(), entityRendering.posZ - entityPlayer.posZ).normalize();
		final double dotProduct = entityPlayer.getLook(1.0F).normalize().dotProduct(entityLookVector);
		final boolean isPlayerLookingAt = dotProduct > 1.0D - 0.025D / entityLookVector.lengthVector() ? entityPlayer.canEntityBeSeen(entityRendering) : false;
		final double distance = entityRendering.getDistanceToEntity(renderManager.livingPlayer);

		return !(Minecraft.getMinecraft().currentScreen instanceof GuiInteraction) && distance < 5.0D && isPlayerLookingAt && Minecraft.isGuiEnabled() && entityRendering != renderManager.livingPlayer && !entityRendering.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) && entityRendering.riddenByEntity == null;
	}
}
