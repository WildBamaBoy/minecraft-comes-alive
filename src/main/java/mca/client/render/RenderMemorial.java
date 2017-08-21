/*******************************************************************************
 * RenderTombstone.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.client.render;

import org.lwjgl.opengl.GL11;

import mca.client.model.ModelMemorial;
import mca.tile.TileMemorial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMemorial extends TileEntitySpecialRenderer<TileMemorial>
{
	private static final ResourceLocation RING_TEXTURE = new ResourceLocation("mca:textures/brokenringentity.png");
	private static final ResourceLocation TRAIN_TEXTURE = new ResourceLocation("mca:textures/trainentity.png");
	private static final ResourceLocation DOLL_TEXTURE = new ResourceLocation("mca:textures/skins/FarmerF12.png");

	private final ModelMemorial memorialModel;

	public RenderMemorial()
	{
		memorialModel = new ModelMemorial();
	}

	public void renderMemorialAt(TileMemorial memorialEntity, double posX, double posY, double posZ, float partialTickTime)
	{
		GL11.glPushMatrix();

		GL11.glTranslated(posX + 0.50F, posY + 1.5F, posZ + 0.53F);
		GL11.glScalef(1F, -1F, -1F);
		GL11.glScalef(0.7F, 1.0F, 0.7F);

		try
		{
			switch (memorialEntity.getType())
			{
			case BROKEN_RING: bindResource(RING_TEXTURE); memorialModel.renderRing(); break;
			case TRAIN: bindResource(TRAIN_TEXTURE); memorialModel.renderTrain(); break;
			case DOLL: bindResource(DOLL_TEXTURE); memorialModel.renderDoll(); break;
			}
		}

		catch (Exception e)
		{
			//Pass
		}

		GL11.glPopMatrix();
	}

	public void render(TileMemorial te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		renderMemorialAt((TileMemorial) te, x, y, z, partialTicks);
	}

	protected void bindResource(ResourceLocation resourceLocation)
	{
		final TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

		if (textureManager != null)
		{
			textureManager.bindTexture(resourceLocation);
		}
	}
}
