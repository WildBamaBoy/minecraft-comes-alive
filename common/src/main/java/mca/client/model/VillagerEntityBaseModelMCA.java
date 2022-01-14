package mca.client.model;

import com.google.common.collect.ImmutableList;
import mca.entity.VillagerLike;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.VillagerDimensions;
import net.minecraft.client.model.ModelPart;
import mca.util.compat.model.ModelTransform;
import mca.util.compat.model.BipedEntityModelCompat;
import mca.util.compat.model.Dilation;
import mca.util.compat.model.ModelData;
import mca.util.compat.model.ModelPartCompat;
import mca.util.compat.model.ModelPartBuilder;
import mca.util.compat.model.ModelPartData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;

public class VillagerEntityBaseModelMCA<T extends MobEntity & VillagerLike<T>> extends BipedEntityModel<T> {

    protected static final String BREASTS = "breasts";

    protected final ModelPart breasts;

    private float breastSize;
    private VillagerDimensions dimensions;

    public VillagerEntityBaseModelMCA(ModelPartCompat root) {
        super(root.getOriginalDilation(), 0, root.getTextureWidth(), root.getTextureHeight());
        this.breasts = root.getChild(BREASTS);
    }

    public static ModelData getModelData(Dilation dilation) {
        ModelData modelData = BipedEntityModelCompat.getModelData(dilation, 0);
        ModelPartData data = modelData.getRoot();

        data.addChild(BREASTS, newBreasts(dilation, 0), ModelTransform.NONE);

        return modelData;
    }

    protected static ModelPartBuilder newBreasts(Dilation dilation, int oy) {
        ModelPartBuilder builder = ModelPartBuilder.create();
        builder.uv(18, 21 + oy).cuboid(-3.25F, -1.25F, -1.5F, 6, 3, 3, dilation);
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
    public void animateModel(T entity, float limbAngle, float limbDistance, float tickDelta) {
        super.animateModel(entity, limbDistance, limbAngle, tickDelta);
        riding |= entity.getAgeState() == AgeState.BABY;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        if (entity.getAgeState() == AgeState.BABY && !entity.hasVehicle()) {
            limbDistance = (float)Math.sin(entity.age / 12F);
            limbAngle = (float)Math.cos(entity.age / 9F) * 3;
            headYaw += (float)Math.sin(entity.age / 2F);
        }
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);

        if (entity.getVillagerBrain().isPanicking()) {
            float toRadiums = (float)Math.PI / 180;

            float armRaise = (((float)Math.sin(animationProgress / 5) * 30 - 180)
                    + ((float)Math.sin(animationProgress / 3) * 3))
                    * toRadiums;
            float waveSideways = ((float)Math.sin(animationProgress / 2) * 12 - 17) * toRadiums;

            this.leftArm.pitch = armRaise;
            this.leftArm.roll = -waveSideways;
            this.rightArm.pitch = -armRaise;
            this.rightArm.roll = waveSideways;
        }

        dimensions = entity.getVillagerDimensions();
        breastSize = entity.getGenetics().getBreastSize();

        breasts.visible = entity.getGenetics().getGender() == Gender.FEMALE;
        breasts.copyTransform(body);
    }

    @Override
    public void setAttributes(BipedEntityModel<T> target) {
        super.setAttributes(target);

        if (target instanceof VillagerEntityBaseModelMCA) {
            VillagerEntityBaseModelMCA<T> m = (VillagerEntityBaseModelMCA<T>)target;
            m.dimensions = dimensions;
            m.breastSize = breastSize;
            m.breasts.visible = breasts.visible;
            m.breasts.copyTransform(breasts);
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        //head
        float headSize = dimensions.getHead();

        matrices.push();
        matrices.scale(headSize, headSize, headSize);
        this.getHeadParts().forEach(a -> a.render(matrices, vertices, light, overlay, red, green, blue, alpha));
        matrices.pop();

        //body
        this.getBodyParts().forEach(a -> a.render(matrices, vertices, light, overlay, red, green, blue, alpha));

        if (breasts.visible && body.visible) {
            float breastSize = this.breastSize * dimensions.getBreasts();

            if (breastSize > 0) {
                matrices.push();
                matrices.translate(0.0625 * 0.25, 0.175D + breastSize * 0.1, -0.075D - breastSize * 0.05);
                matrices.scale(1.166666f, 0.8f + breastSize * 0.3f, 0.75f + breastSize * 0.45f);
                matrices.scale(breastSize * 0.275f + 0.85f, breastSize * 0.65f + 0.75f, breastSize * 0.65f + 0.75f);
                for (ModelPart part : breastsParts()) {
                    part.pitch = (float)Math.PI * 0.3f;
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
                matrices.pop();
            }
        }
    }
}
