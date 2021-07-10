package mca.client.render;

import mca.client.model.VillagerEntityArmorModelMCA;
import mca.client.model.VillagerEntityBaseModelMCA;
import mca.client.model.VillagerEntityModelMCA;
import mca.client.render.layer.ClothingLayer;
import mca.client.render.layer.FaceLayer;
import mca.client.render.layer.HairLayer;
import mca.client.render.layer.SkinLayer;
import mca.entity.VillagerEntityMCA;
import mca.enums.AgeState;
import mca.enums.Gender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class VillagerEntityMCARenderer extends BipedEntityRenderer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    List<VillagerEntityBaseModelMCA<VillagerEntityMCA>> models = new LinkedList<>();

    public VillagerEntityMCARenderer(EntityRenderDispatcher manager) {
        super(manager, new VillagerEntityModelMCA<>(), 0.5F);

        this.addFeature(new SkinLayer(this, createModel(0.0f, 0.0f, false, true)));
        this.addFeature(new ClothingLayer(this, createModel(0.0833f, 0.16666f, true, false)));
        this.addFeature(new HairLayer(this, createModel(0.16666f, 0.0833f, false, false)));
        this.addFeature(new FaceLayer(this, createModel(0.01f, 0.01f, false, true)));
        this.addFeature(new ArmorFeatureRenderer<>(this,
                createArmorModel(0.5f),
                createArmorModel(1.0f)));
        this.addFeature(new HeldItemFeatureRenderer<>(this));
    }

    private VillagerEntityModelMCA<VillagerEntityMCA> createModel(float modelSize, float headSize, boolean cloth, boolean hideWear) {
        VillagerEntityModelMCA<VillagerEntityMCA> m = new VillagerEntityModelMCA<>(modelSize, headSize, cloth, hideWear);
        models.add(m);
        return m;
    }

    private VillagerEntityArmorModelMCA<VillagerEntityMCA> createArmorModel(float modelSize) {
        VillagerEntityArmorModelMCA<VillagerEntityMCA> m = new VillagerEntityArmorModelMCA<>(modelSize, modelSize);
        models.add(m);
        return m;
    }

    @Override
    protected void scale(VillagerEntityMCA villager, MatrixStack matrixStackIn, float partialTickTime) {
        AgeState ageState = villager.getAgeState();
        float scale = ageState.getHeight();
        matrixStackIn.scale(scale, scale, scale);

        //dimensions
        float height = villager.gene_size.get() * 0.5f + 0.75f;
        float width = villager.gene_width.get() * 0.5f + 0.75f;
        width *= ageState.getWidth();
        matrixStackIn.scale(width, height, width);
    }

    @Nullable
    @Override
    protected RenderLayer getRenderType(VillagerEntityMCA p_230496_1_, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_) {
        //setting the type to null prevents it from rendering
        //we need a skin layer anyways because of the color
        return null;
    }

    @Override
    public void render(VillagerEntityMCA villager, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, VertexConsumerProvider p_225623_5_, int p_225623_6_) {
        AgeState ageState = villager.getAgeState();
        model.headSize = ageState.getHead();
        model.headWidth = model.headSize / ageState.getWidth();
        model.breastSize = villager.getGender() == Gender.FEMALE ? villager.gene_breast.get() * ageState.getBreasts() : 0.0f;

        //also apply this to the layers models, not sure if this is the intended solution
        for (VillagerEntityBaseModelMCA<VillagerEntityMCA> m : models) {
            m.breastSize = this.model.breastSize;
            m.headSize = this.model.headSize;
            m.headWidth = this.model.headWidth;
        }

        super.render(villager, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
    }

    @Override
    protected boolean shouldShowName(VillagerEntityMCA villager) {
        if (MinecraftClient.getInstance().player != null) {
            return MinecraftClient.getInstance().player.squaredDistanceTo(villager) < 25.0F;
        }
        return false;
    }
}
