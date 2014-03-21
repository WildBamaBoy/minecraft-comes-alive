/*******************************************************************************
 * ModelTombstone.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/**
 * Defines the model of a Tombstone.
 */
public class ModelTombstone extends ModelBase
{
	/** The base, or bottom of the tombstone. */
	ModelRenderer base;
	
	/** The center text area of the timestone. */
	ModelRenderer textArea;
	
	/** The topmost curve of the tombstone. */
	ModelRenderer topCurve;

	/**
	 * Constructs the tombstone model.
	 */
	public ModelTombstone()
	{
		textureWidth = 64;
		textureHeight = 64;

		base = new ModelRenderer(this, 0, 0);
		base.addBox(0F, 0F, 0F, 14, 1, 6);
		base.setRotationPoint(-7F, 23F, -3F);
		base.setTextureSize(64, 64);
		textArea = new ModelRenderer(this, 0, 11);
		textArea.addBox(0F, 0F, 0F, 12, 8, 2);
		textArea.setRotationPoint(-6F, 15F, -1F);
		textArea.setTextureSize(64, 64);
		topCurve = new ModelRenderer(this, 35, 18);
		topCurve.addBox(0F, 0F, 0F, 10, 1, 2);
		topCurve.setRotationPoint(-5F, 14F, -1F);
		topCurve.setTextureSize(64, 64);
	}
	
	/**
	 * Renders each component of the tombstone.
	 */
	public void renderTombstone()
	{
		base.render(0.0625F);
		textArea.render(0.0625F);
		topCurve.render(0.0625F);
	}
}
