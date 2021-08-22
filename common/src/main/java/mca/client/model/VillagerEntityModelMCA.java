package mca.client.model;

import com.google.common.collect.ImmutableList;
import mca.entity.VillagerLike;
import net.minecraft.client.model.ModelPart;
import mca.util.compat.model.ModelTransform;
import mca.util.compat.model.PlayerEntityModelCompat;
import mca.util.compat.model.Dilation;
import mca.util.compat.model.EntityModelPartNames;
import mca.util.compat.model.ModelData;
import mca.util.compat.model.ModelPartCompat;
import mca.util.compat.model.ModelPartData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.mob.MobEntity;

public class VillagerEntityModelMCA<T extends MobEntity & VillagerLike<T>> extends VillagerEntityBaseModelMCA<T> {
    protected static final String BREASTPLATE = "breastplate";

    public final ModelPart breastsWear;
    public final ModelPart leftArmwear;
    public final ModelPart rightArmwear;
    public final ModelPart leftLegwear;
    public final ModelPart rightLegwear;
    public final ModelPart bodyWear;

    public VillagerEntityModelMCA(ModelPartCompat tree, boolean clothing, boolean hideWear) {
        super(tree, clothing);

        bodyWear = tree.getChild(EntityModelPartNames.JACKET);
        leftArmwear = tree.getChild("left_sleeve");
        rightArmwear = tree.getChild("right_sleeve");
        leftLegwear = tree.getChild("left_pants");
        rightLegwear = tree.getChild("right_pants");
        breastsWear = tree.getChild(BREASTPLATE);

        if (hideWear) {
            breastsWear.visible = false;
            leftArmwear.visible = false;
            rightArmwear.visible = false;
            leftLegwear.visible = false;
            rightLegwear.visible = false;
            bodyWear.visible = false;
        }
    }

    public static ModelData getModelData(Dilation dilation, Dilation headDilation, boolean clothing) {
        ModelData modelData = PlayerEntityModelCompat.getTexturedModelData(dilation, headDilation, false);
        ModelPartData data = modelData.getRoot();
        data.addChild(BREASTS, newBreasts(dilation, clothing, 0), ModelTransform.NONE);
        data.addChild(BREASTPLATE, newBreasts(dilation.add(0.25F), clothing, 0), ModelTransform.NONE);
        return modelData;
    }

    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of(head, hat);
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg, bodyWear, leftLegwear, rightLegwear, leftArmwear, rightArmwear);
    }

    @Override
    protected Iterable<ModelPart> breastsParts() {
        return ImmutableList.of(breasts, breastsWear);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        leftLegwear.copyTransform(leftLeg);
        rightLegwear.copyTransform(rightLeg);
        leftArmwear.copyTransform(leftArm);
        rightArmwear.copyTransform(rightArm);
        bodyWear.copyTransform(body);
        breastsWear.copyTransform(body);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        leftArmwear.visible = visible;
        rightArmwear.visible = visible;
        leftLegwear.visible = visible;
        rightLegwear.visible = visible;
        bodyWear.visible = visible;
    }

    @Override
    public void setAttributes(BipedEntityModel<T> target) {
        super.setAttributes(target);
        if (target instanceof VillagerEntityModelMCA) {
            copyAttributes((VillagerEntityModelMCA<T>)target);
        }
    }

    private void copyAttributes(VillagerEntityModelMCA<T> target) {
        target.leftLegwear.copyTransform(leftLegwear);
        target.rightLegwear.copyTransform(rightLegwear);
        target.leftArmwear.copyTransform(leftArmwear);
        target.rightArmwear.copyTransform(rightArmwear);
        target.bodyWear.copyTransform(bodyWear);
        target.breastsWear.copyTransform(breastsWear);
    }
}
