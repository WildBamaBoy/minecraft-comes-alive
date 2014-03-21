/*******************************************************************************
 * ModelHorseExtension.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.model;

import mca.entity.AbstractEntity;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.MathHelper;

/**
 * Forces the saddle to render on a horse when an MCA villager is riding it.
 */
public class ModelHorseExtension extends ModelHorse
{
	private final ModelRenderer headpiece;
	private final ModelRenderer seatCenter;
	private final ModelRenderer seatFront;
	private final ModelRenderer seatBack;
	private final ModelRenderer leftLegRein;
	private final ModelRenderer leftLegReinClip;
	private final ModelRenderer rightLegRein;
	private final ModelRenderer rightLegReinClip;
	private final ModelRenderer leftBit;
	private final ModelRenderer rightBit;
	private final ModelRenderer leftNeckRein;
	private final ModelRenderer rightNeckRein;

	/**
	 * Constructs the model.
	 */
	public ModelHorseExtension()
	{
		super();

		this.seatCenter = new ModelRenderer(this, 80, 0);
		this.seatCenter.addBox(-5.0F, 0.0F, -3.0F, 10, 1, 8);
		this.seatCenter.setRotationPoint(0.0F, 2.0F, 2.0F);

		this.seatFront = new ModelRenderer(this, 106, 9);
		this.seatFront.addBox(-1.5F, -1.0F, -3.0F, 3, 1, 2);
		this.seatFront.setRotationPoint(0.0F, 2.0F, 2.0F);

		this.seatBack = new ModelRenderer(this, 80, 9);
		this.seatBack.addBox(-4.0F, -1.0F, 3.0F, 8, 1, 2);
		this.seatBack.setRotationPoint(0.0F, 2.0F, 2.0F);

		this.leftLegReinClip = new ModelRenderer(this, 74, 0);
		this.leftLegReinClip.addBox(-0.5F, 6.0F, -1.0F, 1, 2, 2);
		this.leftLegReinClip.setRotationPoint(5.0F, 3.0F, 2.0F);

		this.leftLegRein = new ModelRenderer(this, 70, 0);
		this.leftLegRein.addBox(-0.5F, 0.0F, -0.5F, 1, 6, 1);
		this.leftLegRein.setRotationPoint(5.0F, 3.0F, 2.0F);

		this.rightLegReinClip = new ModelRenderer(this, 74, 4);
		this.rightLegReinClip.addBox(-0.5F, 6.0F, -1.0F, 1, 2, 2);
		this.rightLegReinClip.setRotationPoint(-5.0F, 3.0F, 2.0F);

		this.rightLegRein = new ModelRenderer(this, 80, 0);
		this.rightLegRein.addBox(-0.5F, 0.0F, -0.5F, 1, 6, 1);
		this.rightLegRein.setRotationPoint(-5.0F, 3.0F, 2.0F);

		this.leftBit = new ModelRenderer(this, 74, 13);
		this.leftBit.addBox(1.5F, -8.0F, -4.0F, 1, 2, 2);
		this.leftBit.setRotationPoint(0.0F, 4.0F, -10.0F);
		this.setModelRotationAngles(this.leftBit, 0.5235988F, 0.0F, 0.0F);

		this.rightBit = new ModelRenderer(this, 74, 13);
		this.rightBit.addBox(-2.5F, -8.0F, -4.0F, 1, 2, 2);
		this.rightBit.setRotationPoint(0.0F, 4.0F, -10.0F);
		this.setModelRotationAngles(this.rightBit, 0.5235988F, 0.0F, 0.0F);

		this.leftNeckRein = new ModelRenderer(this, 44, 10);
		this.leftNeckRein.addBox(2.6F, -6.0F, -6.0F, 0, 3, 16);
		this.leftNeckRein.setRotationPoint(0.0F, 4.0F, -10.0F);

		this.rightNeckRein = new ModelRenderer(this, 44, 5);
		this.rightNeckRein.addBox(-2.6F, -6.0F, -6.0F, 0, 3, 16);
		this.rightNeckRein.setRotationPoint(0.0F, 4.0F, -10.0F);

		this.headpiece = new ModelRenderer(this, 80, 12);
		this.headpiece.addBox(-2.5F, -10.1F, -7.0F, 5, 5, 12, 0.2F);
		this.headpiece.setRotationPoint(0.0F, 4.0F, -10.0F);
		this.setModelRotationAngles(this.headpiece, 0.5235988F, 0.0F, 0.0F);
	}

