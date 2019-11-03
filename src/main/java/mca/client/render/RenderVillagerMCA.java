package mca.client.render;

import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumAgeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

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
        super.renderName(entity, x, y ,z);
        if (canRenderName(entity)) {
            if (entity.getHealth() < entity.getMaxHealth()) {
                renderHealth(entity, x, y, z, (int) entity.getHealth(), (int) entity.getMaxHealth());
            }

            if (entity.getCurrentActivity() != null) {
                double d0 = entity.getDistanceSq(this.renderManager.renderViewEntity);
                float f = entity.isSneaking() ? NAME_TAG_RANGE_SNEAK : NAME_TAG_RANGE;
                if (d0 < (double) (f * f)) {
                    this.renderEntityName(entity, x, y - 0.25F, z, "(" + entity.getCurrentActivity() + ")", d0);
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
        if (entityLiving.isEntityAlive() && entityLiving.isSleeping()) {
            super.renderLivingAt(entityLiving, x + (double)entityLiving.renderOffsetX, y + (double)entityLiving.renderOffsetY, z + (double)entityLiving.renderOffsetZ);
        } else {
            super.renderLivingAt(entityLiving, x, y, z);
        }
    }

    @Override
    protected void applyRotations(EntityVillagerMCA entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
        if (entityLiving.isSleeping()) {
            GlStateManager.rotate(entityLiving.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
            rotationYaw = 180.0f;
        }
        super.applyRotations(entityLiving, p_77043_2_, rotationYaw, partialTicks);
    }
}
