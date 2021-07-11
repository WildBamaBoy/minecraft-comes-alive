package mca.client.model;

import com.google.common.collect.ImmutableList;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class VillagerEntityBaseModelMCA<T extends VillagerEntityMCA> extends BipedEntityModel<T> {
    private final boolean cloth;

    public final ModelPart breasts;

    public float breastSize = 1;
    public float headSize = 1;
    public float headWidth = 1;

    public VillagerEntityBaseModelMCA(ModelPart root, boolean clothing) {
        super(root);
        this.cloth = clothing;
        this.breasts = root.getChild("breasts");
    }

    public static ModelData getModelData(Dilation dilation, boolean clothing) {
        ModelData modelData = BipedEntityModel.getModelData(dilation, 0);
        ModelPartData data = modelData.getRoot();

        VillagerEntityBaseModelMCA.newBreasts("breasts", data, dilation, clothing, 0);

        return modelData;
    }

    protected static void newBreasts(String name, ModelPartData data, Dilation dilation, boolean cloth, int oy) {
        ModelPartBuilder builder = ModelPartBuilder.create().mirrored();

        if (cloth) {
            builder.uv(18, 21 + oy).cuboid(-3.25F, -1.25F, -1.5F, 6, 3, 3, dilation);
        } else {
            builder
                .uv(17, 21 + oy).cuboid(-3.25F, -1.25F, -1.5F, 3, 3, 3, dilation)
                .uv(22, 21 + oy).cuboid(0.25F, -1.25F, -1.5F, 3, 3, 3, dilation);
        }

        data.addChild(name,
                builder,
                ModelTransform.pivot(0, 0, 0)
        );
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
        return ImmutableList.of(breasts);
    }

    @Override
    public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float p_225597_5_, float p_225597_6_) {
        super.setAngles(entity, limbSwing, limbSwingAmount, ageInTicks, p_225597_5_, p_225597_6_);

        breasts.copyTransform(body);
    }

    @Override
    public void setAttributes(BipedEntityModel<T> target) {
        super.setAttributes(target);

        if (target instanceof VillagerEntityBaseModelMCA) {
            ((VillagerEntityBaseModelMCA<T>)target).breasts.copyTransform(breasts);
        }
    }

    @Override
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
