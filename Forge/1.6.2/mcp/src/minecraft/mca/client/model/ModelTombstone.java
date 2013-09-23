/*******************************************************************************
 * ModelTombstone.java
 * Copyright (c) 2013 WildBamaBoy.
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
    private ModelRenderer tombstoneBase;
    private ModelRenderer tombstoneBlock;
    private ModelRenderer tombstoneBlockMidpoint;
    private ModelRenderer tombstoneBlockTopPoint;

    /**
     * Constructor
     */
    public ModelTombstone()
    {
        textureWidth = 64;
        textureHeight = 32;
        
        tombstoneBase = new ModelRenderer(this, 0, 28);
        tombstoneBase.addBox(0F, 0F, 0F, 14, 1, 3);
        tombstoneBase.setRotationPoint(-7F, 23F, -2F);
        tombstoneBase.setTextureSize(64, 32);
        
        tombstoneBlock = new ModelRenderer(this, 6, 18);
        tombstoneBlock.addBox(0F, 0F, 0F, 10, 9, 1);
        tombstoneBlock.setRotationPoint(-5F, 14F, -1F);
        tombstoneBlock.setTextureSize(64, 32);
        
        tombstoneBlockMidpoint = new ModelRenderer(this, 8, 16);
        tombstoneBlockMidpoint.addBox(0F, 0F, 0F, 8, 1, 1);
        tombstoneBlockMidpoint.setRotationPoint(-4F, 13F, -1F);
        tombstoneBlockMidpoint.setTextureSize(64, 32);
        
        tombstoneBlockTopPoint = new ModelRenderer(this, 10, 14);
        tombstoneBlockTopPoint.addBox(0F, 0F, 0F, 6, 1, 1);
        tombstoneBlockTopPoint.setRotationPoint(-3F, 12F, -1F);
        tombstoneBlockTopPoint.setTextureSize(64, 32);
    }

    /**
     * Renders each component of the tombstone.
     */
    public void renderTombstone()
    {
        tombstoneBase.render(0.0625F);
        tombstoneBlock.render(0.0625F);
        tombstoneBlockMidpoint.render(0.0625F);
        tombstoneBlockTopPoint.render(0.0625F);
    }
}
