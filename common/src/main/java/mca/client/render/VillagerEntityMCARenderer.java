package mca.client.render;

import mca.client.model.VillagerEntityModelMCA;
import mca.client.render.layer.ClothingLayer;
import mca.client.render.layer.FaceLayer;
import mca.client.render.layer.HairLayer;
import mca.client.render.layer.SkinLayer;
import mca.entity.VillagerEntityMCA;
import mca.util.compat.model.Dilation;
import mca.util.compat.model.TexturedModelData;
import net.minecraft.client.render.entity.EntityRenderDispatcher;

public class VillagerEntityMCARenderer extends VillagerLikeEntityMCARenderer<VillagerEntityMCA> {
    public VillagerEntityMCARenderer(EntityRenderDispatcher ctx) {
        super(ctx, createModel(0, 0, false, false));

        addFeature(new SkinLayer<>(this, createModel(0.0f, 0.0f, false, true)));
        addFeature(new ClothingLayer<>(this, createModel(0.0833f, 0.16666f, true, false), "normal"));
        addFeature(new HairLayer<>(this, createModel(0.16666f, 0.0833f, false, false)));
        addFeature(new FaceLayer<>(this, createModel(0.01f, 0.01f, false, true), "normal"));
    }

    private static VillagerEntityModelMCA<VillagerEntityMCA> createModel(float dilation, float headSize, boolean cloth, boolean hideWear) {
        return new VillagerEntityModelMCA<>(
                TexturedModelData.of(
                        VillagerEntityModelMCA.getModelData(new Dilation(dilation), cloth), 64, 64)
                .createModel(), cloth, hideWear);
    }

}
