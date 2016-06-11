package mca.client.render;

import org.lwjgl.opengl.GL11;

import mca.client.model.ModelGrimReaper;
import mca.entity.EntityGrimReaper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.ResourceLocation;

public class RenderGrimReaper<T extends EntityGrimReaper> extends RenderBiped<T>
{
	private static final ResourceLocation TEXTURE = new ResourceLocation("mca:textures/grimreaper.png");

	public RenderGrimReaper() 
	{
		super(Minecraft.getMinecraft().getRenderManager(), new ModelGrimReaper(), 0.5F);
	}

	@Override
	public void doRender(T entity, double posX, double posY, double posZ, float angle, float offsetY) 
	{
		super.doRender(entity, posX, posY, posZ, angle, offsetY);
		BossStatus.setBossStatus((IBossDisplayData) entity, false);
	}

	@Override
	protected void preRenderCallback(T entity, float partialTickTime) 
	{
		super.preRenderCallback(entity, partialTickTime);
		
		double scale = 1.3D;
		GL11.glScaled(scale, scale, scale);
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity)
	{
		return TEXTURE;
	}
}
