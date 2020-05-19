package mca.client.render;

import mca.api.objects.Pos;
import org.lwjgl.opengl.GL11;

import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumAgeState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RenderVillagerMCA<T extends EntityVillagerMCA> extends RenderBiped<EntityVillagerMCA> {
    private static final ResourceLocation gui = new ResourceLocation("mca:textures/gui.png");
    private static final float LABEL_SCALE = 0.027F;

    public RenderVillagerMCA(RenderManager manager) {
        super(manager, new ModelVillagerMCA(), 0.5F);
        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
    }

    @Override
    protected void preRenderCallback(EntityVillagerMCA villager, float partialTickTime) {
        if (villager.isChild()) {
            float scaleForAge = EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)).getScaleForAge();
            GlStateManager.scale(scaleForAge, scaleForAge, scaleForAge);
        }

        if (villager.isRiding()) {
            GlStateManager.translate(0, 0.5, 0);
        }
    }

    @Override
    public void renderName(EntityVillagerMCA entity, double x, double y, double z) {
        float modY = entity.get(EntityVillagerMCA.SLEEPING) ? -1.5F : 0;
        super.renderName(entity, x, y + modY, z);

        if (canRenderName(entity)) {
            if (entity.getHealth() < entity.getMaxHealth()) {
                renderHealth(entity, x, y + modY, z, (int) entity.getHealth(), (int) entity.getMaxHealth());
            }

            if (entity.getCurrentActivity() != null) {
                double d0 = entity.getDistanceSq(this.renderManager.renderViewEntity);
                float f = entity.isSneaking() ? NAME_TAG_RANGE_SNEAK : NAME_TAG_RANGE;
                if (d0 < (double) (f * f)) {
                    this.renderEntityName(entity, x, y - 0.25F + modY , z, "(" + entity.getCurrentActivity() + ")", d0);
                }
            }
        }
    }

    private void renderHealth(EntityVillagerMCA villager, double posX, double posY, double posZ, int currentHealth, int maxHealth) {
        final int redHeartU = 80;
        final int darkHeartU = 96;
        int heartsDrawn = 0;

        float maxHealthF = Math.round((float)maxHealth / 2.0F);
        float currentHealthF = Math.round((float)currentHealth / 2.0F);
        int heartsMax = Math.round((maxHealthF / maxHealthF) * 10.0F);
        int heartsToDraw = Math.round((currentHealthF / maxHealthF) * 10.0F);

        for (int i = 0; i < heartsMax; i++) {
            int heartU = i < heartsToDraw ? redHeartU : darkHeartU;
            heartsDrawn++;

            GL11.glPushMatrix();{
                GL11.glTranslatef((float) posX + 0.0F, (float) posY + villager.height + 1.0F, (float) posZ);
                GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                GL11.glScalef(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glTranslatef(-2.0F, 2.0F, -2.0F);
                drawTexturedRectangle(gui, (int)posX + (heartsDrawn * 8) - 45, (int)posY - 4, heartU, 0, 16, 16);
            }
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVillagerMCA villager) {
        return villager.getTextureResourceLocation();
    }

    @Override
    protected boolean canRenderName(EntityVillagerMCA entity) {
        float distance = Minecraft.getMinecraft().player.getDistance(entity);
        return distance < 5F;
    }

    public static void drawTexturedRectangle(ResourceLocation texture, int x, int y, int u, int v, int width, int height)
    {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        float f = 0.00390625F;
        float f1 = 0.00390625F;

        final Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x + 0, y + height, 0.0D).tex((u + 0) * f, ((v + height) * f1)).endVertex();
        buffer.pos(x + width, y + height, 0.0D).tex((u + width) * f, ((v + height) * f1)).endVertex();
        buffer.pos(x + width, y + 0,	0.0D).tex((u + width) * f, ((v + 0) * f1)).endVertex();
        buffer.pos(x + 0, y + 0, 0.0D).tex((u + 0) * f, ((v + 0) * f1)).endVertex();
        tessellator.draw();
    }

    @Override
    protected void renderLivingAt(EntityVillagerMCA entityLiving, double x, double y, double z) {
        if (entityLiving.isEntityAlive() && entityLiving.get(EntityVillagerMCA.SLEEPING)) {
            super.renderLivingAt(entityLiving, x + (double)entityLiving.getRenderOffsetX(), y + (double)entityLiving.getRenderOffsetY(), z + (double)entityLiving.getRenderOffsetZ());
        } else {
            super.renderLivingAt(entityLiving, x, y, z);
        }
    }

    @Override
    protected void applyRotations(EntityVillagerMCA entity, float p_77043_2_, float rotationYaw, float partialTicks) {
        if (entity.get(EntityVillagerMCA.SLEEPING)) {
            GlStateManager.rotate(getBedOrientationInDegrees(entity), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.getDeathMaxRotation(entity), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
            rotationYaw = 180.0f;
        }
        super.applyRotations(entity, p_77043_2_, rotationYaw, partialTicks);
    }
    
	private float getBedOrientationInDegrees(EntityVillagerMCA entity) {
        BlockPos bedLocation = entity.get(EntityVillagerMCA.BED_POS);
        IBlockState state = bedLocation == BlockPos.ORIGIN ? null : entity.world.getBlockState(new Pos(bedLocation));
        if (state != null && state.getBlock().isBed(state, entity.world.getVanillaWorld(), bedLocation, entity)) {
            EnumFacing enumfacing = state.getBlock().getBedDirection(state, entity.world.getVanillaWorld(), bedLocation);

            switch (enumfacing) {
                case SOUTH:
                    return 90.0F;
                case WEST:
                    return 0.0F;
                case NORTH:
                    return 270.0F;
                case EAST:
                    return 180.0F;
			default:
				break;
            }
        }

        return 0.0F;
    }
}
