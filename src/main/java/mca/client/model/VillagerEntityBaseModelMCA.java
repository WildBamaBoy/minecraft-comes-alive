package mca.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerEntityBaseModelMCA<T extends VillagerEntityMCA> extends BipedModel<T> {
    protected final boolean cloth;
    public float breastSize = 1.0f;
    public float headSize = 1.0f;

    public VillagerEntityBaseModelMCA() {
        this(32, 1.0f, true);
    }

    public VillagerEntityBaseModelMCA(int size, float modelSize, boolean cloth) {
        super(modelSize, 0.0F, 64, size);
        this.cloth = cloth;
    }

    protected ModelRenderer newBreasts(float modelSize, boolean cloth, int oy) {
        ModelRenderer breasts = new ModelRenderer(this, 18, 21 + oy);
        if (cloth) {
            breasts.addBox(-3.25F, -1.25F, -1.5F, 6, 3, 3, modelSize);
        } else {
            breasts.texOffs(17, 21 + oy);
            breasts.addBox(-3.25F, -1.25F, -1.5F, 3, 3, 3, modelSize);
            breasts.texOffs(22, 21 + oy);
            breasts.addBox(0.25F, -1.25F, -1.5F, 3, 3, 3, modelSize);
        }
        breasts.setPos(0F, 0F, 0F);
        breasts.mirror = true;
        return breasts;
    }

    @Override
    protected Iterable<ModelRenderer> headParts() {
        return ImmutableList.of(head, hat);
    }

    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg);
    }

    protected Iterable<ModelRenderer> breastsParts() {
        return ImmutableList.of();
    }

    public void renderToBuffer(MatrixStack transform, IVertexBuilder vertexBuilder, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
        //head
        transform.pushPose();
        transform.scale(headSize, headSize, headSize);
        transform.translate(0.0D, 0.0f, 0.0f);
        this.headParts().forEach((p_228230_8_) -> p_228230_8_.render(transform, vertexBuilder, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_));
        transform.popPose();

        //body
        this.bodyParts().forEach((p_228227_8_) -> p_228227_8_.render(transform, vertexBuilder, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_));

        //breasts
        if (breastSize > 0.0) {
            transform.pushPose();
            transform.translate(cloth ? 0.0625 * 0.25 : 0.0, 0.175D + breastSize * 0.1, -0.11D);
            transform.scale(cloth ? 1.166666f : 1.0f, 1.0f, 0.75f + breastSize * 0.5f);
            transform.scale(breastSize * 0.3f + 0.85f, breastSize * 0.75f + 0.75f, breastSize * 0.75f + 0.75f);
            for (ModelRenderer part : breastsParts()) {
                part.xRot = (float) Math.PI * 0.3f; //TODO this will cause minor distortion
                part.render(transform, vertexBuilder, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            }
            transform.popPose();
        }
    }
}
