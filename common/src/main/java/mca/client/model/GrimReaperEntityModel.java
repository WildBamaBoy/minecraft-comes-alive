package mca.client.model;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import mca.entity.GrimReaperEntity;
import mca.entity.ReaperAttackState;
import mca.util.compat.model.ModelTransform;
import mca.util.compat.model.BipedEntityModelCompat;
import mca.util.compat.model.Dilation;
import mca.util.compat.model.ModelData;
import mca.util.compat.model.ModelPartCompat;
import mca.util.compat.model.ModelPartBuilder;
import mca.util.compat.model.ModelPartData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;

import static mca.client.model.ModelTransformSet.Op.*;
import static mca.util.compat.model.EntityModelPartNames.*;

public class GrimReaperEntityModel<T extends GrimReaperEntity> extends BipedEntityModel<T> {
    private static final Map<ReaperAttackState, ModelTransformSet> POSES = ImmutableMap.of(
        ReaperAttackState.PRE, new ModelTransformSet.Builder()
            .rotate(HEAD, -15.6F, 40.4F, 0)
            .rotate(BODY, 0, -13, 0)
            .rotate(LEFT_ARM, -130, -112, 7.8F)
            .rotate(RIGHT_ARM, -36.5F, 122.6F, 0)
            .rotate(LEFT_LEG, 18, -13, 0)
            .rotate(RIGHT_LEG, 13, -13, 0)
            .rotate("scythe_handle", 0, 0, 90)
            .build(),
        ReaperAttackState.POST, new ModelTransformSet.Builder()
            .rotate(HEAD, 44.3F, 41.7F, 0)
            .rotate(BODY, 34, 34, 0)
            .rotate(LEFT_ARM, -44, 62, 7.8F)
            .with(RIGHT_ARM, -5, 1.7F, 3.3F, -36.5F, 122.6F, 0)
            .with(LEFT_LEG, 5.4F, 9.8F, 4.6F, 28.7F, 39, -2.6F)
            .with(RIGHT_LEG, 2, 10, 6.6F, 31.3F, 34, -5.2F)
            .with("scythe_handle", -10, 10, 0, 0, -10, 90)
            .build(),
        ReaperAttackState.BLOCK, new ModelTransformSet.Builder()
            .rotate(HEAD, 7.8F, 0, 0)
            .with(BODY, 0, 0, 1, -5.2F, 5.2F, 0)
            .rotate(LEFT_ARM, -86F, 23.5F, 7.8F)
            .rotate(RIGHT_ARM, -70, 0, 107)
            .rotate(LEFT_LEG, -7.8F, 2.6F, 0)
            .rotate(RIGHT_LEG, -7.8F, 5.2F, 0)
            .rotate("scythe_handle", 120, 88, 0)
            .build(),
        ReaperAttackState.REST, new ModelTransformSet.Builder()
            .rotate(HEAD, 62.6F, 0, 1.8F)
            .rotate(BODY, 0, 5.2F, 0)
            .rotate(LEFT_ARM, 0, 0, -20, ADD)
            .rotate(RIGHT_ARM, 0, 0, 20, ADD)
            .rotate(LEFT_LEG, 2.6F, 2.6F, 0)
            .rotate(RIGHT_LEG, 2.6F, 5.2F, 0)
            .with("scythe_handle", 0, 10, 0, 90, -20, 90, KEEP, KEEP)
            .build());

    private final ModelPartCompat idleCowl;
    private final ModelPartCompat scythe;

    public ReaperAttackState reaperState = ReaperAttackState.IDLE;

    private final ModelTransform scytheTransform;

    public GrimReaperEntityModel(ModelPartCompat tree) {
        super(0, 0, 64, 64);
        idleCowl = tree.getChild("idle_cowl");
        scythe = tree.getChild(LEFT_ARM).getChild("scythe_handle");
        scytheTransform = scythe.getTransform();

        this.leftArm.addChild(scythe);
    }

    public static ModelData getModelData(Dilation dilation) {
        ModelData modelData = BipedEntityModelCompat.getModelData(dilation, 0);
        ModelPartData data = modelData.getRoot();

        data.getChild(LEFT_ARM)
            .addChild("scythe_handle",
                ModelPartBuilder.create().uv(36, 32).cuboid(0, -26, 0, 1, 31, 1, dilation)
                                         .uv(0, 32).cuboid(0.5F, -26, 0.5F, 16, 16, 0, dilation),
                                         ModelTransformSet.Builder.createTransform(0, 10, 0, 90, -20, 90)
            );
        data.addChild("idle_cowl",
                ModelPartBuilder.create().uv(16, 16).cuboid(-4, -8, -4, 8, 8, 8, dilation.add(0.5F)),
                ModelTransform.NONE
        );

        return modelData;
    }

    @Override
    public void setAngles(T entity, float f, float g, float h, float i, float j) {
        super.setAngles(entity, f, g, h, i, j);

        body.setPivot(0, 0, 0);
        ModelPartCompat.setAngles(body, 0, 0, 0);

        leftLeg.setPivot(1.9F, 12, 0);
        ModelPartCompat.setAngles(leftLeg, 0, 0, 0);
        rightLeg.setPivot(-1.9F, 12, 0);
        ModelPartCompat.setAngles(rightLeg, 0, 0, 0);

        scythe.setTransform(scytheTransform);

        reaperState = entity.getAttackState();
        ModelTransformSet set = POSES.get(reaperState);

        if (set != null) {
            set.get(HEAD).applyTo(head);
            set.get(BODY).applyTo(body);
            set.get(LEFT_ARM).applyTo(leftArm);
            set.get(RIGHT_ARM).applyTo(rightArm);
            set.get(LEFT_LEG).applyTo(leftLeg);
            set.get(RIGHT_LEG).applyTo(rightLeg);
            set.get("scythe_handle").applyTo(scythe);
        }

        getHat().copyTransform(head);
    }

    private ModelPart getHat() {
        return reaperState == ReaperAttackState.IDLE ? idleCowl : hat;
    }

    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of(head, getHat());
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg);
    }
}