package mca.client.render;

import mca.client.model.VillagerEntityModelMCA;
import mca.client.model.ZombieVillagerEntityModelMCA;
import mca.client.render.layer.ClothingLayer;
import mca.client.render.layer.FaceLayer;
import mca.client.render.layer.HairLayer;
import mca.client.render.layer.SkinLayer;
import mca.entity.ZombieVillagerEntityMCA;
import mca.util.compat.model.Dilation;
import mca.util.compat.model.TexturedModelData;
import net.minecraft.client.render.entity.EntityRenderDispatcher;

public class ZombieVillagerEntityMCARenderer extends VillagerLikeEntityMCARenderer<ZombieVillagerEntityMCA> {
    public ZombieVillagerEntityMCARenderer(EntityRenderDispatcher ctx) {
        super(ctx, createModel(0, 0, false, false));

        addFeature(new SkinLayer<>(this, createModel(0, 0, false, true)));
        addFeature(new ClothingLayer<>(this, createModel(0.075F, 0.1F, true, false), "zombie"));
        addFeature(new HairLayer<>(this, createModel(0.1F, 2.05F, false, false)));
        addFeature(new FaceLayer<>(this, createModel(0.01F, 0.01F, false, true), "zombie"));
    }

    private static VillagerEntityModelMCA<ZombieVillagerEntityMCA> createModel(float dilation, float headSize, boolean cloth, boolean hideWear) {
        return new ZombieVillagerEntityModelMCA<>(
                TexturedModelData.of(
                        VillagerEntityModelMCA.getModelData(new Dilation(dilation), new Dilation(headSize), cloth), 64, 64)
                .createModel(), cloth, hideWear);
    }

    @Override
    protected boolean isShaking(ZombieVillagerEntityMCA entity) {
        return entity.isConverting() || entity.isConvertingInWater();
    }
}
