package mca.client.model;

import mca.entity.EntityGrimReaper;
import mca.enums.EnumReaperAttackState;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class ModelGrimReaper extends ModelBiped {
    private ModelRenderer head;
    private ModelRenderer rightArm;
    private ModelRenderer leftLeg;
    private ModelRenderer cowl;
    private ModelRenderer chest;
    private ModelRenderer leftArm;
    private ModelRenderer rightLeg;

    private ModelRenderer cowlPreAttack;
    private ModelRenderer rightArmPreAttack;
    private ModelRenderer leftLegPreAttack;
    private ModelRenderer headPreAttack;
    private ModelRenderer chestPreAttack;
    private ModelRenderer leftArmPreAttack;
    private ModelRenderer rightLegPreAttack;
    private ModelRenderer scytheHandlePreAttack;
    private ModelRenderer scytheHeadPreAttack;

    private ModelRenderer cowlPostAttack;
    private ModelRenderer rightArmPostAttack;
    private ModelRenderer leftLegPostAttack;
    private ModelRenderer headPostAttack;
    private ModelRenderer chestPostAttack;
    private ModelRenderer leftArmPostAttack;
    private ModelRenderer rightLegPostAttack;
    private ModelRenderer scytheHandlePostAttack;
    private ModelRenderer scytheHeadPostAttack;

    private ModelRenderer cowlBlock;
    private ModelRenderer rightArmBlock;
    private ModelRenderer leftLegBlock;
    private ModelRenderer headBlock;
    private ModelRenderer chestBlock;
    private ModelRenderer leftArmBlock;
    private ModelRenderer rightLegBlock;
    private ModelRenderer scytheHandleBlock;
    private ModelRenderer scytheHeadBlock;

    private ModelRenderer cowlRest;
    private ModelRenderer rightArmRest;
    private ModelRenderer leftLegRest;
    private ModelRenderer chestRest;
    private ModelRenderer leftArmRest;
    private ModelRenderer rightLegRest;
    private ModelRenderer scytheHandleRest;
    private ModelRenderer scytheHeadRest;
    private ModelRenderer headRest;

    public ModelGrimReaper() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.leftLeg = new ModelRenderer(this, 0, 16);
        this.leftLeg.mirror = true;
        this.leftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.leftArm = new ModelRenderer(this, 40, 16);
        this.leftArm.mirror = true;
        this.leftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.rightLeg = new ModelRenderer(this, 0, 16);
        this.rightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.chest = new ModelRenderer(this, 16, 16);
        this.chest.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.chest.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.cowl = new ModelRenderer(this, 0, 0);
        this.cowl.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cowl.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.head = new ModelRenderer(this, 32, 0);
        this.head.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.rightArm = new ModelRenderer(this, 40, 16);
        this.rightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        this.leftArmPreAttack = new ModelRenderer(this, 40, 16);
        this.leftArmPreAttack.mirror = true;
        this.leftArmPreAttack.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.leftArmPreAttack.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftArmPreAttack, -2.276432943376204F, -1.9577358219620393F, 0.136659280431156F);
        this.cowlPreAttack = new ModelRenderer(this, 32, 0);
        this.cowlPreAttack.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cowlPreAttack.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.setRotateAngle(cowlPreAttack, -0.27314402793711257F, 0.18203784098300857F, 0.0F);
        this.rightLegPreAttack = new ModelRenderer(this, 0, 16);
        this.rightLegPreAttack.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.rightLegPreAttack.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightLegPreAttack, 0.22759093446006054F, -0.22759093446006054F, 0.0F);
        this.leftLegPreAttack = new ModelRenderer(this, 0, 16);
        this.leftLegPreAttack.mirror = true;
        this.leftLegPreAttack.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.leftLegPreAttack.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftLegPreAttack, 0.31869712141416456F, -0.22759093446006054F, 0.0F);
        this.chestPreAttack = new ModelRenderer(this, 16, 16);
        this.chestPreAttack.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.chestPreAttack.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.setRotateAngle(chestPreAttack, 0.0F, -0.22759093446006054F, 0.0F);
        this.scytheHandlePreAttack = new ModelRenderer(this, 36, 32);
        this.scytheHandlePreAttack.setRotationPoint(7.0F, -12.4F, 17.2F);
        this.scytheHandlePreAttack.addBox(0.0F, 0.0F, 0.0F, 1, 31, 1, 0.0F);
        this.setRotateAngle(scytheHandlePreAttack, -1.0471975511965976F, -0.36425021489121656F, 0.0F);
        this.rightArmPreAttack = new ModelRenderer(this, 40, 16);
        this.rightArmPreAttack.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.rightArmPreAttack.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightArmPreAttack, -0.6373942428283291F, 2.1399481958702475F, 0.0F);
        this.headPreAttack = new ModelRenderer(this, 0, 0);
        this.headPreAttack.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.headPreAttack.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.setRotateAngle(headPreAttack, -0.27314402793711257F, 0.18203784098300857F, 0.0F);
        this.scytheHeadPreAttack = new ModelRenderer(this, 0, 32);
        this.scytheHeadPreAttack.setRotationPoint(7.8F, -11.8F, 17.5F);
        this.scytheHeadPreAttack.addBox(0.0F, 0.0F, 0.0F, 16, 16, 0, 0.0F);
        this.setRotateAngle(scytheHeadPreAttack, -1.0471975511965976F, -0.36425021489121656F, 0.0F);

        this.chestPostAttack = new ModelRenderer(this, 16, 16);
        this.chestPostAttack.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.chestPostAttack.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.setRotateAngle(chestPostAttack, 0.5918411493512771F, 0.5918411493512771F, 0.0F);
        this.leftArmPostAttack = new ModelRenderer(this, 40, 16);
        this.leftArmPostAttack.mirror = true;
        this.leftArmPostAttack.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.leftArmPostAttack.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftArmPostAttack, -0.7740535232594852F, 1.0927506446736497F, 0.136659280431156F);
        this.cowlPostAttack = new ModelRenderer(this, 32, 0);
        this.cowlPostAttack.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cowlPostAttack.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.setRotateAngle(cowlPostAttack, 0.7740535232594852F, 0.7285004297824331F, 0.0F);
        this.scytheHeadPostAttack = new ModelRenderer(this, 0, 32);
        this.scytheHeadPostAttack.setRotationPoint(0.8F, 8.9F, -24.0F);
        this.scytheHeadPostAttack.addBox(0.0F, 0.0F, 0.0F, 16, 16, 0, 0.0F);
        this.setRotateAngle(scytheHeadPostAttack, -1.6235052702051254F, 2.9543188248508017F, -0.27314402793711257F);
        this.scytheHandlePostAttack = new ModelRenderer(this, 37, 32);
        this.scytheHandlePostAttack.setRotationPoint(-4.3F, 9.0F, 6.7F);
        this.scytheHandlePostAttack.addBox(0.0F, 0.0F, 0.0F, 1, 31, 1, 0.0F);
        this.setRotateAngle(scytheHandlePostAttack, 1.5025539530419183F, 2.9595548126067843F, -0.36425021489121656F);
        this.rightLegPostAttack = new ModelRenderer(this, 0, 16);
        this.rightLegPostAttack.setRotationPoint(2.0F, 10.0F, 6.6F);
        this.rightLegPostAttack.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightLegPostAttack, 0.5462880558742251F, 0.5918411493512771F, -0.091106186954104F);
        this.headPostAttack = new ModelRenderer(this, 0, 0);
        this.headPostAttack.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.headPostAttack.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.setRotateAngle(headPostAttack, 0.7740535232594852F, 0.7285004297824331F, 0.0F);
        this.leftLegPostAttack = new ModelRenderer(this, 0, 16);
        this.leftLegPostAttack.mirror = true;
        this.leftLegPostAttack.setRotationPoint(5.4F, 9.8F, 4.6F);
        this.leftLegPostAttack.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftLegPostAttack, 0.5009094953223726F, 0.6829473363053812F, -0.045553093477052F);
        this.rightArmPostAttack = new ModelRenderer(this, 40, 16);
        this.rightArmPostAttack.setRotationPoint(-5.0F, 1.7F, 3.3F);
        this.rightArmPostAttack.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightArmPostAttack, -1.593485607070823F, 2.5497515042385164F, 0.0F);

        this.cowlBlock = new ModelRenderer(this, 32, 0);
        this.cowlBlock.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cowlBlock.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.setRotateAngle(cowlBlock, 0.091106186954104F, 0.0F, 0.0F);
        this.leftLegBlock = new ModelRenderer(this, 0, 16);
        this.leftLegBlock.mirror = true;
        this.leftLegBlock.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.leftLegBlock.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftLegBlock, -0.136659280431156F, 0.045553093477052F, 0.0F);
        this.headBlock = new ModelRenderer(this, 0, 0);
        this.headBlock.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.headBlock.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.setRotateAngle(headBlock, 0.136659280431156F, 0.0F, 0.0F);
        this.rightArmBlock = new ModelRenderer(this, 40, 16);
        this.rightArmBlock.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.rightArmBlock.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightArmBlock, -1.2292353921796064F, 0.0F, 1.8668041679331349F);
        this.scytheHandleBlock = new ModelRenderer(this, 36, 32);
        this.scytheHandleBlock.setRotationPoint(-18.5F, -3.7F, -10.1F);
        this.scytheHandleBlock.addBox(0.0F, 0.0F, 0.0F, 1, 31, 1, 0.0F);
        this.setRotateAngle(scytheHandleBlock, 1.2292353921796064F, 1.5481070465189704F, 0.0F);
        this.rightLegBlock = new ModelRenderer(this, 0, 16);
        this.rightLegBlock.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.rightLegBlock.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightLegBlock, -0.136659280431156F, 0.091106186954104F, 0.0F);
        this.chestBlock = new ModelRenderer(this, 16, 16);
        this.chestBlock.setRotationPoint(0.0F, 0.0F, 1.0F);
        this.chestBlock.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.setRotateAngle(chestBlock, -0.091106186954104F, 0.091106186954104F, 0.0F);
        this.leftArmBlock = new ModelRenderer(this, 40, 16);
        this.leftArmBlock.mirror = true;
        this.leftArmBlock.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.leftArmBlock.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftArmBlock, -1.5025539530419183F, 0.40980330836826856F, 0.136659280431156F);
        this.scytheHeadBlock = new ModelRenderer(this, 0, 32);
        this.scytheHeadBlock.setRotationPoint(-18.5F, -3.7F, -10.1F);
        this.scytheHeadBlock.addBox(0.0F, 0.0F, 0.5F, 16, 16, 0, 0.0F);
        this.setRotateAngle(scytheHeadBlock, 1.2292353921796064F, 1.5481070465189704F, 0.0F);

        this.rightArmRest = new ModelRenderer(this, 40, 16);
        this.rightArmRest.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.rightArmRest.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightArmRest, 0.091106186954104F, -0.045553093477052F, 0.4553564018453205F);
        this.rightLegRest = new ModelRenderer(this, 0, 16);
        this.rightLegRest.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.rightLegRest.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightLegRest, 0.045553093477052F, 0.091106186954104F, 0.0F);
        this.leftArmRest = new ModelRenderer(this, 40, 16);
        this.leftArmRest.mirror = true;
        this.leftArmRest.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.leftArmRest.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftArmRest, -1.6845917940249266F, -1.5481070465189704F, 0.9560913642424937F);
        this.chestRest = new ModelRenderer(this, 16, 16);
        this.chestRest.setRotationPoint(0.0F, 0.0F, 0.1F);
        this.chestRest.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.setRotateAngle(chestRest, 0.0F, 0.091106186954104F, 0.0F);
        this.leftLegRest = new ModelRenderer(this, 0, 16);
        this.leftLegRest.mirror = true;
        this.leftLegRest.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.leftLegRest.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftLegRest, 0.045553093477052F, 0.045553093477052F, 0.0F);
        this.headRest = new ModelRenderer(this, 0, 0);
        this.headRest.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.headRest.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.setRotateAngle(headRest, 1.0927506446736497F, 0.0F, 0.03159045946109736F);
        this.scytheHeadRest = new ModelRenderer(this, 0, 32);
        this.scytheHeadRest.setRotationPoint(-0.1F, -7.3F, -11.7F);
        this.scytheHeadRest.addBox(0.0F, 0.0F, 0.5F, 16, 16, 0, 0.0F);
        this.setRotateAngle(scytheHeadRest, 0.091106186954104F, 1.2747884856566583F, 0.091106186954104F);
        this.cowlRest = new ModelRenderer(this, 32, 0);
        this.cowlRest.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cowlRest.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.setRotateAngle(cowlRest, 1.0471975511965976F, 0.0F, 0.0F);
        this.scytheHandleRest = new ModelRenderer(this, 36, 32);
        this.scytheHandleRest.setRotationPoint(-1.0F, 7.0F, -10.1F);
        this.scytheHandleRest.addBox(0.5F, -15.5F, 0.5F, 1, 31, 1, 0.0F);
        this.setRotateAngle(scytheHandleRest, 0.091106186954104F, 1.2747884856566583F, 0.091106186954104F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        EntityGrimReaper reaper = (EntityGrimReaper) entity;

        if (reaper.getAttackState() == EnumReaperAttackState.PRE) {
            this.leftLegPreAttack.render(f5);
            this.leftArmPreAttack.render(f5);
            this.rightLegPreAttack.render(f5);
            this.chestPreAttack.render(f5);
            this.cowlPreAttack.render(f5);
            this.headPreAttack.render(f5);
            this.rightArmPreAttack.render(f5);
            this.scytheHandlePreAttack.render(f5);
            this.scytheHeadPreAttack.render(f5);
        } else if (reaper.getAttackState() == EnumReaperAttackState.POST) {
            this.leftLegPostAttack.render(f5);
            this.leftArmPostAttack.render(f5);
            this.rightLegPostAttack.render(f5);
            this.chestPostAttack.render(f5);
            this.cowlPostAttack.render(f5);
            this.headPostAttack.render(f5);
            this.rightArmPostAttack.render(f5);
            this.scytheHandlePostAttack.render(f5);
            this.scytheHeadPostAttack.render(f5);
        } else if (reaper.getAttackState() == EnumReaperAttackState.BLOCK) {
            this.cowlBlock.render(f5);
            this.leftLegBlock.render(f5);
            this.headBlock.render(f5);
            this.rightArmBlock.render(f5);
            this.scytheHandleBlock.render(f5);
            this.rightLegBlock.render(f5);
            this.chestBlock.render(f5);
            this.leftArmBlock.render(f5);
            this.scytheHeadBlock.render(f5);
        } else if (reaper.getAttackState() == EnumReaperAttackState.REST) {

            GL11.glPushMatrix();
            {
                double amt = Math.cos(reaper.getFloatingTicks()) / 4;
                GL11.glTranslated(0.0D, amt, 0.0D);

                this.rightArmRest.render(f5);
                this.rightLegRest.render(f5);
                this.leftArmRest.render(f5);
                this.chestRest.render(f5);
                this.leftLegRest.render(f5);
                this.cowlRest.render(f5);

                GL11.glPushMatrix();
                {
                    double amt2 = Math.cos(reaper.getFloatingTicks()) / 8;
                    GL11.glTranslated(0.0D, amt2, 0.0D);

                    this.scytheHeadRest.render(f5);
                    this.scytheHandleRest.render(f5);
                }
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();
        } else {
            rightLeg.rotateAngleX = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * f1, 0.0F, 1.1F);
            leftLeg.rotateAngleX = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * f1, 0.0F, 1.1F);
            rightLeg.rotateAngleX = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * f1, 0.0F, 1.1F);
            leftLeg.rotateAngleX = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * f1, 0.0F, 1.1F);

            rightLeg.rotateAngleY = 0.0F;
            leftLeg.rotateAngleY = 0.0F;

            this.leftLeg.render(f5);
            this.leftArm.render(f5);
            this.rightLeg.render(f5);
            this.chest.render(f5);
            this.cowl.render(f5);
            this.head.render(f5);
            this.rightArm.render(f5);
        }
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}