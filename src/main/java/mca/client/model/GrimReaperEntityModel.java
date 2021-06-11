package mca.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mca.entity.GrimReaperEntity;
import mca.enums.ReaperAttackState;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GrimReaperEntityModel<T extends GrimReaperEntity> extends BipedModel<T> {
    private final ModelRenderer head;
    private final ModelRenderer rightArm;
    private final ModelRenderer leftLeg;
    private final ModelRenderer cowl;
    private final ModelRenderer chest;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightLeg;

    private final ModelRenderer cowlPreAttack;
    private final ModelRenderer rightArmPreAttack;
    private final ModelRenderer leftLegPreAttack;
    private final ModelRenderer headPreAttack;
    private final ModelRenderer chestPreAttack;
    private final ModelRenderer leftArmPreAttack;
    private final ModelRenderer rightLegPreAttack;
    private final ModelRenderer scytheHandlePreAttack;
    private final ModelRenderer scytheHeadPreAttack;

    private final ModelRenderer cowlPostAttack;
    private final ModelRenderer rightArmPostAttack;
    private final ModelRenderer leftLegPostAttack;
    private final ModelRenderer headPostAttack;
    private final ModelRenderer chestPostAttack;
    private final ModelRenderer leftArmPostAttack;
    private final ModelRenderer rightLegPostAttack;
    private final ModelRenderer scytheHandlePostAttack;
    private final ModelRenderer scytheHeadPostAttack;

    private final ModelRenderer cowlBlock;
    private final ModelRenderer rightArmBlock;
    private final ModelRenderer leftLegBlock;
    private final ModelRenderer headBlock;
    private final ModelRenderer chestBlock;
    private final ModelRenderer leftArmBlock;
    private final ModelRenderer rightLegBlock;
    private final ModelRenderer scytheHandleBlock;
    private final ModelRenderer scytheHeadBlock;

    private final ModelRenderer cowlRest;
    private final ModelRenderer rightArmRest;
    private final ModelRenderer leftLegRest;
    private final ModelRenderer chestRest;
    private final ModelRenderer leftArmRest;
    private final ModelRenderer rightLegRest;
    private final ModelRenderer scytheHandleRest;
    private final ModelRenderer scytheHeadRest;
    private final ModelRenderer headRest;

    public ReaperAttackState reaperState = ReaperAttackState.IDLE;

    public GrimReaperEntityModel() {
        super(0.0f);

        this.texWidth = 64;
        this.texHeight = 64;

        this.leftLeg = new ModelRenderer(this, 0, 16);
        this.leftLeg.mirror = true;
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.leftArm = new ModelRenderer(this, 40, 16);
        this.leftArm.mirror = true;
        this.leftArm.setPos(5.0F, 2.0F, 0.0F);
        this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.rightLeg = new ModelRenderer(this, 0, 16);
        this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.chest = new ModelRenderer(this, 16, 16);
        this.chest.setPos(0.0F, 0.0F, 0.0F);
        this.chest.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.cowl = new ModelRenderer(this, 0, 0);
        this.cowl.setPos(0.0F, 0.0F, 0.0F);
        this.cowl.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.head = new ModelRenderer(this, 32, 0);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.rightArm = new ModelRenderer(this, 40, 16);
        this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
        this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        this.leftArmPreAttack = new ModelRenderer(this, 40, 16);
        this.leftArmPreAttack.mirror = true;
        this.leftArmPreAttack.setPos(5.0F, 2.0F, 0.0F);
        this.leftArmPreAttack.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftArmPreAttack, -2.276432943376204F, -1.9577358219620393F, 0.136659280431156F);
        this.cowlPreAttack = new ModelRenderer(this, 32, 0);
        this.cowlPreAttack.setPos(0.0F, 0.0F, 0.0F);
        this.cowlPreAttack.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.setRotateAngle(cowlPreAttack, -0.27314402793711257F, 0.18203784098300857F, 0.0F);
        this.rightLegPreAttack = new ModelRenderer(this, 0, 16);
        this.rightLegPreAttack.setPos(-1.9F, 12.0F, 0.0F);
        this.rightLegPreAttack.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightLegPreAttack, 0.22759093446006054F, -0.22759093446006054F, 0.0F);
        this.leftLegPreAttack = new ModelRenderer(this, 0, 16);
        this.leftLegPreAttack.mirror = true;
        this.leftLegPreAttack.setPos(1.9F, 12.0F, 0.0F);
        this.leftLegPreAttack.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftLegPreAttack, 0.31869712141416456F, -0.22759093446006054F, 0.0F);
        this.chestPreAttack = new ModelRenderer(this, 16, 16);
        this.chestPreAttack.setPos(0.0F, 0.0F, 0.0F);
        this.chestPreAttack.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.setRotateAngle(chestPreAttack, 0.0F, -0.22759093446006054F, 0.0F);
        this.scytheHandlePreAttack = new ModelRenderer(this, 36, 32);
        this.scytheHandlePreAttack.setPos(7.0F, -12.4F, 17.2F);
        this.scytheHandlePreAttack.addBox(0.0F, 0.0F, 0.0F, 1, 31, 1, 0.0F);
        this.setRotateAngle(scytheHandlePreAttack, -1.0471975511965976F, -0.36425021489121656F, 0.0F);
        this.rightArmPreAttack = new ModelRenderer(this, 40, 16);
        this.rightArmPreAttack.setPos(-5.0F, 2.0F, 0.0F);
        this.rightArmPreAttack.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightArmPreAttack, -0.6373942428283291F, 2.1399481958702475F, 0.0F);
        this.headPreAttack = new ModelRenderer(this, 0, 0);
        this.headPreAttack.setPos(0.0F, 0.0F, 0.0F);
        this.headPreAttack.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.setRotateAngle(headPreAttack, -0.27314402793711257F, 0.18203784098300857F, 0.0F);
        this.scytheHeadPreAttack = new ModelRenderer(this, 0, 32);
        this.scytheHeadPreAttack.setPos(7.8F, -11.8F, 17.5F);
        this.scytheHeadPreAttack.addBox(0.0F, 0.0F, 0.0F, 16, 16, 0, 0.0F);
        this.setRotateAngle(scytheHeadPreAttack, -1.0471975511965976F, -0.36425021489121656F, 0.0F);

        this.chestPostAttack = new ModelRenderer(this, 16, 16);
        this.chestPostAttack.setPos(0.0F, 0.0F, 0.0F);
        this.chestPostAttack.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.setRotateAngle(chestPostAttack, 0.5918411493512771F, 0.5918411493512771F, 0.0F);
        this.leftArmPostAttack = new ModelRenderer(this, 40, 16);
        this.leftArmPostAttack.mirror = true;
        this.leftArmPostAttack.setPos(5.0F, 2.0F, 0.0F);
        this.leftArmPostAttack.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftArmPostAttack, -0.7740535232594852F, 1.0927506446736497F, 0.136659280431156F);
        this.cowlPostAttack = new ModelRenderer(this, 32, 0);
        this.cowlPostAttack.setPos(0.0F, 0.0F, 0.0F);
        this.cowlPostAttack.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.setRotateAngle(cowlPostAttack, 0.7740535232594852F, 0.7285004297824331F, 0.0F);
        this.scytheHeadPostAttack = new ModelRenderer(this, 0, 32);
        this.scytheHeadPostAttack.setPos(0.8F, 8.9F, -24.0F);
        this.scytheHeadPostAttack.addBox(0.0F, 0.0F, 0.0F, 16, 16, 0, 0.0F);
        this.setRotateAngle(scytheHeadPostAttack, -1.6235052702051254F, 2.9543188248508017F, -0.27314402793711257F);
        this.scytheHandlePostAttack = new ModelRenderer(this, 37, 32);
        this.scytheHandlePostAttack.setPos(-4.3F, 9.0F, 6.7F);
        this.scytheHandlePostAttack.addBox(0.0F, 0.0F, 0.0F, 1, 31, 1, 0.0F);
        this.setRotateAngle(scytheHandlePostAttack, 1.5025539530419183F, 2.9595548126067843F, -0.36425021489121656F);
        this.rightLegPostAttack = new ModelRenderer(this, 0, 16);
        this.rightLegPostAttack.setPos(2.0F, 10.0F, 6.6F);
        this.rightLegPostAttack.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightLegPostAttack, 0.5462880558742251F, 0.5918411493512771F, -0.091106186954104F);
        this.headPostAttack = new ModelRenderer(this, 0, 0);
        this.headPostAttack.setPos(0.0F, 0.0F, 0.0F);
        this.headPostAttack.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.setRotateAngle(headPostAttack, 0.7740535232594852F, 0.7285004297824331F, 0.0F);
        this.leftLegPostAttack = new ModelRenderer(this, 0, 16);
        this.leftLegPostAttack.mirror = true;
        this.leftLegPostAttack.setPos(5.4F, 9.8F, 4.6F);
        this.leftLegPostAttack.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftLegPostAttack, 0.5009094953223726F, 0.6829473363053812F, -0.045553093477052F);
        this.rightArmPostAttack = new ModelRenderer(this, 40, 16);
        this.rightArmPostAttack.setPos(-5.0F, 1.7F, 3.3F);
        this.rightArmPostAttack.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightArmPostAttack, -1.593485607070823F, 2.5497515042385164F, 0.0F);

        this.cowlBlock = new ModelRenderer(this, 32, 0);
        this.cowlBlock.setPos(0.0F, 0.0F, 0.0F);
        this.cowlBlock.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.setRotateAngle(cowlBlock, 0.091106186954104F, 0.0F, 0.0F);
        this.leftLegBlock = new ModelRenderer(this, 0, 16);
        this.leftLegBlock.mirror = true;
        this.leftLegBlock.setPos(1.9F, 12.0F, 0.0F);
        this.leftLegBlock.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftLegBlock, -0.136659280431156F, 0.045553093477052F, 0.0F);
        this.headBlock = new ModelRenderer(this, 0, 0);
        this.headBlock.setPos(0.0F, 0.0F, 0.0F);
        this.headBlock.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.setRotateAngle(headBlock, 0.136659280431156F, 0.0F, 0.0F);
        this.rightArmBlock = new ModelRenderer(this, 40, 16);
        this.rightArmBlock.setPos(-5.0F, 2.0F, 0.0F);
        this.rightArmBlock.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightArmBlock, -1.2292353921796064F, 0.0F, 1.8668041679331349F);
        this.scytheHandleBlock = new ModelRenderer(this, 36, 32);
        this.scytheHandleBlock.setPos(-18.5F, -3.7F, -10.1F);
        this.scytheHandleBlock.addBox(0.0F, 0.0F, 0.0F, 1, 31, 1, 0.0F);
        this.setRotateAngle(scytheHandleBlock, 1.2292353921796064F, 1.5481070465189704F, 0.0F);
        this.rightLegBlock = new ModelRenderer(this, 0, 16);
        this.rightLegBlock.setPos(-1.9F, 12.0F, 0.0F);
        this.rightLegBlock.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightLegBlock, -0.136659280431156F, 0.091106186954104F, 0.0F);
        this.chestBlock = new ModelRenderer(this, 16, 16);
        this.chestBlock.setPos(0.0F, 0.0F, 1.0F);
        this.chestBlock.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.setRotateAngle(chestBlock, -0.091106186954104F, 0.091106186954104F, 0.0F);
        this.leftArmBlock = new ModelRenderer(this, 40, 16);
        this.leftArmBlock.mirror = true;
        this.leftArmBlock.setPos(5.0F, 2.0F, 0.0F);
        this.leftArmBlock.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftArmBlock, -1.5025539530419183F, 0.40980330836826856F, 0.136659280431156F);
        this.scytheHeadBlock = new ModelRenderer(this, 0, 32);
        this.scytheHeadBlock.setPos(-18.5F, -3.7F, -10.1F);
        this.scytheHeadBlock.addBox(0.0F, 0.0F, 0.5F, 16, 16, 0, 0.0F);
        this.setRotateAngle(scytheHeadBlock, 1.2292353921796064F, 1.5481070465189704F, 0.0F);

        this.rightArmRest = new ModelRenderer(this, 40, 16);
        this.rightArmRest.setPos(-5.0F, 2.0F, 0.0F);
        this.rightArmRest.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightArmRest, 0.091106186954104F, -0.045553093477052F, 0.4553564018453205F);
        this.rightLegRest = new ModelRenderer(this, 0, 16);
        this.rightLegRest.setPos(-1.9F, 12.0F, 0.0F);
        this.rightLegRest.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(rightLegRest, 0.045553093477052F, 0.091106186954104F, 0.0F);
        this.leftArmRest = new ModelRenderer(this, 40, 16);
        this.leftArmRest.mirror = true;
        this.leftArmRest.setPos(5.0F, 2.0F, 0.0F);
        this.leftArmRest.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftArmRest, -1.6845917940249266F, -1.5481070465189704F, 0.9560913642424937F);
        this.chestRest = new ModelRenderer(this, 16, 16);
        this.chestRest.setPos(0.0F, 0.0F, 0.1F);
        this.chestRest.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.setRotateAngle(chestRest, 0.0F, 0.091106186954104F, 0.0F);
        this.leftLegRest = new ModelRenderer(this, 0, 16);
        this.leftLegRest.mirror = true;
        this.leftLegRest.setPos(1.9F, 12.0F, 0.0F);
        this.leftLegRest.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(leftLegRest, 0.045553093477052F, 0.045553093477052F, 0.0F);
        this.headRest = new ModelRenderer(this, 0, 0);
        this.headRest.setPos(0.0F, 0.0F, 0.0F);
        this.headRest.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.setRotateAngle(headRest, 1.0927506446736497F, 0.0F, 0.03159045946109736F);
        this.scytheHeadRest = new ModelRenderer(this, 0, 32);
        this.scytheHeadRest.setPos(-0.1F, -7.3F, -11.7F);
        this.scytheHeadRest.addBox(0.0F, 0.0F, 0.5F, 16, 16, 0, 0.0F);
        this.setRotateAngle(scytheHeadRest, 0.091106186954104F, 1.2747884856566583F, 0.091106186954104F);
        this.cowlRest = new ModelRenderer(this, 32, 0);
        this.cowlRest.setPos(0.0F, 0.0F, 0.0F);
        this.cowlRest.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.setRotateAngle(cowlRest, 1.0471975511965976F, 0.0F, 0.0F);
        this.scytheHandleRest = new ModelRenderer(this, 36, 32);
        this.scytheHandleRest.setPos(-1.0F, 7.0F, -10.1F);
        this.scytheHandleRest.addBox(0.5F, -15.5F, 0.5F, 1, 31, 1, 0.0F);
        this.setRotateAngle(scytheHandleRest, 0.091106186954104F, 1.2747884856566583F, 0.091106186954104F);
    }

    @Override
    public void renderToBuffer(MatrixStack transform, IVertexBuilder p_225598_2_, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
        if (reaperState == ReaperAttackState.PRE) {
            this.leftLegPreAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.leftArmPreAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.rightLegPreAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.chestPreAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.cowlPreAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.headPreAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.rightArmPreAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.scytheHandlePreAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.scytheHeadPreAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
        } else if (reaperState == ReaperAttackState.POST) {
            this.leftLegPostAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.leftArmPostAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.rightLegPostAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.chestPostAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.cowlPostAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.headPostAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.rightArmPostAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.scytheHandlePostAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.scytheHeadPostAttack.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
        } else if (reaperState == ReaperAttackState.BLOCK) {
            this.cowlBlock.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.leftLegBlock.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.headBlock.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.rightArmBlock.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.scytheHandleBlock.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.rightLegBlock.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.chestBlock.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.leftArmBlock.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.scytheHeadBlock.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
        } else if (reaperState == ReaperAttackState.REST) {
            transform.pushPose();
            {
                this.rightArmRest.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
                this.rightLegRest.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
                this.leftArmRest.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
                this.chestRest.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
                this.leftLegRest.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
                this.cowlRest.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
                this.headRest.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);

                transform.pushPose();
                {
                    transform.translate(-0.6, 0.0, 0.65);
                    this.scytheHeadRest.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
                    this.scytheHandleRest.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
                }
                transform.popPose();
            }
            transform.popPose();
        } else {
            rightLeg.xRot = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * p_225598_5_, 0.0F, 1.1F);
            leftLeg.xRot = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * p_225598_5_, 0.0F, 1.1F);
            rightLeg.xRot = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * p_225598_5_, 0.0F, 1.1F);
            leftLeg.xRot = MathHelper.clamp(MathHelper.cos(100F * 0.6662F + 3.141593F) * 2.5F * p_225598_5_, 0.0F, 1.1F);

            rightLeg.yRot = 0.0F;
            leftLeg.yRot = 0.0F;

            this.leftLeg.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.leftArm.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.rightLeg.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.chest.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.cowl.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.head.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            this.rightArm.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
        }
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}