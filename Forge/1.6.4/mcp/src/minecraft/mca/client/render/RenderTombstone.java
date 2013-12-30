/*******************************************************************************
 * RenderTombstone.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.render;

import mca.client.model.ModelTombstone;
import mca.core.Constants;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * Determines how the Tombstone is rendered.
 */
public class RenderTombstone extends TileEntitySpecialRenderer
{
	private static final ResourceLocation texture = new ResourceLocation("mca:textures/blocks/Tombstone.png");
	private final ModelTombstone tombstoneModel;

	/**
	 * Constructor
	 */
	public RenderTombstone()
	{
		tombstoneModel = new ModelTombstone();
	}

	/**
	 * Renders the tombstone at the provided location.
	 * 
	 * @param 	tombstoneEntity	The tombstone being rendered.
	 * @param 	posX			The tombstone's x position.
	 * @param 	posY			The tombstone's y position.
	 * @param 	posZ			The tombstone's z position.
	 * @param 	partialTickTime	The amount of time since the last in-game tick.
	 */
	public void renderTileEntityTombstoneAt(TileEntityTombstone tombstoneEntity, double posX, double posY, double posZ, float partialTickTime)
	{
		final StringBuilder stringBuilder = new StringBuilder();
		final FontRenderer fontRenderer = getFontRenderer();
		fontRenderer.FONT_HEIGHT = 4;

		final int meta = tombstoneEntity.getBlockMetadata();
		float rotation = 0.0F;

		GL11.glPushMatrix();

		switch (meta)
		{
		case 0: rotation = 0.0F; break;
		case 1: rotation = 45F; break;
		case 2: rotation = 45F; break;
		case 3: rotation = 45F; break;
		case 4: rotation = 90F; break;
		case 5: rotation = 135F; break;
		case 6: rotation = 135F; break;
		case 7: rotation = 135F; break;
		case 8: rotation = 180F; break;
		case 9: rotation = 225F; break;
		case 10: rotation = 225F; break;
		case 11: rotation = 225F; break;
		case 12: rotation = 270F; break;
		case 13: rotation = 315F; break;
		case 14: rotation = 315F; break;
		case 15: rotation = 315F; break;
		default: break;
		}

		GL11.glTranslated(posX + 0.45F, posY + 1F, posZ + 0.53F);
		GL11.glRotatef(-rotation, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(0, 0.5F, 0);
		this.bindResource(texture);

		GL11.glPushMatrix();
		{
			GL11.glScalef(1F, -1F, -1F);
			tombstoneModel.renderTombstone();
		}
		GL11.glPopMatrix();

		//Text size is 0.017F.
		GL11.glTranslatef(0.0F, -1.1F, 0.07F);
		GL11.glScalef(0.017F / 2, -0.017F / 2, 0.017F / 2);
		GL11.glNormal3f(0.0F, 0.0F, -1F * 0.017F);
		GL11.glDepthMask(false);

		for (int line = 0; line < tombstoneEntity.signText.length; line++)
		{
			String lineText = Constants.FORMAT_BOLD + tombstoneEntity.signText[line];

			if (line == tombstoneEntity.lineBeingEdited)
			{
				lineText = stringBuilder.append("> ").append(lineText).append(" <").toString();
			}
			
			fontRenderer.drawString(lineText, -fontRenderer.getStringWidth(lineText) / 2, line * 10 - tombstoneEntity.signText.length * 5, 0x000000);
		}

		GL11.glDepthMask(true);
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity tombstoneEntity, double posX, double posY, double posZ, float partialTickTime)
	{
		renderTileEntityTombstoneAt((TileEntityTombstone)tombstoneEntity, posX, posY, posZ, partialTickTime);
	}

	protected void bindResource(ResourceLocation resourceLocation)
	{
		final TextureManager textureManager = this.tileEntityRenderer.renderEngine;

		if (textureManager != null)
		{
			textureManager.bindTexture(resourceLocation);
		}
	}
}