	@Override
	public void render(Entity entity, float posX, float posY, float posZ, float rotationYaw, float rotationPitch, float yOffset) 
	{
		super.render(entity, posX, posY, posZ, rotationYaw, rotationPitch, yOffset);
		final EntityHorse entityHorse = (EntityHorse)entity;

		if (entityHorse.riddenByEntity instanceof AbstractEntity)
		{
			this.headpiece.render(yOffset);
			this.seatCenter.render(yOffset);
			this.seatFront.render(yOffset);
			this.seatBack.render(yOffset);
			this.leftLegRein.render(yOffset);
			this.leftLegReinClip.render(yOffset);
			this.rightLegRein.render(yOffset);
			this.rightLegReinClip.render(yOffset);
			this.leftBit.render(yOffset);
			this.rightBit.render(yOffset);
			this.leftNeckRein.render(yOffset);
			this.rightNeckRein.render(yOffset);
		}
	}

	private void setModelRotationAngles(ModelRenderer modelRenderer, float rotationAngleX, float rotationAngleY, float rotationAngleZ)
	{
		modelRenderer.rotateAngleX = rotationAngleX;
		modelRenderer.rotateAngleY = rotationAngleY;
		modelRenderer.rotateAngleZ = rotationAngleZ;
	}

	private float adjuestRotations(float prevRotation, float currentRotation, float partialTickTime)
	{
		float deltaRotations;

		for (deltaRotations = currentRotation - prevRotation; deltaRotations < -180.0F; deltaRotations += 360.0F)
		{
			;
		}

		while (deltaRotations >= 180.0F)
		{
			deltaRotations -= 360.0F;
		}

		return prevRotation + partialTickTime * deltaRotations;
	}

