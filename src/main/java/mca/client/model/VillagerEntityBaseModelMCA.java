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

    protected static final String BREASTS = "breasts";

    private final boolean cloth;

    protected final ModelPart breasts;

    public float breastSize = 1;
    public float headSize = 1;
    public float headWidth = 1;

    public VillagerEntityBaseModelMCA(ModelPart root, boolean clothing) {
        super(root);
        this.cloth = clothing;
        this.breasts = root.getChild(BREASTS);
    }

    public static ModelData getModelData(Dilation dilation, boolean clothing) {
        ModelData modelData = BipedEntityModel.getModelData(dilation, 0);
        ModelPartData data = modelData.getRoot();

        data.addChild(BREASTS, newBreasts(dilation, clothing, 0), ModelTransform.NONE);

        return modelData;
    }

    protected static ModelPartBuilder newBreasts(Dilation dilation, boolean clothing, int oy) {
        ModelPartBuilder builder = ModelPartBuilder.create().mirrored();

        if (clothing) {
            builder.uv(18, 21 + oy).cuboid(-3.25F, -1.25F, -1.5F, 6, 3, 3, dilation);
        } else {
            builder
                .uv(17, 21 + oy).cuboid(-3.25F, -1.25F, -1.5F, 3, 3, 3, dilation)
                .uv(22, 21 + oy).cuboid(0.25F, -1.25F, -1.5F, 3, 3, 3, dilation);
        }
        return builder;

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
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);

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
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        //head
        matrices.push();
        matrices.scale(headWidth, headSize, headWidth);
        this.getHeadParts().forEach(a -> a.render(matrices, vertices, light, overlay, red, green, blue, alpha));
        matrices.pop();

        //body
        this.getBodyParts().forEach(a -> a.render(matrices, vertices, light, overlay, red, green, blue, alpha));

        //breasts
        if (breastSize > 0) {
            matrices.push();
            matrices.translate(cloth ? 0.0625 * 0.25 : 0.0, 0.175D + breastSize * 0.1, -0.11D);
            matrices.scale(cloth ? 1.166666f : 1.0f, 1.0f, 0.75f + breastSize * 0.5f);
            matrices.scale(breastSize * 0.3f + 0.85f, breastSize * 0.75f + 0.75f, breastSize * 0.75f + 0.75f);
            for (ModelPart part : breastsParts()) {
                part.pitch = (float) Math.PI * 0.3f;//TODO this will cause minor distortion
                part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
            }
            matrices.pop();
        }
    }
}
