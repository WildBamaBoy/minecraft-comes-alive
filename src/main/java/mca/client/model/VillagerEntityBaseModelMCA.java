package mca.client.model;

import com.google.common.collect.ImmutableList;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class VillagerEntityBaseModelMCA<T extends VillagerEntityMCA> extends BipedEntityModel<T> {
    protected final boolean cloth;
    public float breastSize = 1.0f;
    public float headSize = 1.0f;
    public float headWidth = 1.0f;

    public VillagerEntityBaseModelMCA() {
        this(32, 1.0f, true);
    }

    public VillagerEntityBaseModelMCA(int size, float modelSize, boolean cloth) {
        super(modelSize, 0.0F, 64, size);
        this.cloth = cloth;
    }

    protected ModelPart newBreasts(float modelSize, boolean cloth, int oy) {
        ModelPart breasts = new ModelPart(this, 18, 21 + oy);
        if (cloth) {
            breasts.addBox(-3.25F, -1.25F, -1.5F, 6, 3, 3, modelSize);
        } else {
            breasts.texOffs(17, 21 + oy);
            breasts.addBox(-3.25F, -1.25F, -1.5F, 3, 3, 3, modelSize);
            breasts.texOffs(22, 21 + oy);
            breasts.addBox(0.25F, -1.25F, -1.5F, 3, 3, 3, modelSize);
        }
        breasts.setPivot(0F, 0F, 0F);
        breasts.mirror = true;
        return breasts;
    }

    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of(head, hat);
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg);
    }

    protected Iterable<ModelPart> breastsParts() {
        return ImmutableList.of();
    }

    public void render(MatrixStack transform, VertexConsumer vertexBuilder, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
        //head
        transform.push();
        transform.scale(headWidth, headSize, headWidth);
        transform.translate(0.0D, 0.0f, 0.0f);
        this.getHeadParts().forEach((p_228230_8_) -> p_228230_8_.render(transform, vertexBuilder, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_));
        transform.pop();

        //body
        this.getBodyParts().forEach((p_228227_8_) -> p_228227_8_.render(transform, vertexBuilder, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_));

        //breasts
        if (breastSize > 0.0) {
            transform.push();
            transform.translate(cloth ? 0.0625 * 0.25 : 0.0, 0.175D + breastSize * 0.1, -0.11D);
            transform.scale(cloth ? 1.166666f : 1.0f, 1.0f, 0.75f + breastSize * 0.5f);
            transform.scale(breastSize * 0.3f + 0.85f, breastSize * 0.75f + 0.75f, breastSize * 0.75f + 0.75f);
            for (ModelPart part : breastsParts()) {
                part.pitch = (float) Math.PI * 0.3f; //TODO this will cause minor distortion
                part.render(transform, vertexBuilder, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            }
            transform.pop();
        }
    }
}
