/*******************************************************************************
 * RenderHuman.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.render;

import mca.chore.ChoreHunting;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.WorldPropertiesList;
import mca.core.util.LanguageHelper;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import mca.entity.EntityVillagerAdult;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;

import org.lwjgl.opengl.GL11;

/**
 * Determines how a Human is rendered.
 */
public class RenderHuman extends RenderBiped
{
	private static final float LABEL_SCALE = 0.027F;
	private final ModelBiped modelArmorChestplate;
	private final ModelBiped modelArmor;

	/**
	 * Constructor
	 */
	public RenderHuman()
	{
		super(new ModelBiped(0.0F), 0.5F);

		modelBipedMain       = (ModelBiped)mainModel;
		modelArmorChestplate = new ModelBiped(1.0F);
		modelArmor           = new ModelBiped(0.5F);
	}

	@Override
	public void doRender(Entity entity, double posX, double posY, double posZ, float rotationYaw, float rotationPitch)
	{
		renderHuman((AbstractEntity)entity, posX, posY, posZ, rotationYaw, rotationPitch);
	}

	@Override
	public void doRenderLiving(EntityLiving entityLiving, double posX, double posY, double posZ, float rotationYaw, float rotationPitch)
	{
		renderHuman((AbstractEntity)entityLiving, posX, posY, posZ, rotationYaw, rotationPitch);
	}

	@Override
	protected int shouldRenderPass(EntityLivingBase entityLivingBase, int armorId, float partialTickTime)
	{
		return setArmorModel((AbstractEntity)entityLivingBase, armorId);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entityLivingBase, float partialTickTime)
	{
		final AbstractEntity entity = (AbstractEntity)entityLivingBase;
		final float scale = entity.isMale ? Constants.SCALE_MALE_ADULT: Constants.SCALE_FEMALE_ADULT;

		if (entity.isAffectedByHeight)
		{
			GL11.glScalef(scale, scale + entity.villagerHeightFactor, scale);
		}

		else
		{
			GL11.glScalef(scale, scale, scale);
		}
	}

