package mca.client.model;

import com.google.common.collect.ImmutableList;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.model.ModelPart;

public class VillagerEntityArmorModelMCA<T extends VillagerEntityMCA> extends VillagerEntityBaseModelMCA<T> {
    public final ModelPart breasts;

    public VillagerEntityArmorModelMCA() {
        this(1.0f, 1.0f);
    }

    public VillagerEntityArmorModelMCA(float modelSize, float headSize) {
        super(32, modelSize, true);

        breasts = newBreasts(modelSize, true, 0);
    }

    @Override
    protected Iterable<ModelPart> breastsParts() {
        return ImmutableList.of(breasts);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float p_225597_5_, float p_225597_6_) {
        super.setAngles(entity, limbSwing, limbSwingAmount, ageInTicks, p_225597_5_, p_225597_6_);

        breasts.copyTransform(body);
    }

    public void copyPropertiesTo(VillagerEntityModelMCA<T> target) {
        super.setAttributes(target);

        target.breasts.copyTransform(breasts);
    }
}