/*******************************************************************************
 * RenderTombstone.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import net.minecraft.block.Block;
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
	
	/** The model of the tombstone. */
	private ModelTombstone tombstoneModel;

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
		Block block = tombstoneEntity.getBlockType();
		int meta = tombstoneEntity.getBlockMetadata();
		float rotation = 0.0F;
		
		GL11.glPushMatrix();

		switch (meta)
		{
			case 2: rotation = 180F; break;
			case 4: rotation = 90F; break;
			case 8: rotation = -180F; break;
			case 12: rotation = -90F; break;
		}

		//Orient the tombstone properly and give it a texture.
		GL11.glTranslatef((float)posX + 0.5F, (float)posY + 1F, (float)posZ + 0.5F);
		GL11.glRotatef(-rotation, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(0, (float)0.5F, 0);
		this.func_110628_a(texture);

		GL11.glPushMatrix();
		
		GL11.glScalef(1F, -1F, -1F);
		tombstoneModel.renderTombstone();
		
		GL11.glPopMatrix();
		
		//Render the text.
		FontRenderer fontrenderer = getFontRenderer();
		fontrenderer.FONT_HEIGHT = 4;
		
		//Text size is 0.017F.
		GL11.glTranslatef(0.0F, -1.1F, 0.07F);
		GL11.glScalef(0.017F / 2, -0.017F / 2, 0.017F / 2);
		GL11.glNormal3f(0.0F, 0.0F, -1F * 0.017F);
		GL11.glDepthMask(false);

		for (int line = 0; line < tombstoneEntity.signText.length; line++)
		{
			String lineText = tombstoneEntity.signText[line];

			if (line == tombstoneEntity.lineBeingEdited)
			{
				lineText = (new StringBuilder()).append("> ").append(lineText).append(" <").toString();

				if (tombstoneEntity.guiOpen)
				{
					fontrenderer.drawString(lineText, -fontrenderer.getStringWidth(lineText) / 2, line * 10 - tombstoneEntity.signText.length * 5, 0x000000);
				}

				else
				{
					fontrenderer.drawString(lineText, -fontrenderer.getStringWidth(lineText) / 2, line * 10 - tombstoneEntity.signText.length * 5, 0xCFB52B);
				}
			}
			
			else
			{
				if (tombstoneEntity.guiOpen)
				{
					fontrenderer.drawString(lineText, -fontrenderer.getStringWidth(lineText) / 2, line * 10 - tombstoneEntity.signText.length * 5, 0x000000);
				}
				
				else
				{
					fontrenderer.drawString(lineText, -fontrenderer.getStringWidth(lineText) / 2, line * 10 - tombstoneEntity.signText.length * 5, 0xCFB52B);
				}
			}

		}

		GL11.glDepthMask(true);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity tombstoneEntity, double posX, double posY, double posZ, float partialTickTime)
	{
		renderTileEntityTombstoneAt((TileEntityTombstone)tombstoneEntity, posX, posY, posZ, partialTickTime);
	}
	
    protected void func_110628_a(ResourceLocation par1ResourceLocation)
    {
        TextureManager texturemanager = this.tileEntityRenderer.renderEngine;

        if (texturemanager != null)
        {
            texturemanager.func_110577_a(par1ResourceLocation);
        }
    }
}
