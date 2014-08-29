/*******************************************************************************
 * ModelHorseExtension.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
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

		seatCenter = new ModelRenderer(this, 80, 0);
		seatCenter.addBox(-5.0F, 0.0F, -3.0F, 10, 1, 8);
		seatCenter.setRotationPoint(0.0F, 2.0F, 2.0F);

		seatFront = new ModelRenderer(this, 106, 9);
		seatFront.addBox(-1.5F, -1.0F, -3.0F, 3, 1, 2);
		seatFront.setRotationPoint(0.0F, 2.0F, 2.0F);

		seatBack = new ModelRenderer(this, 80, 9);
		seatBack.addBox(-4.0F, -1.0F, 3.0F, 8, 1, 2);
		seatBack.setRotationPoint(0.0F, 2.0F, 2.0F);

		leftLegReinClip = new ModelRenderer(this, 74, 0);
		leftLegReinClip.addBox(-0.5F, 6.0F, -1.0F, 1, 2, 2);
		leftLegReinClip.setRotationPoint(5.0F, 3.0F, 2.0F);

		leftLegRein = new ModelRenderer(this, 70, 0);
		leftLegRein.addBox(-0.5F, 0.0F, -0.5F, 1, 6, 1);
		leftLegRein.setRotationPoint(5.0F, 3.0F, 2.0F);

		rightLegReinClip = new ModelRenderer(this, 74, 4);
		rightLegReinClip.addBox(-0.5F, 6.0F, -1.0F, 1, 2, 2);
		rightLegReinClip.setRotationPoint(-5.0F, 3.0F, 2.0F);

		rightLegRein = new ModelRenderer(this, 80, 0);
		rightLegRein.addBox(-0.5F, 0.0F, -0.5F, 1, 6, 1);
		rightLegRein.setRotationPoint(-5.0F, 3.0F, 2.0F);

		leftBit = new ModelRenderer(this, 74, 13);
		leftBit.addBox(1.5F, -8.0F, -4.0F, 1, 2, 2);
		leftBit.setRotationPoint(0.0F, 4.0F, -10.0F);
		setModelRotationAngles(leftBit, 0.5235988F, 0.0F, 0.0F);

		rightBit = new ModelRenderer(this, 74, 13);
		rightBit.addBox(-2.5F, -8.0F, -4.0F, 1, 2, 2);
		rightBit.setRotationPoint(0.0F, 4.0F, -10.0F);
		setModelRotationAngles(rightBit, 0.5235988F, 0.0F, 0.0F);

		leftNeckRein = new ModelRenderer(this, 44, 10);
		leftNeckRein.addBox(2.6F, -6.0F, -6.0F, 0, 3, 16);
		leftNeckRein.setRotationPoint(0.0F, 4.0F, -10.0F);

		rightNeckRein = new ModelRenderer(this, 44, 5);
		rightNeckRein.addBox(-2.6F, -6.0F, -6.0F, 0, 3, 16);
		rightNeckRein.setRotationPoint(0.0F, 4.0F, -10.0F);

		headpiece = new ModelRenderer(this, 80, 12);
		headpiece.addBox(-2.5F, -10.1F, -7.0F, 5, 5, 12, 0.2F);
		headpiece.setRotationPoint(0.0F, 4.0F, -10.0F);
		setModelRotationAngles(headpiece, 0.5235988F, 0.0F, 0.0F);
	}

	@Override
	public void render(Entity entity, float posX, float posY, float posZ, float rotationYaw, float rotationPitch, float yOffset)
	{
		super.render(entity, posX, posY, posZ, rotationYaw, rotationPitch, yOffset);
		final EntityHorse entityHorse = (EntityHorse) entity;

		if (entityHorse.riddenByEntity instanceof AbstractEntity)
		{
			headpiece.render(yOffset);
			seatCenter.render(yOffset);
			seatFront.render(yOffset);
			seatBack.render(yOffset);
			leftLegRein.render(yOffset);
			leftLegReinClip.render(yOffset);
			rightLegRein.render(yOffset);
			rightLegReinClip.render(yOffset);
			leftBit.render(yOffset);
			rightBit.render(yOffset);
			leftNeckRein.render(yOffset);
			rightNeckRein.render(yOffset);
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

		final EntityHorse entityHorse = (EntityHorse) entityLiving;

		if (entityHorse.riddenByEntity instanceof AbstractEntity)
		{
			final float yawOffset = adjuestRotations(entityLiving.prevRenderYawOffset, entityLiving.renderYawOffset, partialTickTime);
			final float rotationYawHead = adjuestRotations(entityLiving.prevRotationYawHead, entityLiving.rotationYawHead, partialTickTime);
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

			float rotationPitch360 = rotationPitch / (180F / (float) Math.PI);

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
			final float headRotateAngleY = rearingCurrent * (deltaYawOffset / (180F / (float) Math.PI)) + (1.0F - Math.max(rearingCurrent, grassEatingAmount)) * (deltaYawOffset / (180F / (float) Math.PI));
			final float bodyRotateAngleX = rearingCurrent * -((float) Math.PI / 4F) + rearingRemaining * 0.0F;

			seatCenter.rotationPointY = rearingCurrent * 0.5F + rearingRemaining * 2.0F;
			seatCenter.rotationPointZ = rearingCurrent * 11.0F + rearingRemaining * 2.0F;
			seatFront.rotationPointY = seatCenter.rotationPointY;
			seatBack.rotationPointY = seatCenter.rotationPointY;
			leftLegRein.rotationPointY = seatCenter.rotationPointY;
			rightLegRein.rotationPointY = seatCenter.rotationPointY;
			leftLegReinClip.rotationPointY = seatCenter.rotationPointY;
			rightLegReinClip.rotationPointY = seatCenter.rotationPointY;
			seatFront.rotationPointZ = seatCenter.rotationPointZ;
			seatBack.rotationPointZ = seatCenter.rotationPointZ;
			leftLegRein.rotationPointZ = seatCenter.rotationPointZ;
			rightLegRein.rotationPointZ = seatCenter.rotationPointZ;
			leftLegReinClip.rotationPointZ = seatCenter.rotationPointZ;
			rightLegReinClip.rotationPointZ = seatCenter.rotationPointZ;
			seatCenter.rotateAngleX = bodyRotateAngleX;
			seatFront.rotateAngleX = bodyRotateAngleX;
			seatBack.rotateAngleX = bodyRotateAngleX;
			leftNeckRein.rotationPointY = headRotatePointY;
			rightNeckRein.rotationPointY = headRotatePointY;
			headpiece.rotationPointY = headRotatePointY;
			leftBit.rotationPointY = headRotatePointY;
			rightBit.rotationPointY = headRotatePointY;
			leftNeckRein.rotationPointZ = headRotatePointZ;
			rightNeckRein.rotationPointZ = headRotatePointZ;
			headpiece.rotationPointZ = headRotatePointZ;
			leftBit.rotationPointZ = headRotatePointZ;
			rightBit.rotationPointZ = headRotatePointZ;
			leftNeckRein.rotateAngleX = rotationPitch360;
			rightNeckRein.rotateAngleX = rotationPitch360;
			headpiece.rotateAngleX = headRotateAngleX;
			leftBit.rotateAngleX = headRotateAngleX;
			rightBit.rotateAngleX = headRotateAngleX;
			headpiece.rotateAngleY = headRotateAngleY;
			leftBit.rotateAngleY = headRotateAngleY;
			leftNeckRein.rotateAngleY = headRotateAngleY;
			rightBit.rotateAngleY = headRotateAngleY;
			rightNeckRein.rotateAngleY = headRotateAngleY;

			leftLegRein.rotateAngleX = -1.0471976F;
			leftLegReinClip.rotateAngleX = -1.0471976F;
			rightLegRein.rotateAngleX = -1.0471976F;
			rightLegReinClip.rotateAngleX = -1.0471976F;
			leftLegRein.rotateAngleZ = 0.0F;
			leftLegReinClip.rotateAngleZ = 0.0F;
			rightLegRein.rotateAngleZ = 0.0F;
			rightLegReinClip.rotateAngleZ = 0.0F;
		}
	}
}
