/*******************************************************************************
 * RenderFishHook.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.render;

import mca.core.MCA;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityPlayerChild;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Determines how the fish hook is rendered.
 */
@SideOnly(Side.CLIENT)
public class RenderFishHook extends Render
{
	private static final ResourceLocation field_110792_a = new ResourceLocation("textures/particle/particles.png");
	
	/**
	 * Renders the fish hook in the world.
	 * 
	 * @param 	entityFishHook	The fish hook being rendered.
	 * @param 	posX			The x position the hook is being rendered at.
	 * @param 	posY			The y position the hook is being rendered at.
	 * @param 	posZ			The z position the hook is being rendered at.
	 * @param 	angle			The angle relative to the angler that the hook is rendered at.
	 * @param 	offsetY			The y offset of the hook.
	 */
    public void doRenderFishHook(EntityChoreFishHook entityFishHook, double posX, double posY, double posZ, float angle, float offsetY)
    {	    	
        GL11.glPushMatrix();
        GL11.glTranslatef((float)posX, (float)posY, (float)posZ);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.5F, 0.5F, 0.5F);

        this.func_110777_b(entityFishHook);
        Tessellator tessellator = Tessellator.instance;

        float textureSizeU = (float)(1 * 8 + 0) / 128.0F;
        float textureSizeV = (float)(1 * 8 + 8) / 128.0F;
        float textureLocationX = (float)(2 * 8 + 0) / 128.0F;
        float textureLocationY = (float)(2 * 8 + 8) / 128.0F;

        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV((double)(-0.5F), (double)(-0.5F), 0.0D, (double)textureSizeU, (double)textureLocationY);
        tessellator.addVertexWithUV((double)(0.5F), (double)(-0.5F), 0.0D, (double)textureSizeV, (double)textureLocationY);
        tessellator.addVertexWithUV((double)(0.5F), (double)(0.5F), 0.0D, (double)textureSizeV, (double)textureLocationX);
        tessellator.addVertexWithUV((double)(-0.5F), (double)(0.5F), 0.0D, (double)textureSizeU, (double)textureLocationX);
        tessellator.draw();
        
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();

        if (entityFishHook.angler != null)
        {
            float swingProgress = entityFishHook.angler.getSwingProgress(offsetY);
            float orientation = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
            
            Vec3 fishHookVector = entityFishHook.worldObj.getWorldVec3Pool().getVecFromPool(-0.5D, 0.03D, 0.8D);
            fishHookVector.rotateAroundX(-(entityFishHook.angler.prevRotationPitch + (entityFishHook.angler.rotationPitch - entityFishHook.angler.prevRotationPitch) * offsetY) * (float)Math.PI / 180.0F);
            fishHookVector.rotateAroundY(-(entityFishHook.angler.prevRotationYaw + (entityFishHook.angler.rotationYaw - entityFishHook.angler.prevRotationYaw) * offsetY) * (float)Math.PI / 180.0F);
            fishHookVector.rotateAroundY(orientation * 0.5F);
            fishHookVector.rotateAroundX(-orientation * 0.7F);
            
            double anglerCorrectionX = entityFishHook.angler.prevPosX + (entityFishHook.angler.posX - entityFishHook.angler.prevPosX) * (double)offsetY + fishHookVector.xCoord;
            double anglerCorrectionY = entityFishHook.angler.prevPosY + (entityFishHook.angler.posY - entityFishHook.angler.prevPosY) * (double)offsetY + fishHookVector.yCoord;
            double anglerCorrectionZ = entityFishHook.angler.prevPosZ + (entityFishHook.angler.posZ - entityFishHook.angler.prevPosZ) * (double)offsetY + fishHookVector.zCoord;
            double eyeHeight = (double)entityFishHook.angler.getEyeHeight();
            
            if (this.renderManager.options.thirdPersonView > 0)
            {
                float offsetYaw = (entityFishHook.angler.prevRenderYawOffset + (entityFishHook.angler.renderYawOffset - entityFishHook.angler.prevRenderYawOffset) * offsetY) * (float)Math.PI / 180.0F;
                double sinOffsetYaw = (double)MathHelper.sin(offsetYaw);
                double cosOffsetYaw = (double)MathHelper.cos(offsetYaw);
                
                anglerCorrectionX = entityFishHook.angler.prevPosX + (entityFishHook.angler.posX - entityFishHook.angler.prevPosX) * (double)offsetY - cosOffsetYaw * 0.35D - sinOffsetYaw * 0.85D;
                anglerCorrectionY = entityFishHook.angler.prevPosY + eyeHeight + (entityFishHook.angler.posY - entityFishHook.angler.prevPosY) * (double)offsetY - 0.45D;
                anglerCorrectionZ = entityFishHook.angler.prevPosZ + (entityFishHook.angler.posZ - entityFishHook.angler.prevPosZ) * (double)offsetY - sinOffsetYaw * 0.35D + cosOffsetYaw * 0.85D;
            }

            double fishHookCorrectionX = 0;
            double fishHookCorrectionY = 0;
            double fishHookCorrectionZ = 0;
            
            if (entityFishHook.angler instanceof EntityPlayerChild)
            {
            	int age = ((EntityPlayerChild)entityFishHook.angler).age;
            	float scale = 0.7F + ((0.2375F / MCA.instance.modPropertiesManager.modProperties.kidGrowUpTimeMinutes) * age);
            	
            	fishHookCorrectionX = entityFishHook.prevPosX + (entityFishHook.posX - entityFishHook.prevPosX) * (double)offsetY;
            	fishHookCorrectionY = entityFishHook.prevPosY + (entityFishHook.posY - entityFishHook.prevPosY) * (double)offsetY - scale;
            	fishHookCorrectionZ = entityFishHook.prevPosZ + (entityFishHook.posZ - entityFishHook.prevPosZ) * (double)offsetY;
            }
            
            else
            {
            	fishHookCorrectionX = entityFishHook.prevPosX + (entityFishHook.posX - entityFishHook.prevPosX) * (double)offsetY;
            	fishHookCorrectionY = entityFishHook.prevPosY + (entityFishHook.posY - entityFishHook.prevPosY) * (double)offsetY - 0.9375D;
            	fishHookCorrectionZ = entityFishHook.prevPosZ + (entityFishHook.posZ - entityFishHook.prevPosZ) * (double)offsetY;
            }
            
            double deltaX = (double)((float)(anglerCorrectionX - fishHookCorrectionX));
            double deltaY = (double)((float)(anglerCorrectionY - fishHookCorrectionY));
            double deltaZ = (double)((float)(anglerCorrectionZ - fishHookCorrectionZ));
            
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            tessellator.startDrawing(3);
            tessellator.setColorOpaque_I(0);

            for (int i = 0; i <= 16; ++i)
            {
                float quotient = (float)i / (float)16;
                tessellator.addVertex(posX + deltaX * (double)quotient, posY + deltaY * (double)(quotient * quotient + quotient) * 0.5D + 0.25D, posZ + deltaZ * (double)quotient);
            }

            tessellator.draw();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    @Override
    public void doRender(Entity entity, double posX, double posY, double posZ, float f1, float f2)
    {
        this.doRenderFishHook((EntityChoreFishHook)entity, posX, posY, posZ, f1, f2);
    }

    protected ResourceLocation func_110791_a(EntityChoreFishHook par1EntityFishHook)
    {
        return field_110792_a;
    }
    
	@Override
	protected ResourceLocation func_110775_a(Entity entity) 
	{
		return this.func_110791_a((EntityChoreFishHook)entity);
	}
}