	@Override
	public void setLivingAnimations(EntityLivingBase entityLiving, float limbSwing, float prevLimbSwing, float partialTickTime) 
	{
		super.setLivingAnimations(entityLiving, limbSwing, prevLimbSwing, partialTickTime);

		final EntityHorse entityHorse = (EntityHorse)entityLiving;

		if (entityHorse.riddenByEntity instanceof AbstractEntity)
		{
			final float yawOffset = this.adjuestRotations(entityLiving.prevRenderYawOffset, entityLiving.renderYawOffset, partialTickTime);
			final float rotationYawHead = this.adjuestRotations(entityLiving.prevRotationYawHead, entityLiving.rotationYawHead, partialTickTime);
			final float rotationPitch = entityLiving.prevRotationPitch + (entityLiving.rotationPitch - entityLiving.prevRotationPitch) * partialTickTime;

			float deltaYawOffset = rotationYawHead - yawOffset;

			if (deltaYawOffset > 20.0F)
			{
				deltaYawOffset = 20.0F;
			}

			if (deltaYawOffset < -20.0F)
			{
				deltaYawOffset = -20.0F;
			}

			float rotationPitch360 = rotationPitch / (180F / (float)Math.PI);

			if (prevLimbSwing > 0.2F)
			{
				rotationPitch360 += MathHelper.cos(limbSwing * 0.4F) * 0.15F * prevLimbSwing;
			}

			final float grassEatingAmount = entityHorse.getGrassEatingAmount(partialTickTime);
			final float rearingCurrent = entityHorse.getRearingAmount(partialTickTime);
			final float rearingRemaining = 1.0F - rearingCurrent;

			entityHorse.func_110201_q(partialTickTime);
			final float headRotatePointY = rearingCurrent * -6.0F + grassEatingAmount * 11.0F + (1.0F - Math.max(rearingCurrent, grassEatingAmount)) * 4.0F;
			final float headRotatePointZ = rearingCurrent * -1.0F + grassEatingAmount * -10.0F + (1.0F - Math.max(rearingCurrent, grassEatingAmount)) * -10.0F;
			final float headRotateAngleX = rearingCurrent * (0.2617994F + rotationPitch360) + grassEatingAmount * 2.18166F + (1.0F - Math.max(rearingCurrent, grassEatingAmount)) * (0.5235988F + rotationPitch360);
			final float headRotateAngleY = rearingCurrent * (deltaYawOffset / (180F / (float)Math.PI)) + (1.0F - Math.max(rearingCurrent, grassEatingAmount)) * (deltaYawOffset / (180F / (float)Math.PI));
			final float bodyRotateAngleX = rearingCurrent * -((float)Math.PI / 4F) + rearingRemaining * 0.0F;

			this.seatCenter.rotationPointY = rearingCurrent * 0.5F + rearingRemaining * 2.0F;
			this.seatCenter.rotationPointZ = rearingCurrent * 11.0F + rearingRemaining * 2.0F;
			this.seatFront.rotationPointY = this.seatCenter.rotationPointY;
			this.seatBack.rotationPointY = this.seatCenter.rotationPointY;
			this.leftLegRein.rotationPointY = this.seatCenter.rotationPointY;
			this.rightLegRein.rotationPointY = this.seatCenter.rotationPointY;
			this.leftLegReinClip.rotationPointY = this.seatCenter.rotationPointY;
			this.rightLegReinClip.rotationPointY = this.seatCenter.rotationPointY;
			this.seatFront.rotationPointZ = this.seatCenter.rotationPointZ;
			this.seatBack.rotationPointZ = this.seatCenter.rotationPointZ;
			this.leftLegRein.rotationPointZ = this.seatCenter.rotationPointZ;
			this.rightLegRein.rotationPointZ = this.seatCenter.rotationPointZ;
			this.leftLegReinClip.rotationPointZ = this.seatCenter.rotationPointZ;
			this.rightLegReinClip.rotationPointZ = this.seatCenter.rotationPointZ;
			this.seatCenter.rotateAngleX = bodyRotateAngleX;
			this.seatFront.rotateAngleX = bodyRotateAngleX;
			this.seatBack.rotateAngleX = bodyRotateAngleX;
			this.leftNeckRein.rotationPointY = headRotatePointY;
			this.rightNeckRein.rotationPointY = headRotatePointY;
			this.headpiece.rotationPointY = headRotatePointY;
			this.leftBit.rotationPointY = headRotatePointY;
			this.rightBit.rotationPointY = headRotatePointY;
			this.leftNeckRein.rotationPointZ = headRotatePointZ;
			this.rightNeckRein.rotationPointZ = headRotatePointZ;
			this.headpiece.rotationPointZ = headRotatePointZ;
			this.leftBit.rotationPointZ = headRotatePointZ;
			this.rightBit.rotationPointZ = headRotatePointZ;
			this.leftNeckRein.rotateAngleX = rotationPitch360;
			this.rightNeckRein.rotateAngleX = rotationPitch360;
			this.headpiece.rotateAngleX = headRotateAngleX;
			this.leftBit.rotateAngleX = headRotateAngleX;
			this.rightBit.rotateAngleX = headRotateAngleX;
			this.headpiece.rotateAngleY = headRotateAngleY;
			this.leftBit.rotateAngleY = headRotateAngleY;
			this.leftNeckRein.rotateAngleY = headRotateAngleY;
			this.rightBit.rotateAngleY = headRotateAngleY;
			this.rightNeckRein.rotateAngleY = headRotateAngleY;

			this.leftLegRein.rotateAngleX = -1.0471976F;
			this.leftLegReinClip.rotateAngleX = -1.0471976F;
			this.rightLegRein.rotateAngleX = -1.0471976F;
			this.rightLegReinClip.rotateAngleX = -1.0471976F;
			this.leftLegRein.rotateAngleZ = 0.0F;
			this.leftLegReinClip.rotateAngleZ = 0.0F;
			this.rightLegRein.rotateAngleZ = 0.0F;
			this.rightLegReinClip.rotateAngleZ = 0.0F;
		}
	}
}
