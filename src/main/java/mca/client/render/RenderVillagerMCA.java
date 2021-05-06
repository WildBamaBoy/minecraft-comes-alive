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

public class RenderVillagerMCA extends BipedRenderer<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>> {
    private static final ResourceLocation gui = new ResourceLocation("mca:textures/gui.png");
    private static final float LABEL_SCALE = 0.027F;

    private final EntityRendererManager renderManager;

    public RenderVillagerMCA(EntityRendererManager manager) {
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
    public ResourceLocation getTextureLocation(EntityVillagerMCA villager) {
        EnumGender gender = EnumGender.byId(villager.gender.get());
        int skin = (int) Math.min(9, Math.max(0, villager.GENE_SKIN.get() * 10));
        String s = String.format("mca:skins/skin/%s/%d.png", gender == EnumGender.FEMALE ? "female" : "male", skin);
        return ResourceLocationCache.get(s);
    }

    @Override
    protected boolean shouldShowName(EntityVillagerMCA villager) {
        if (Minecraft.getInstance().player != null) {
            return Minecraft.getInstance().player.distanceTo(villager) < 5.0F;
        }
        return false;
    }
}
