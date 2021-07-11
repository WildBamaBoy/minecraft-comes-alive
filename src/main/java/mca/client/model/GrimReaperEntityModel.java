package mca.client.model;

import mca.entity.GrimReaperEntity;
import mca.enums.ReaperAttackState;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class GrimReaperEntityModel<T extends GrimReaperEntity> extends BipedEntityModel<T> {
    private final ModelPart idleCowl;
    private final ModelPart activeCowl;

    private final ModelPart scytheHandle;
    private final ModelPart restingScytheHandle;

    private final ModelPart scytheHead;
    private final ModelPart attackingScytheHead;

    public ReaperAttackState reaperState = ReaperAttackState.IDLE;

    public GrimReaperEntityModel(ModelPart tree) {
        super(tree);
        idleCowl = tree.getChild("idle_cowl");
        activeCowl = tree.getChild("active_cowl");

        scytheHead = tree.getChild("scythe_head");
        attackingScytheHead = tree.getChild("attacking_scythe_head");

        scytheHandle = tree.getChild("scythe_handle");
        restingScytheHandle = tree.getChild("resting_scythe_handle");
    }

    public static ModelData getModelData(Dilation dilation) {
        ModelData modelData = BipedEntityModel.getModelData(dilation, 0);
        ModelPartData data = modelData.getRoot();

        data.addChild(EntityModelPartNames.HEAD,
                ModelPartBuilder.create().uv(32, 0).cuboid(-4, -8, -4, 8, 8, 8, dilation.add(0.5F)),
                ModelTransform.pivot(0, 0, 0)
        );
        data.addChild(EntityModelPartNames.LEFT_LEG,
                ModelPartBuilder.create().uv(0, 16).cuboid(-2, 0, -2, 4, 12, 4, dilation).mirrored(),
                ModelTransform.pivot(1.9F, 12, 0)
        );
        data.addChild(EntityModelPartNames.RIGHT_LEG,
                ModelPartBuilder.create().uv(0, 16).cuboid(-2, 0, -2, 4, 12, 4, dilation),
                ModelTransform.pivot(-1.9F, 12, 0)
        );
        data.addChild(EntityModelPartNames.LEFT_ARM,
                ModelPartBuilder.create().uv(40, 16).cuboid(-1, -2, -2, 4, 12, 4, dilation).mirrored(),
                ModelTransform.pivot(5, 2, 0)
        );
        data.addChild(EntityModelPartNames.RIGHT_ARM,
                ModelPartBuilder.create().uv(40, 16).cuboid(-3, -2, -2, 4, 12, 4, dilation),
                ModelTransform.pivot(-5, 2, 0)
        );
        data.addChild(EntityModelPartNames.BODY,
                ModelPartBuilder.create().uv(16, 16).cuboid(-4, 0, -2, 8, 12, 4, dilation),
                ModelTransform.pivot(0, 0, 0)
        );

        data.addChild("idle_cowl",
                ModelPartBuilder.create().uv(16, 16).cuboid(-4, -8, -4, 8, 8, 8, dilation),
                ModelTransform.pivot(0, 0, 0)
        );
        data.addChild("active_cowl",
                ModelPartBuilder.create().uv(32, 0).cuboid(-4, -8, -4, 8, 8, 8, dilation),
                ModelTransform.pivot(0, 0, 0)
        );
        data.addChild("scythe_handle",
                ModelPartBuilder.create().uv(36, 32).cuboid(0, 0, 0, 1, 31, 1, dilation),
                ModelTransform.pivot(0, 0, 0)
        );
        data.addChild("resting_scythe_handle",
                ModelPartBuilder.create().uv(36, 32).cuboid(0.5F, -15.5F, 0.5F, 1, 31, 1, dilation),
                ModelTransform.pivot(0, 0, 0)
        );

        data.addChild("scythe_head",
                ModelPartBuilder.create().uv(0, 32).cuboid(0, 0.5F, 0, 16, 16, 0, dilation),
                ModelTransform.pivot(0, 0, 0)
        );
        data.addChild("attacking_scythe_head",
                ModelPartBuilder.create().uv(0, 32).cuboid(0, 0, 0, 16, 16, 0, dilation),
                ModelTransform.pivot(0, 0, 0)
        );

        return modelData;
     }

    @Override
    public void setAngles(T entity, float f, float g, float h, float i, float j) {
        super.setAngles(entity, f, g, h, i, j);
        reaperState = entity.getAttackState();

        head.setAngles(0, 0, 0);

        body.setPivot(0, 0, 0);
        body.setAngles(0, 0, 0);

        leftArm.setAngles(0, 0, 0);
        rightArm.setAngles(0, 0, 0);
        rightArm.setPivot(-5, 2, 0);

        leftLeg.setPivot(1.9F, 12, 0);
        leftLeg.setAngles(0, 0, 0);
        rightLeg.setPivot(-1.9F, 12, 0);
        rightLeg.setAngles(0, 0, 0);

        scytheHandle.setPivot(0, 0, 0);
        scytheHead.setPivot(0, 0, 0);

        if (reaperState == ReaperAttackState.PRE) {
            head.setAngles(-0.27314402793711257F, 0.18203784098300857F, 0.0F);
            activeCowl.setAngles(-0.27314402793711257F, 0.18203784098300857F, 0.0F);
            body.setAngles(0.0F, -0.22759093446006054F, 0.0F);

            leftArm.setAngles(-2.276432943376204F, -1.9577358219620393F, 0.136659280431156F);
            rightArm.setAngles(-0.6373942428283291F, 2.1399481958702475F, 0);
            leftLeg.setAngles(0.31869712141416456F, -0.22759093446006054F, 0.0F);
            rightLeg.setAngles(0.22759093446006054F, -0.22759093446006054F, 0.0F);

            attackingScytheHead.setPivot(7.8F, -11.8F, 17.5F);
            attackingScytheHead.setAngles(-1.0471975511965976F, -0.36425021489121656F, 0.0F);
            scytheHandle.setPivot(7.0F, -12.4F, 17.2F);
            scytheHandle.setAngles(-1.0471975511965976F, -0.36425021489121656F, 0.0F);
        } else if (reaperState == ReaperAttackState.POST) {
            head.setAngles(0.7740535232594852F, 0.7285004297824331F, 0.0F);
            activeCowl.setAngles(0.7740535232594852F, 0.7285004297824331F, 0.0F);

            body.setAngles(0.5918411493512771F, 0.5918411493512771F, 0.0F);
            leftArm.setAngles(-0.7740535232594852F, 1.0927506446736497F, 0.136659280431156F);

            rightArm.setPivot(-5, 1.7F, 3.3F);
            rightArm.setAngles(-1.593485607070823F, 2.5497515042385164F, 0);

            leftLeg.setPivot(5.4F, 9.8F, 4.6F);
            leftLeg.setAngles(0.5009094953223726F, 0.6829473363053812F, -0.045553093477052F);

            rightLeg.setPivot(2, 10, 6.6F);
            rightLeg.setAngles(0.5462880558742251F, 0.5918411493512771F, -0.091106186954104F);

            attackingScytheHead.setPivot(0.8F, 8.9F, -24.0F);
            attackingScytheHead.setAngles(-1.6235052702051254F, 2.9543188248508017F, -0.27314402793711257F);
            scytheHandle.setPivot(-4.3F, 9.0F, 6.7F);
            scytheHandle.setAngles(1.5025539530419183F, 2.9595548126067843F, -0.36425021489121656F);
        } else if (reaperState == ReaperAttackState.BLOCK) {
            head.setAngles(0.136659280431156F, 0.0F, 0.0F);
            activeCowl.setAngles(0.091106186954104F, 0.0F, 0.0F);

            body.setPivot(0, 0, 1);
            body.setAngles(-0.091106186954104F, 0.091106186954104F, 0.0F);

            leftArm.setAngles(-1.5025539530419183F, 0.40980330836826856F, 0.136659280431156F);
            rightArm.setAngles(-1.2292353921796064F, 0, 1.8668041679331349F);

            leftLeg.setAngles(-0.136659280431156F, 0.045553093477052F, 0.0F);
            rightLeg.setAngles(-0.136659280431156F, 0.091106186954104F, 0.0F);

            scytheHead.setPivot(-18.5F, -3.7F, -10.1F);
            scytheHead.setAngles(1.2292353921796064F, 1.5481070465189704F, 0.0F);
            scytheHandle.setPivot(-18.5F, -3.7F, -10.1F);
            scytheHandle.setAngles(1.2292353921796064F, 1.5481070465189704F, 0.0F);
        } else if (reaperState == ReaperAttackState.REST) {
            head.setAngles(1.0927506446736497F, 0.0F, 0.03159045946109736F);
            activeCowl.setAngles(1.0471975511965976F, 0.0F, 0.0F);

            body.setPivot(0, 0, 0.1F);
            body.setAngles(0.0F, 0.091106186954104F, 0.0F);

            leftArm.setAngles(-1.6845917940249266F, -1.5481070465189704F, 0.9560913642424937F);
            rightArm.setAngles(0.091106186954104F, -0.045553093477052F, 0.4553564018453205F);
            leftLeg.setAngles(0.045553093477052F, 0.045553093477052F, 0.0F);
            rightLeg.setAngles(0.045553093477052F, 0.091106186954104F, 0.0F);

            scytheHead.setPivot(-0.1F, -7.3F, -11.7F);
            scytheHead.setAngles(0.091106186954104F, 1.2747884856566583F, 0.091106186954104F);
            restingScytheHandle.setPivot(-1.0F, 7.0F, -10.1F);
            restingScytheHandle.setAngles(0.091106186954104F, 1.2747884856566583F, 0.091106186954104F);
            scytheHead.setPivot(-0.6F, 0, 0.65F);
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        body.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        head.render(matrices, vertices, light, overlay, red, green, blue, alpha);

        if (reaperState == ReaperAttackState.IDLE) {
            rightLeg.pitch = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * red, 0, 1.1F);
            leftLeg.pitch = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * red, 0, 1.1F);
            rightLeg.pitch = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * red, 0, 1.1F);
            leftLeg.pitch = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * red, 0, 1.1F);

            rightLeg.yaw = 0.0F;
            leftLeg.yaw = 0.0F;

            idleCowl.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        } else {
            activeCowl.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }

        if (reaperState == ReaperAttackState.REST) {
            matrices.push();
            matrices.translate(-0.6F, 0, 0.65F);
            restingScytheHandle.render(matrices, vertices, light, overlay, red, green, blue, alpha);
            scytheHead.render(matrices, vertices, light, overlay, red, green, blue, alpha);
            matrices.pop();
        } else {
            scytheHandle.render(matrices, vertices, light, overlay, red, green, blue, alpha);

            if (reaperState == ReaperAttackState.PRE || reaperState == ReaperAttackState.POST) {
                attackingScytheHead.render(matrices, vertices, light, overlay, red, green, blue, alpha);
            } else {
                scytheHead.render(matrices, vertices, light, overlay, red, green, blue, alpha);
            }
        }

        leftArm.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        rightArm.render(matrices, vertices, light, overlay, red, green, blue, alpha);

        leftLeg.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        rightLeg.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}