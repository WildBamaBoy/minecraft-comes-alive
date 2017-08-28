package mca.client.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import mca.actions.ActionSleep;
import mca.client.gui.GuiInteraction;
import mca.client.gui.GuiVillagerEditor;
import mca.client.model.ModelHuman;
import mca.core.Constants;
import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.util.UVPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import radixcore.modules.RadixMath;
import radixcore.modules.client.RadixRender;

/**
 * Determines how a Human is rendered.
 */
public class RenderVillagerMCA<T extends EntityVillagerMCA> extends RenderBiped<EntityVillagerMCA>
{
	private static final ResourceLocation gui = new ResourceLocation("mca:textures/gui.png");
	private static final UVPoint exMark = new UVPoint(55, 18, 3, 13);
	private static final UVPoint minus = new UVPoint(69, 23, 7, 3);
	private static final UVPoint plus = new UVPoint(85, 21, 7, 7);
	private static final float LABEL_SCALE = 0.027F;

	public RenderVillagerMCA(RenderManager manager)
	{
		super(manager, new ModelHuman(), 0.5F);

		//Build the render layers.
		this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
	}

	@Override
	protected void preRenderCallback(EntityVillagerMCA entityLivingBase, float partialTickTime)
	{
		final EntityVillagerMCA entity = (EntityVillagerMCA) entityLivingBase;
		final ActionSleep sleepAI = entity.getBehavior(ActionSleep.class);
		float scale = entity.attributes.getGender() == EnumGender.MALE ? Constants.SCALE_M_ADULT : Constants.SCALE_F_ADULT;
		
		if (entity.attributes.getIsChild())
		{
			final boolean doGradualGrowth = MCA.getConfig().isAgingEnabled;
			final float growthFactor = (entity.attributes.getGender() == EnumGender.MALE ? 0.39F : 0.37F) / MCA.getConfig().childGrowUpTime * entity.attributes.getAge();

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

		GL11.glScalef(scale, scale + entity.attributes.getScaleHeight(), scale);
		GL11.glScalef(scale + entity.attributes.getScaleWidth(), scale, scale + entity.attributes.getScaleWidth());

		if (sleepAI.getIsInBed())
		{
			renderHumanSleeping(entity, partialTickTime);
		}
		
		if (entityLivingBase.getRidingEntity() != null)
		{
			if (entityLivingBase.getRidingEntity() instanceof EntityHorse)
			{
				GL11.glTranslated(0.0D, 0.55D, 0.1D);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void doRenderEntity(EntityVillagerMCA entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		final EntityPlayer player = Minecraft.getMinecraft().player;
		final PlayerMemory memory = entity.attributes.getPlayerMemoryWithoutCreating(player);

		//Pass special renders according to player memory here.
		if (RadixMath.getDistanceToEntity(entity, player) <= 5.0F && !entity.getBehavior(ActionSleep.class).getIsSleeping() && !entity.isInteractionGuiOpen && memory != null)
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
					RadixRender.drawTexturedRectangle(gui, (int) x, (int) y + 12, uvp.getU(), uvp.getV(), uvp.getWidth(), uvp.getHeight());
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

		super.doRender((EntityVillagerMCA) entity, x, posYCorrection, z, entityYaw, partialTicks);
	}

	/**
	 * Renders labels, effects, etc. around the entity.
	 */
	private void doRenderEffects(EntityVillagerMCA entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		final int currentHealth = (int) entity.getHealth();
		final int maxHealth = (int) entity.getMaxHealth();
		final double distanceFromPlayer = RadixMath.getDistanceToEntity(entity, Minecraft.getMinecraft().player);

		//Ignore special effects in the villager editor.
		if (Minecraft.getMinecraft().currentScreen instanceof GuiVillagerEditor)
		{
			return;
		}

		//Render health first, if they're damaged.
		else if (currentHealth < maxHealth && distanceFromPlayer <= 8.0D)
		{
			renderLabel(entity, x, y + 0.1, z, MCA.getLocalizer().getString("label.health"));
			renderHealth(entity, x, y, z, currentHealth, maxHealth);
		}

		//Render their name assuming that they're not damaged.
		else if (canRenderNameTag(entity) && MCA.getConfig().showNameTagOnHover)
		{
			renderLabel(entity, x, y, z, entity.attributes.getTitle(Minecraft.getMinecraft().player));
		}

		//When in the interaction GUI, render the person's name and hearts value.
		else if (entity.isInteractionGuiOpen)
		{
			renderLabel(entity , x, y + 0.1, z, entity.attributes.getTitle(Minecraft.getMinecraft().player));
			renderHearts(entity, x, y + 0.1, z, entity.attributes.getPlayerMemory(Minecraft.getMinecraft().player).getHearts());
		}

		//When performing a chore, render the name of the chore above their head.
		else if (entity.getBehaviors().isToggleActionActive())
		{
			renderLabel(entity, x, y + 0.1, z, entity.getBehaviors().getActiveActionName());
		}
	}

	private void renderHealth(EntityVillagerMCA human, double posX, double posY, double posZ, int currentHealth, int maxHealth)
	{
		final int redHeartU = 5;
		final int darkHeartU = 21;
		int heartsDrawn = 0;
		maxHealth = Math.round((float)maxHealth / 2.0F);
		currentHealth = Math.round((float)currentHealth / 2.0F);
		int depletedHealth = maxHealth - currentHealth;
		int mid = maxHealth == 10 ? 45 : 90;
		
		for (int i = 0; i < currentHealth; i++)
		{
			heartsDrawn++;
			
			GL11.glPushMatrix();
			{
				GL11.glTranslatef((float) posX + 0.0F, (float) posY + human.height + 0.25F, (float) posZ);
				GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
				GL11.glScalef(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glTranslatef(0.0F, 0.25F / LABEL_SCALE, 0.0F);

				RadixRender.drawTexturedRectangle(gui, (int)posX + (heartsDrawn * 8) - mid, (int)posY - 4, redHeartU, 20, 9, 9);
			}
			GL11.glPopMatrix();

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
		
		for (int i = 0; i < depletedHealth; i++)
		{
			heartsDrawn++;
			
			GL11.glPushMatrix();
			{
				GL11.glTranslatef((float) posX + 0.0F, (float) posY + human.height + 0.25F, (float) posZ);
				GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
				GL11.glScalef(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glTranslatef(0.0F, 0.25F / LABEL_SCALE, 0.0F);

				RadixRender.drawTexturedRectangle(gui, (int)posX + (heartsDrawn * 8) - mid, (int)posY - 4, darkHeartU, 20, 9, 9);
			}
			GL11.glPopMatrix();

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}
	
	private void renderHearts(EntityVillagerMCA human, double posX, double posY, double posZ, int heartsLevel)
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
						RadixRender.drawTexturedRectangle(gui, ((int)posX + (10 * 2) - 22), (int)posY - 4, heartsToDraw.get(0), 20, 9, 9); break;
					case 2: 
						for (int i = 0; i < 2; i++){RadixRender.drawTexturedRectangle(gui, ((int)posX + (10 * i) - 9), (int)posY - 4, heartsToDraw.get(i), 20, 9, 9); } break;
					case 3: 
						for (int i = 0; i < 3; i++){RadixRender.drawTexturedRectangle(gui, ((int)posX + (10 * i) - 14), (int)posY - 4, heartsToDraw.get(i), 20, 9, 9); } break;
					case 4: 
						for (int i = 0; i < 4; i++){RadixRender.drawTexturedRectangle(gui, ((int)posX + (10 * i) - 19), (int)posY - 4, heartsToDraw.get(i), 20, 9, 9); } break;
					case 5: 
						for (int i = 0; i < 5; i++){RadixRender.drawTexturedRectangle(gui, ((int)posX + (10 * i) - 23), (int)posY - 4, heartsToDraw.get(i), 20, 9, 9); } break;
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

	private void renderLabel(EntityVillagerMCA human, double posX, double posY, double posZ, String labelText)
	{
		renderLivingLabel(human, labelText, posX, posY, posZ, 64);
	}

	private boolean canRenderNameTag(EntityLivingBase entityRendering)
	{
		final EntityPlayer entityPlayer = Minecraft.getMinecraft().player;

		final Vec3d entityLookVector = new Vec3d(entityRendering.posX - entityPlayer.posX, entityRendering.getEntityBoundingBox().minY - 3 + (double) entityRendering.height / 2.0F - entityPlayer.posY + entityPlayer.getEyeHeight(), entityRendering.posZ - entityPlayer.posZ).normalize(); //entityRendering.getLook(1.0F).normalize();
		final double dotProduct = entityPlayer.getLook(1.0F).normalize().dotProduct(entityLookVector);
		final boolean isPlayerLookingAt = dotProduct > 1.0D - 0.025D / entityLookVector.lengthVector() ? entityPlayer.canEntityBeSeen(entityRendering) : false;
		final double distance = entityRendering.getDistanceToEntity(Minecraft.getMinecraft().player);

		return !(Minecraft.getMinecraft().currentScreen instanceof GuiInteraction) && distance < 5.0D && isPlayerLookingAt && Minecraft.isGuiEnabled() && entityRendering != Minecraft.getMinecraft().player && !entityRendering.isInvisibleToPlayer(Minecraft.getMinecraft().player) && !entityRendering.isBeingRidden();
	}

	protected void renderHumanSleeping(EntityVillagerMCA entity, double partialTickTime)
	{
		final ActionSleep sleepAI = entity.getBehavior(ActionSleep.class);
		final int meta = sleepAI.getBedMeta();

		if (meta == 0)
		{
			entity.rotationYawHead = 180.0F;
			GL11.glTranslated(-0.5D, 0.0D, 0.0D);
			GL11.glRotated(90, -1, 0, 0);
			GL11.glTranslated(0.0D, 0.0D, -0.75D);
		}

		else if (meta == 3)
		{
			entity.rotationYawHead = 90.0F;
			GL11.glTranslated(0.5D, 0.0D, 0.0D);
			GL11.glRotated(90, -1, 0, 0);
			GL11.glTranslated(0.0D, 0.0D, -0.75D);
		}

		else if (meta == 2)
		{
			entity.rotationYawHead = 0.0F;
			GL11.glTranslated(0.5D, 0.0D, -1.0D);
			GL11.glRotated(90, -1, 0, 0);
			GL11.glTranslated(0.0D, 0.0D, -0.75D);
		}

		else if (meta == 1)
		{
			entity.rotationYawHead = -90.0F;
			GL11.glTranslated(-0.5D, 0.0D, -1.0D);
			GL11.glRotated(90, -1, 0, 0);
			GL11.glTranslated(0.0D, 0.0D, -0.75D);
		}
	}
	
	@Override
	public void doRender(EntityVillagerMCA entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		doRenderEntity(entity, x, y, z, entityYaw, partialTicks);
		doRenderEffects(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityVillagerMCA entity)
	{
		final EntityVillagerMCA human = (EntityVillagerMCA)entity;
		final String skinName = human.attributes.getHeadTexture();

		if (skinName.isEmpty())
		{
			return new ResourceLocation("minecraft:textures/entity/steve.png");
		}

		else
		{
			return new ResourceLocation(((EntityVillagerMCA)entity).attributes.getHeadTexture());
		}
	}
}
