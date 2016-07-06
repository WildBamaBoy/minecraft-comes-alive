package mca.client.model;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelMemorial extends ModelBase
{
	ModelRenderer ring;

	//Train
	ModelRenderer top;
	ModelRenderer backColumn;
	ModelRenderer basePlate;
	ModelRenderer frontExtension;
	ModelRenderer chimney;
	ModelRenderer wheelLF;
	ModelRenderer wheelLR;
	ModelRenderer wheelRF;
	ModelRenderer wheelRR;

	//Doll
	ModelRenderer head;
	ModelRenderer body;
	ModelRenderer rightarm;
	ModelRenderer leftarm;
	ModelRenderer rightleg;
	ModelRenderer leftleg;

	public ModelMemorial()
	{
		textureWidth = 64;
		textureHeight = 32;

		ring = new ModelRenderer(this, -16, 0);
		ring.addBox(-8F, -0.5F, -8F, 16, 1, 16);
		ring.setRotationPoint(0F, 24.4F, 0F);
		ring.setTextureSize(64, 32);
		ring.mirror = true;
		setRotation(ring, 0F, 0.8179294F, 0F);

		top = new ModelRenderer(this, 0, 0);
		top.addBox(0F, 0F, 0F, 8, 1, 8);
		top.setRotationPoint(0F, 0F, 0F);
		top.setTextureSize(64, 32);
		top.mirror = true;
		setRotation(top, 0F, 0F, 0F);
		backColumn = new ModelRenderer(this, 0, 9);
		backColumn.addBox(0F, 0F, 0F, 6, 6, 6);
		backColumn.setRotationPoint(1F, 1F, 1F);
		backColumn.setTextureSize(64, 32);
		backColumn.mirror = true;
		setRotation(backColumn, 0F, 0F, 0F);
		basePlate = new ModelRenderer(this, 2, 23);
		basePlate.addBox(0F, 0F, 0F, 19, 1, 8);
		basePlate.setRotationPoint(-10F, 7F, 0F);
		basePlate.setTextureSize(64, 32);
		basePlate.mirror = true;
		setRotation(basePlate, 0F, 0F, 0F);
		frontExtension = new ModelRenderer(this, 32, 0);
		frontExtension.addBox(0F, 0F, 0F, 9, 4, 4);
		frontExtension.setRotationPoint(-8F, 3F, 2F);
		frontExtension.setTextureSize(64, 32);
		frontExtension.mirror = true;
		setRotation(frontExtension, 0F, 0F, 0F);
		chimney = new ModelRenderer(this, 41, 11);
		chimney.addBox(0F, 0F, 0F, 2, 3, 2);
		chimney.setRotationPoint(-6F, 0F, 3F);
		chimney.setTextureSize(64, 32);
		chimney.mirror = true;
		setRotation(chimney, 0F, 0F, 0F);
		wheelLF = new ModelRenderer(this, 29, 11);
		wheelLF.addBox(-2F, -2F, -0.5F, 4, 4, 1);
		wheelLF.setRotationPoint(-6F, 7.5F, -0.5F);
		wheelLF.setTextureSize(64, 32);
		wheelLF.mirror = true;
		setRotation(wheelLF, 0F, 0F, 0.669215F);
		wheelLR = new ModelRenderer(this, 29, 11);
		wheelLR.addBox(-2F, -2F, -0.5F, 4, 4, 1);
		wheelLR.setRotationPoint(5F, 7.5F, -0.5F);
		wheelLR.setTextureSize(64, 32);
		wheelLR.mirror = true;
		setRotation(wheelLR, 0F, 0F, 0.6556861F);
		wheelRF = new ModelRenderer(this, 29, 11);
		wheelRF.addBox(-2F, -2F, -0.5F, 4, 4, 1);
		wheelRF.setRotationPoint(-6F, 7.5F, 8.5F);
		wheelRF.setTextureSize(64, 32);
		wheelRF.mirror = true;
		setRotation(wheelRF, 0F, 0F, 1.710216F);
		wheelRR = new ModelRenderer(this, 29, 11);
		wheelRR.addBox(-2F, -2F, -0.5F, 4, 4, 1);
		wheelRR.setRotationPoint(5F, 7.5F, 8.5F);
		wheelRR.setTextureSize(64, 32);
		wheelRR.mirror = true;
		setRotation(wheelRR, 0F, 0F, 1.115358F);
		
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-4F, -8F, -4F, 8, 8, 8);
		head.setRotationPoint(0F, 0F, 0F);
		head.setTextureSize(64, 32);
		head.mirror = true;
		setRotation(head, 0F, 0F, 0F);
		body = new ModelRenderer(this, 16, 16);
		body.addBox(-4F, 0F, -2F, 8, 12, 4);
		body.setRotationPoint(0F, 0F, 0F);
		body.setTextureSize(64, 32);
		body.mirror = true;
		setRotation(body, 0F, 0F, 0F);
		rightarm = new ModelRenderer(this, 40, 16);
		rightarm.addBox(-3F, -2F, -2F, 4, 12, 4);
		rightarm.setRotationPoint(-5F, 2F, 0F);
		rightarm.setTextureSize(64, 32);
		rightarm.mirror = true;
		setRotation(rightarm, 0F, 0F, 0F);
		leftarm = new ModelRenderer(this, 40, 16);
		leftarm.addBox(-1F, -2F, -2F, 4, 12, 4);
		leftarm.setRotationPoint(5F, 2F, 0F);
		leftarm.setTextureSize(64, 32);
		leftarm.mirror = true;
		setRotation(leftarm, 0F, 0F, 0F);
		rightleg = new ModelRenderer(this, 0, 16);
		rightleg.addBox(-2F, 0F, -2F, 4, 12, 4);
		rightleg.setRotationPoint(-2F, 12F, 0F);
		rightleg.setTextureSize(64, 32);
		rightleg.mirror = true;
		setRotation(rightleg, 0F, 0F, 0F);
		leftleg = new ModelRenderer(this, 0, 16);
		leftleg.addBox(-2F, 0F, -2F, 4, 12, 4);
		leftleg.setRotationPoint(2F, 12F, 0F);
		leftleg.setTextureSize(64, 32);
		leftleg.mirror = true;
		setRotation(leftleg, 0F, 0F, 0F);
	}

	public void renderRing()
	{
		ring.render(0.0625F);
	}

	public void renderTrain()
	{
		GL11.glPushMatrix();
		{
			GL11.glScaled(0.5D, 0.5D, 0.5D);
			GL11.glTranslated(0.1D, 2.40D, -0.1D);
			top.render(0.0625F);
			backColumn.render(0.0625F);
			basePlate.render(0.0625F);
			frontExtension.render(0.0625F);
			chimney.render(0.0625F);
			wheelLF.render(0.0625F);
			wheelLR.render(0.0625F);
			wheelRF.render(0.0625F);
			wheelRR.render(0.0625F);
		}
		GL11.glPopMatrix();
	}

	public void renderDoll()
	{
		GL11.glPushMatrix();
		{
			GL11.glScaled(0.4D, 0.4D, 0.4D);
			GL11.glRotated(-90.0, 1.0D, 0.0D, 0.0D);
			GL11.glRotated(33.0, 0.0D, 0.0D, 1.0D);
			GL11.glTranslated(0D, -0.7D, 3.6D);
			
		    head.render(0.0625F);
		    body.render(0.0625F);
		    rightarm.render(0.0625F);
		    leftarm.render(0.0625F);
		    rightleg.render(0.0625F);
		    leftleg.render(0.0625F);
		}
		GL11.glPopMatrix();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
