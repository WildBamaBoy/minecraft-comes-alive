package mca.client.model;

import com.google.common.collect.ImmutableList;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.renderer.model.ModelRenderer;

public class VillagerEntityArmorModelMCA<T extends VillagerEntityMCA> extends VillagerEntityBaseModelMCA<T> {
    public final ModelRenderer breasts;

    public VillagerEntityArmorModelMCA() {
        this(1.0f, 1.0f);
    }

    public VillagerEntityArmorModelMCA(float modelSize, float headSize) {
        super(32, modelSize, true);

        breasts = newBreasts(modelSize, true, 0);
    }

    @Override
    protected Iterable<ModelRenderer> breastsParts() {
        return ImmutableList.of(breasts);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float p_225597_5_, float p_225597_6_) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, p_225597_5_, p_225597_6_);

        breasts.copyFrom(body);
    }

    public void copyPropertiesTo(VillagerEntityModelMCA<T> target) {
        super.copyPropertiesTo(target);

        target.breasts.copyFrom(breasts);
    }
}