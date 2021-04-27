package mca.client.render;

import cobalt.util.ResourceLocationCache;
import com.mojang.blaze3d.matrix.MatrixStack;
import mca.client.colors.SkinColors;
import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumAgeState;
import mca.enums.EnumGender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

public class RenderVillagerMCA extends BipedRenderer<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>> {
    private static final ResourceLocation gui = new ResourceLocation("mca:textures/gui.png");
    private static final float LABEL_SCALE = 0.027F;

    private final EntityRendererManager renderManager;

    public RenderVillagerMCA(EntityRendererManager manager, boolean p_i46103_2_) {
        super(manager, new ModelVillagerMCA(), 0.5F);

        renderManager = manager;

        this.addLayer(new LayerClothing(this, new ModelVillagerMCA(0.16666f, 0.0833f, true)));
        this.addLayer(new LayerHair(this, new ModelVillagerMCA(0.0833f, 0.16666f, false)));
        this.addLayer(new LayerFace(this, new ModelVillagerMCA(0.0f, 0.0f, false)));
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel(0.5F), new BipedModel(1.0F)));
        this.addLayer(new HeldItemLayer<>(this));
    }

    @Override
    protected void scale(EntityVillagerMCA villager, MatrixStack matrixStackIn, float partialTickTime) {
        if (villager.isBaby()) {
            float scaleForAge = EnumAgeState.byId(villager.ageState.get()).getScaleForAge();
            matrixStackIn.scale(scaleForAge, scaleForAge, scaleForAge);
        }

        //dimensions TODO the head gets deformed when turning
        float height = villager.GENE_SIZE.get() * 0.5f + 0.75f;
        float width = villager.GENE_WIDTH.get() * 0.5f + 0.75f;
        matrixStackIn.scale(width, height, width);
    }

    @Override
    public void render(EntityVillagerMCA villager, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
        super.render(villager, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);

        float melanin = villager.GENE_MELANIN.get();
        float hemoglobin = villager.GENE_HEMOGLOBIN.get();

        double[] color = SkinColors.getColor(melanin, hemoglobin);
//        GlStateManager.color((float) color[0], (float) color[1], (float) color[2]);
    }

    @Override
    protected void renderNameTag(EntityVillagerMCA villager, ITextComponent p_225629_2_, MatrixStack p_225629_3_, IRenderTypeBuffer p_225629_4_, int p_225629_5_) {
        super.renderNameTag(villager, p_225629_2_, p_225629_3_, p_225629_4_, p_225629_5_);

        if (shouldShowName(villager)) {
            if (villager.getHealth() < villager.getMaxHealth()) {
                renderHealth(villager, villager.getX(), villager.getY(), villager.getZ(), (int) villager.getHealth(), (int) villager.getMaxHealth());
            }
        }
    }


    private void renderHealth(EntityVillagerMCA villager, double posX, double posY, double posZ, int currentHealth, int maxHealth) {
        final int redHeartU = 80;
        final int darkHeartU = 96;
        int heartsDrawn = 0;

        float maxHealthF = Math.round((float) maxHealth / 2.0F);
        float currentHealthF = Math.round((float) currentHealth / 2.0F);
        int heartsMax = Math.round((maxHealthF / maxHealthF) * 10.0F);
        int heartsToDraw = Math.round((currentHealthF / maxHealthF) * 10.0F);

        for (int i = 0; i < heartsMax; i++) {
            int heartU = i < heartsToDraw ? redHeartU : darkHeartU;
            heartsDrawn++;

            GL11.glPushMatrix();
            {
//                GL11.glTranslatef((float) posX + 0.0F, (float) posY + villager.height + 1.0F, (float) posZ);
//                GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
//                GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                GL11.glScalef(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glTranslatef(-2.0F, 2.0F, -2.0F);
                drawTexturedRectangle(gui, (int) posX + (heartsDrawn * 8) - 45, (int) posY - 4, heartU, 0, 16, 16);
            }
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityVillagerMCA villager) {
        EnumGender gender = EnumGender.byId(villager.gender.get());
        int skin = (int) Math.min(9, Math.max(0, villager.GENE_SKIN.get() * 10));
        String s = String.format("mca:skins/skin/%s/%d.png", gender == EnumGender.FEMALE ? "female" : "male", skin);
        return ResourceLocationCache.get(s);
    }

    @Override
    protected boolean shouldShowName(EntityVillagerMCA villager) {
        if (Minecraft.getInstance().player != null) {
            if (Minecraft.getInstance().player.distanceTo(villager) < 5.0F) {
                return true;
            }
        }
        return false;
    }

    public void drawTexturedRectangle(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        renderManager.textureManager.bind(texture);
//
//        float f = 0.00390625F;
//        float f1 = 0.00390625F;
//
//        final Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder buffer = tessellator.getBuilder();
//
//        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
//        buffer.vertex(x, y + height, 0.0D).tex((u) * f, ((v + height) * f1)).endVertex();
//        buffer.vertex(x + width, y + height, 0.0D).tex((u + width) * f, ((v + height) * f1)).endVertex();
//        buffer.vertex(x + width, y, 0.0D).tex((u + width) * f, ((v) * f1)).endVertex();
//        buffer.vertex(x, y, 0.0D).tex((u) * f, ((v) * f1)).endVertex();
//        tessellator.end();
    }
}
