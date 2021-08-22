package mca.client.render;

import mca.client.model.VillagerEntityModelMCA;
import mca.client.model.ZombieVillagerEntityModelMCA;
import mca.client.render.layer.ClothingLayer;
import mca.client.render.layer.FaceLayer;
import mca.client.render.layer.HairLayer;
import mca.client.render.layer.SkinLayer;
import mca.entity.ZombieVillagerEntityMCA;
import mca.util.compat.model.Dilation;
import mca.util.compat.model.ModelData;
import mca.util.compat.model.TexturedModelData;
import net.minecraft.client.render.entity.EntityRenderDispatcher;

public class ZombieVillagerEntityMCARenderer extends VillagerLikeEntityMCARenderer<ZombieVillagerEntityMCA> {
    public ZombieVillagerEntityMCARenderer(EntityRenderDispatcher ctx) {
        super(ctx, createModel(VillagerEntityModelMCA.bodyData(Dilation.NONE), false).hideWears());

        addFeature(new SkinLayer<>(this, model));
        addFeature(new FaceLayer<>(this, createModel(VillagerEntityModelMCA.bodyData(new Dilation(0.01F)), false).hideWears(), "zombie"));
        addFeature(new ClothingLayer<>(this, createModel(VillagerEntityModelMCA.clothingData(new Dilation(0.075F)), true), "zombie"));
        addFeature(new HairLayer<>(this, createModel(VillagerEntityModelMCA.hairData(new Dilation(0.1F)), true)));
    }

    private static VillagerEntityModelMCA<ZombieVillagerEntityMCA> createModel(ModelData data, boolean clothing) {
        return new ZombieVillagerEntityModelMCA<>(TexturedModelData.of(data, 64, 64).createModel(), clothing);
    }

    @Override
    protected boolean isShaking(ZombieVillagerEntityMCA entity) {
        return entity.isConverting() || entity.isConvertingInWater();
    }
}
