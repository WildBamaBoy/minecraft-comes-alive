package mca.client.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import mca.ai.AISleep;
import mca.client.gui.GuiInteraction;
import mca.client.gui.GuiVillagerEditor;
import mca.client.model.ModelHuman;
import mca.core.Constants;
import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.util.UVPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import radixcore.client.render.RenderHelper;
import radixcore.util.RadixMath;

/**
 * Determines how a Human is rendered.
 */
public class RenderHuman<T extends EntityHuman> extends RenderBiped<T>
{
	private static final ResourceLocation gui = new ResourceLocation("mca:textures/gui.png");
	private static final UVPoint exMark = new UVPoint(55, 18, 3, 13);
	private static final UVPoint minus = new UVPoint(69, 23, 7, 3);
	private static final UVPoint plus = new UVPoint(85, 21, 7, 7);
	private static final float LABEL_SCALE = 0.027F;

	public RenderHuman()
	{
		super(Minecraft.getMinecraft().getRenderManager(), new ModelHuman(), 0.5F);

		//Build the render layers.
		this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
	}

	@Override
	protected void preRenderCallback(EntityHuman entityLivingBase, float partialTickTime)
	{
		final EntityHuman entity = (EntityHuman) entityLivingBase;
		final AISleep sleepAI = entity.getAI(AISleep.class);
		float scale = entity.getIsMale() ? Constants.SCALE_M_ADULT : Constants.SCALE_F_ADULT;
		
		if (entity.getIsChild())
		{
			final boolean doGradualGrowth = MCA.getConfig().isAgingEnabled;
			final float growthFactor = (entity.getIsMale() ? 0.39F : 0.37F) / MCA.getConfig().childGrowUpTime * entity.getAge();

			scale = 0.55F + growthFactor;

			if (entityLivingBase.getRidingEntity() != null)
			{
				if (entityLivingBase.getRidingEntity() instanceof EntityHorse)
				{
					GL11.glTranslated(0.0D, growthFactor - 0.3D, 0.2D);
				}

				else
				{
					GL11.glTranslated(0.0D, growthFactor + 0.1D, 0.4D);
				}
			}
		}

		GL11.glScalef(scale, scale + entity.getHeight(), scale);
		GL11.glScalef(scale + entity.getGirth(), scale, scale + entity.getGirth());

		if (entityLivingBase.getRidingEntity() != null)
		{
			if (entityLivingBase.getRidingEntity() instanceof EntityHorse)
			{
				GL11.glTranslated(0.0D, 0.55D, 0.1D);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void doRenderEntity(EntityHuman entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		final PlayerMemory memory = entity.getPlayerMemoryWithoutCreating(player);

		if (!entity.getDoDisplay())
		{
			return;
		}

		//Pass special renders according to player memory here.
		if (RadixMath.getDistanceToEntity(entity, player) <= 5.0F && !entity.getAI(AISleep.class).getIsSleeping() && !entity.isInteractionGuiOpen && memory != null)
		{
			UVPoint uvp = memory.doDisplayFeedback() ? (memory.getLastInteractionSuccess() ? plus : minus) : memory.getHasQuest() ? exMark : null;

			if (uvp != null)
			{
				GL11.glPushMatrix();
				{
					GL11.glTranslatef((float) x, (float) y + entity.height + 0.25F + 0.5F, (float) z);
					GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
					GL11.glScalef(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glTranslatef(0.0F, 0.25F / LABEL_SCALE, 0.0F);
					RenderHelper.drawTexturedRectangle(gui, (int) x, (int) y + 12, uvp.getU(), uvp.getV(), uvp.getWidth(), uvp.getHeight());
				}
				GL11.glPopMatrix();

				GL11.glDepthMask(true);
				GL11.glEnable(GL11.GL_LIGHTING);
			}
		}

		double posYCorrection = y - entity.getYOffset();

		if (entity.isSneaking())
		{
			posYCorrection -= 0.125D;
		}

		super.doRender((T) entity, x, posYCorrection, z, entityYaw, partialTicks);
	}

	/**
	 * Renders labels, effects, etc. around the entity.
	 */
	private void doRenderEffects(EntityHuman entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		final int currentHealth = (int) entity.getHealth();
		final int maxHealth = (int) entity.getMaxHealth();
		final double distanceFromPlayer = RadixMath.getDistanceToEntity(entity, Minecraft.getMinecraft().thePlayer);

		//Ignore special effects in the villager editor.
		if (Minecraft.getMinecraft().currentScreen instanceof GuiVillagerEditor)
		{
			return;
		}

		//Render health first, if they're damaged.
		else if (currentHealth < maxHealth)
		{
			renderLabel(entity, x, y, z, MCA.getLanguageManager().getString("label.health") + currentHealth + "/" + maxHealth);
		}

		//Render their name assuming that they're not damaged.
		else if (canRenderNameTag(entity) && MCA.getConfig().showNameTagOnHover)
		{
			renderLabel(entity, x, y, z, entity.getTitle(Minecraft.getMinecraft().thePlayer));
		}

		//When in the interaction GUI, render the person's name and hearts value.
		else if (entity.isInteractionGuiOpen)
		{
			renderLabel(entity, x, y + (distanceFromPlayer / 15.0D)  + (entity.getHeight() * 1.15D), z, entity.getTitle(Minecraft.getMinecraft().thePlayer));
			renderHearts(entity, x, y + (distanceFromPlayer / 15.0D) + (entity.getHeight() * 1.15D), z, entity.getPlayerMemory(Minecraft.getMinecraft().thePlayer).getHearts());
		}

		//When performing a chore, render the name of the chore above their head.
		else if (entity.getAIManager().isToggleAIActive())
		{
			renderLabel(entity, x, y + (distanceFromPlayer / 15.0D)  + (entity.getHeight() * 1.15D), z, entity.getAIManager().getNameOfActiveAI());
		}
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

	private void renderLabel(EntityHuman human, double posX, double posY, double posZ, String labelText)
	{
		renderLivingLabel((T) human, labelText, posX, posY, posZ, 64);
	}

	private boolean canRenderNameTag(EntityLivingBase entityRendering)
	{
		final EntityPlayer entityPlayer = Minecraft.getMinecraft().thePlayer;

		final Vec3d entityLookVector = new Vec3d(entityRendering.posX - entityPlayer.posX, entityRendering.getEntityBoundingBox().minY - 3 + (double) entityRendering.height / 2.0F - entityPlayer.posY + entityPlayer.getEyeHeight(), entityRendering.posZ - entityPlayer.posZ).normalize(); //entityRendering.getLook(1.0F).normalize();
		final double dotProduct = entityPlayer.getLook(1.0F).normalize().dotProduct(entityLookVector);
		final boolean isPlayerLookingAt = dotProduct > 1.0D - 0.025D / entityLookVector.lengthVector() ? entityPlayer.canEntityBeSeen(entityRendering) : false;
		final double distance = entityRendering.getDistanceToEntity(Minecraft.getMinecraft().thePlayer);

		return !(Minecraft.getMinecraft().currentScreen instanceof GuiInteraction) && distance < 5.0D && isPlayerLookingAt && Minecraft.isGuiEnabled() && entityRendering != Minecraft.getMinecraft().thePlayer && !entityRendering.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) && !entityRendering.isBeingRidden();
	}

	@Override
	public void doRender(EntityHuman entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		doRenderEntity(entity, x, y, z, entityYaw, partialTicks);
		doRenderEffects(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity)
	{
		final EntityHuman human = (EntityHuman)entity;
		final String skinName = human.getHeadTexture();

		if (skinName.isEmpty())
		{
			return new ResourceLocation("minecraft:textures/entity/steve.png");
		}

		else if (human.getPlayerSkinResourceLocation() != null)
		{
			return human.getPlayerSkinResourceLocation();
		}

		else
		{
			return new ResourceLocation(((EntityHuman)entity).getHeadTexture());
		}
	}
}