	@Override
	protected void renderEquippedItems(EntityLivingBase entityLivingBase, float partialTickTime)
	{
		final AbstractEntity entity = (AbstractEntity)entityLivingBase;

		if (!(entity.getInstanceOfCurrentChore() instanceof ChoreHunting))
		{
			super.renderEquippedItems(entityLivingBase, partialTickTime);

			final ItemStack bootStack = entity.inventory.armorItemInSlot(3);
			final ItemStack heldItem = entity.getHeldItem();

			if (bootStack != null && bootStack.getItem().itemID < 256)
			{
				//TODO Investigate.
				GL11.glPushMatrix();
				modelBipedMain.bipedHead.postRender(0.0625F);

				if (RenderBlocks.renderItemIn3d(Block.blocksList[bootStack.itemID].getRenderType()))
				{
					GL11.glTranslatef(0.0F, -0.25F, 0.0F);
					GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
					GL11.glScalef(0.625F, -0.625F, 0.625F);
				}

				renderManager.itemRenderer.renderItem(entity, bootStack, 0);
				GL11.glPopMatrix();
			}

			if (heldItem != null)
			{
				GL11.glPushMatrix();
				modelBipedMain.bipedRightArm.postRender(0.0625F);
				GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);

				if (heldItem.itemID < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[heldItem.itemID].getRenderType()))
				{
					GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
					GL11.glRotatef(20F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
					GL11.glScalef(0.375F, -0.375F, 0.375F);
				}

				else if (heldItem.itemID == Item.bow.itemID)
				{
					GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
					GL11.glRotatef(-20F, 0.0F, 1.0F, 0.0F);
					GL11.glScalef(0.625F, -0.625F, 0.625F);
					GL11.glRotatef(-100F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
				}

				else if (Item.itemsList[heldItem.itemID].isFull3D())
				{
					if (Item.itemsList[heldItem.itemID].shouldRotateAroundWhenRendering())
					{
						GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
						GL11.glTranslatef(0.0F, -0.125F, 0.0F);
					}

					GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
					GL11.glScalef(0.625F, -0.625F, 0.625F);
					GL11.glRotatef(-100F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
				}

				else
				{
					GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
					GL11.glScalef(0.375F, 0.375F, 0.375F);
					GL11.glRotatef(60F, 0.0F, 0.0F, 1.0F);
					GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(20F, 0.0F, 0.0F, 1.0F);
				}

				renderManager.itemRenderer.renderItem(entity, heldItem, 0);
				GL11.glPopMatrix();
			}
		}
	}

	@Override
	protected void passSpecialRender(EntityLivingBase entityLivingBase, double posX, double posY, double posZ)
	{
		final AbstractEntity entity = (AbstractEntity)entityLivingBase;

		if (Minecraft.isGuiEnabled() && !(entity.getInstanceOfCurrentChore() instanceof ChoreHunting))
		{
			if (Minecraft.getMinecraft().isSingleplayer())
			{
				renderLabelsIntegrated(entity, posX, posY, posZ);
			}

			else
			{
				renderLabelsDedicated(entity, posX, posY, posZ);
			}
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) 
	{
		final AbstractEntity abstractEntity = (AbstractEntity)entity;

		if (abstractEntity.texture.contains("steve"))
		{
			return new ResourceLocation("minecraft:" + abstractEntity.texture);
		}

		else
		{
			return new ResourceLocation("mca:" + abstractEntity.texture);
		}
	}

	/**
	 * Sets the model of the armor that the Entity is wearing.
	 * 
	 * @param	entity				The entity whose armor model is being set.
	 * @param	armorId				The ID of the armor piece being set.
	 * @param	partialTickTime		The amount of time since the last tick.
	 * 
	 * @return	-1 if setting the armor model wasn't successful. 1 if the armor is not enchanted. 15 if the armor is enchanted.
	 */
	private int setArmorModel(AbstractEntity entity, int armorId)
	{
		final ItemStack itemStack = entity.inventory.armorItemInSlot(armorId);

		if (itemStack != null)
		{
			final Item item = itemStack.getItem();

			if (item instanceof ItemArmor)
			{
				final ItemArmor itemArmor = (ItemArmor)item;
				final ModelBiped armorModel = armorId == 2 ? modelArmor : modelArmorChestplate;

				bindTexture(getArmorResource(entity, itemStack, armorId, null));

				armorModel.bipedHead.showModel = armorId == 0;
				armorModel.bipedHeadwear.showModel = armorId == 0;
				armorModel.bipedBody.showModel = armorId == 1 || armorId == 2;
				armorModel.bipedRightArm.showModel = armorId == 1;
				armorModel.bipedLeftArm.showModel = armorId == 1;
				armorModel.bipedRightLeg.showModel = armorId == 2 || armorId == 3;
				armorModel.bipedLeftLeg.showModel = armorId == 2 || armorId == 3;

				setRenderPassModel(armorModel);

				if (armorModel != null)
				{
					armorModel.onGround = mainModel.onGround;
					armorModel.isRiding = mainModel.isRiding;
				}

				if (itemArmor.getArmorMaterial() == EnumArmorMaterial.CLOTH)
				{
					final int armorColor = itemArmor.getColor(itemStack);
					final float colorRed = (armorColor >> 16 & 255) / 255.0F;
					final float colorGreen = (armorColor >> 8 & 255) / 255.0F;
					final float colorBlue = (armorColor & 255) / 255.0F;

					GL11.glColor3f(colorRed, colorGreen, colorBlue);

					if (itemStack.isItemEnchanted())
					{
						return 31;
					}

					else
					{
						return 16;
					}
				}

				GL11.glColor3f(1.0F, 1.0F, 1.0F);
				return itemStack.isItemEnchanted() ? 15 : 1;
			}
		}

		return -1;
	}

	/**
	 * Renders the human on the screen.
	 * 
	 * @param 	entity			The entity being rendered.
	 * @param 	posX			The entity's X position.
	 * @param 	posY			The entity's Y position.
	 * @param 	posZ			The entity's Z position.
	 * @param 	rotationYaw		The entity's rotation yaw.
	 * @param 	rotationPitch	The entity's rotation pitch.
	 */
	private void renderHuman(AbstractEntity entity, double posX, double posY, double posZ, float rotationYaw, float rotationPitch)
	{
		if (entity.hasBeenExecuted)
		{
			modelBipedMain.bipedHead.isHidden = true;
			modelBipedMain.bipedHeadwear.isHidden = true;
		}

		else
		{
			modelBipedMain.bipedHead.isHidden = false;
			modelBipedMain.bipedHeadwear.isHidden = false;
		}

		if (entity.getInstanceOfCurrentChore() instanceof ChoreHunting)
		{
			shadowOpaque = 0F;
		}

		else
		{
			double posYCorrection = posY - entity.yOffset;

			shadowOpaque = 1.0F;

			//			GL11.glColor3f(1.0F, 1.0F, 1.0F);

			final ItemStack heldItem = entity.getHeldItem();
			modelArmorChestplate.heldItemRight = modelArmor.heldItemRight = modelBipedMain.heldItemRight = heldItem == null ? 0 : 1;
			modelArmorChestplate.isSneak = modelArmor.isSneak = modelBipedMain.isSneak = entity.isSneaking();

			if (heldItem != null)
			{
				final EnumAction useAction = heldItem.getItemUseAction();

				if (useAction == EnumAction.bow)
				{
					modelArmorChestplate.aimedBow = modelArmor.aimedBow = modelBipedMain.aimedBow = true;
				}
			}

			if (entity.isSneaking())
			{
				posYCorrection -= 0.125D;
			}

			super.doRenderLiving((EntityLiving)entity, posX, posYCorrection, posZ, rotationYaw, rotationPitch);
			modelArmorChestplate.aimedBow = modelArmor.aimedBow = modelBipedMain.aimedBow = false;
			modelArmorChestplate.isSneak = modelArmor.isSneak = modelBipedMain.isSneak = false;
			modelArmorChestplate.heldItemRight = modelArmor.heldItemRight = modelBipedMain.heldItemRight = 0;
		}
	}

	/**
	 * Determines the appropriate label to render over an entity's head on the integrated server, if any.
	 * 
	 * @param 	entity	The entity that the labels will be rendered on.
	 * @param 	posX	The entity's x position.
	 * @param 	posY	The entity's y position.
	 * @param 	posZ	The entity's z position.
	 */

	private void renderLabelsIntegrated(AbstractEntity entity, double posX, double posY, double posZ)
	{
		final WorldPropertiesList propertiesList = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.username).worldProperties;
		final AbstractEntity clientEntity = (AbstractEntity)DimensionManager.getWorld(entity.worldObj.provider.dimensionId).getEntityByID(entity.entityId);

		if (clientEntity.getHealth() < entity.getMaxHealth())
		{
			renderLabel(entity, posX, posY, posZ, LanguageHelper.getString("gui.overhead.health") + Math.round(clientEntity.getHealth()) + "/" + entity.getMaxHealth());
			return;
		}

		else if (clientEntity.hasArrangerRing)
		{
			renderLabel(entity, posX, posY, posZ, LanguageHelper.getString("gui.overhead.hasring"));
		}

		else if (clientEntity.isSleeping && clientEntity.canEntityBeSeen(Minecraft.getMinecraft().thePlayer) && !propertiesList.hideSleepingTag)
		{
			renderLabel(entity, posX, posY, posZ, LanguageHelper.getString("gui.overhead.sleeping"));
		}

		else if (entity instanceof EntityVillagerAdult)
		{
			final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			final EntityVillagerAdult villager = (EntityVillagerAdult)entity;

			if (villager.playerMemoryMap.containsKey(player.username))
			{
				final PlayerMemory memory = villager.playerMemoryMap.get(player.username);

				if (memory.hasGift)
				{
					renderLabel(entity, posX, posY, posZ, LanguageHelper.getString("gui.overhead.hasgift"));
				}
			}
		}
	}

	/**
	 * Determines the appropriate label to render over an entity's head on the dedicated server, if any.
	 * 
	 * @param 	entity	The entity that the labels will be rendered on.
	 * @param 	posX	The entity's x position.
	 * @param 	posY	The entity's y position.
	 * @param 	posZ	The entity's z position.
	 */

	private void renderLabelsDedicated(AbstractEntity entity, double posX, double posY, double posZ)
	{
		if (entity.hasArrangerRing)
		{
			renderLabel(entity, posX, posY, posZ, LanguageHelper.getString("gui.overhead.hasring"));
		}

		else if (entity.isSleeping)
		{
			if (entity.canEntityBeSeen(Minecraft.getMinecraft().thePlayer))
			{
				renderLabel(entity, posX, posY, posZ, LanguageHelper.getString("gui.overhead.sleeping"));
			}
		}

		else if (entity instanceof EntityVillagerAdult)
		{					
			final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			final EntityVillagerAdult villager = (EntityVillagerAdult)entity;

			if (villager.playerMemoryMap.containsKey(player.username))
			{
				final PlayerMemory memory = villager.playerMemoryMap.get(player.username);

				if (memory.hasGift)
				{
					renderLabel(entity, posX, posY, posZ, LanguageHelper.getString("gui.overhead.hasgift"));
				}
			}
		}
	}

	/**
	 * Renders a label above an entity's head.
	 * 
	 * @param 	abstractEntity	The entity that the label should be rendered on.
	 * @param 	posX			The entity's x position.
	 * @param 	posY			The entity's y position.
	 * @param 	posZ			The entity's z position.
	 * @param 	labelText		The text that should appear on the label.
	 */
	private void renderLabel(AbstractEntity abstractEntity, double posX, double posY, double posZ, String labelText)
	{
		if (abstractEntity.isSneaking())
		{
			final Tessellator  tessellator = Tessellator.instance;
			final FontRenderer fontRenderer = getFontRendererFromRenderManager();
			final int stringWidth = fontRenderer.getStringWidth(labelText) / 2;
			
			GL11.glPushMatrix();

			GL11.glTranslatef((float)posX + 0.0F, (float)posY + 2.3F, (float)posZ);
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
			fontRenderer.drawString(labelText, -fontRenderer.getStringWidth(labelText) / 2, 0, 0x20ffffff);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			GL11.glPopMatrix();
		}

		else
		{
			renderLivingLabel(abstractEntity, labelText, posX, posY, posZ, 64);
		}
	}
}
