package mca.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelTombstone extends ModelBase
{
	ModelRenderer base;
	ModelRenderer textArea;
	ModelRenderer topCurve;

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

	public void renderTombstone()
	{
		base.render(0.0625F);
		textArea.render(0.0625F);
		topCurve.render(0.0625F);
	}
}
