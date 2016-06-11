package mca.client.render;

import org.lwjgl.opengl.GL11;

import mca.client.model.ModelGrimReaper;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.ResourceLocation;

public class RenderGrimReaper extends RenderBiped
{
	public RenderGrimReaper() 
	{
		super(new ModelGrimReaper(), 0.5F);
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("mca:textures/grimreaper.png");

	@Override
	public void doRender(Entity entity, double posX, double posY, double posZ, float angle, float offsetY) 
	{
		super.doRender(entity, posX, posY, posZ, angle, offsetY);
		BossStatus.setBossStatus((IBossDisplayData) entity, false);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entity, float partialTickTime) 
	{
		super.preRenderCallback(entity, partialTickTime);
		
		double scale = 1.3D;
		GL11.glScaled(scale, scale, scale);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return TEXTURE;
	}
}
