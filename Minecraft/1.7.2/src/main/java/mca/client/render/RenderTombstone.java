/*******************************************************************************
 * RenderTombstone.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.render;

import mca.client.model.ModelTombstone;
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
	private static final ResourceLocation TEXTURE = new ResourceLocation("mca:textures/blocks/Tombstone.png");
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
		final FontRenderer fontRendererObj = this.func_147498_b();

		final int meta = tombstoneEntity.getBlockMetadata();
		final float rotation = setRotationByMeta(meta);

		GL11.glPushMatrix();

		GL11.glTranslated(posX + 0.50F, posY + 1.59F, posZ + 0.53F);
		GL11.glRotatef(-rotation, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(0, 0.5F, 0);
		GL11.glScalef(1.4F, 1.4F, 1.4F);
		
		this.bindResource(TEXTURE);

		GL11.glPushMatrix();
		{
			GL11.glScalef(1F, -1F, -1F);
			tombstoneModel.renderTombstone();
		}
		GL11.glPopMatrix();

		//Text size is 0.017F.
		GL11.glTranslatef(0.0F, -1.15F, 0.07F);
		GL11.glScalef(0.017F / 2, -0.017F / 2, 0.017F / 2);
		GL11.glNormal3f(0.0F, 0.0F, -1F * 0.017F);
		GL11.glDepthMask(false);

		for (int line = 0; line < tombstoneEntity.signText.length; line++)
		{
			String lineText = tombstoneEntity.signText[line];

			if (line == tombstoneEntity.lineBeingEdited)
			{
				lineText = stringBuilder.append("> ").append(lineText).append(" <").toString();
			}
			
			fontRendererObj.drawString(lineText, -fontRendererObj.getStringWidth(lineText) / 2, line * 10 - tombstoneEntity.signText.length * 5, 0x000000);
		}

		GL11.glDepthMask(true);
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity tombstoneEntity, double posX, double posY, double posZ, float partialTickTime)
	{
		renderTileEntityTombstoneAt((TileEntityTombstone)tombstoneEntity, posX, posY, posZ, partialTickTime);
	}

	/**
	 * Binds the provided resource location to the texture manager.
	 * 
	 * @param 	resourceLocation	The tombstone's texture location.
	 */
	protected void bindResource(ResourceLocation resourceLocation)
	{
		final TextureManager textureManager = this.field_147501_a.field_147553_e;

		if (textureManager != null)
		{
			textureManager.bindTexture(resourceLocation);
		}
	}
	
	private float setRotationByMeta(int meta)
	{
		switch (meta)
		{
		case 0: return 0F;
		case 1: return 45F;
		case 2: return 45F;
		case 3: return 45F;
		case 4: return 90F;
		case 5: return 135F;
		case 6: return 135F;
		case 7: return 135F;
		case 8: return 180F;
		case 9: return 225F;
		case 10: return 225F;
		case 11: return 225F;
		case 12: return 270F;
		case 13: return 315F;
		case 14: return 315F;
		case 15: return 315F;
		default: return 0F;
		}
	}
}
