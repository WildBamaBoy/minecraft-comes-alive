package mca.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelMemorial extends ModelBase
{
	ModelRenderer ring;

	public ModelMemorial()
	{
		textureWidth = 64;
		textureHeight = 32;

		ring = new ModelRenderer(this, -16, 0);
		ring.addBox(-8F, -0.5F, -8F, 16, 1, 16);
		ring.setRotationPoint(0F, 24.4F, 0F);
		ring.setTextureSize(64, 32);
		ring.mirror = true;
		setRotation(ring, 0F, 0.8179294F, 0F);
	}

	public void renderRing()
	{
		ring.render(0.0625F);
	}
	
	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
