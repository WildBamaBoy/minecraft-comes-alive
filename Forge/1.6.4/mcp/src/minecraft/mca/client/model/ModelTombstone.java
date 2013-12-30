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
    private final ModelRenderer base;
    private final ModelRenderer center;
    private final ModelRenderer centerCurve;
    private final ModelRenderer topEdge;

    /**
     * Constructor
     */
    public ModelTombstone()
    {
        textureWidth = 64;
        textureHeight = 32;
        
        base = new ModelRenderer(this, 0, 28);
        base.addBox(0F, 0F, 0F, 14, 1, 3);
        base.setRotationPoint(-7F, 23F, -2F);
        base.setTextureSize(64, 32);
        
        center = new ModelRenderer(this, 6, 18);
        center.addBox(0F, 0F, 0F, 10, 9, 1);
        center.setRotationPoint(-5F, 14F, -1F);
        center.setTextureSize(64, 32);
        
        centerCurve = new ModelRenderer(this, 8, 16);
        centerCurve.addBox(0F, 0F, 0F, 8, 1, 1);
        centerCurve.setRotationPoint(-4F, 13F, -1F);
        centerCurve.setTextureSize(64, 32);
        
        topEdge = new ModelRenderer(this, 10, 14);
        topEdge.addBox(0F, 0F, 0F, 6, 1, 1);
        topEdge.setRotationPoint(-3F, 12F, -1F);
        topEdge.setTextureSize(64, 32);
    }

    /**
     * Renders each component of the tombstone.
     */
    public void renderTombstone()
    {
        base.render(0.0625F);
        center.render(0.0625F);
        centerCurve.render(0.0625F);
        topEdge.render(0.0625F);
    }
}
