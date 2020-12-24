package com.minecraftcomesalive.mca.client.model;

import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelVillagerMCA<T extends EntityVillagerMCA> extends BipedModel<T> {
    private final ModelRenderer breasts;

    public ModelVillagerMCA() {
        super(0.0F, 0.0F, 64, 64);
        breasts = new ModelRenderer(this, 18, 21);
        breasts.addBox(-3F, 0F, -1F, 6, 3, 3);
        breasts.setRotationPoint(0F, 3.5F, -3F);
        breasts.setTextureSize(64, 64);
        breasts.mirror = true;
    }
}