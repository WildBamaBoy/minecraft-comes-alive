package mca.client.render;

import mca.client.model.VillagerEntityModelMCA;
import mca.client.render.layer.ClothingLayer;
import mca.client.render.layer.FaceLayer;
import mca.client.render.layer.HairLayer;
import mca.client.render.layer.SkinLayer;
import mca.entity.VillagerEntityMCA;
import mca.util.compat.model.Dilation;
import mca.util.compat.model.ModelData;
import mca.util.compat.model.TexturedModelData;
import net.minecraft.client.render.entity.EntityRenderDispatcher;

public class VillagerEntityMCARenderer extends VillagerLikeEntityMCARenderer<VillagerEntityMCA> {
    public VillagerEntityMCARenderer(EntityRenderDispatcher ctx) {
        super(ctx, createModel(VillagerEntityModelMCA.bodyData(Dilation.NONE), false).hideWears());

        addFeature(new SkinLayer<>(this, model));
        addFeature(new FaceLayer<>(this, createModel(VillagerEntityModelMCA.bodyData(new Dilation(0.01F)), false).hideWears(), "normal"));
        addFeature(new ClothingLayer<>(this, createModel(VillagerEntityModelMCA.clothingData(new Dilation(0.075F)), true), "normal"));
        addFeature(new HairLayer<>(this, createModel(VillagerEntityModelMCA.hairData(new Dilation(0.1F)), true)));
    }

    private static VillagerEntityModelMCA<VillagerEntityMCA> createModel(ModelData data, boolean cloth) {
        return new VillagerEntityModelMCA<>(TexturedModelData.of(data, 64, 64).createModel(), cloth);
    }
}
