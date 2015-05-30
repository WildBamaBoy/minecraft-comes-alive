package mca.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiButtonPatreon extends GuiButton
{
	private static ResourceLocation texture = new ResourceLocation("mca:textures/patreon.png");
	
	public GuiButtonPatreon(int id, int x, int y)
	{
		super(id, x, y, 107, 27, "");
	}

	@Override
	public void drawButton(Minecraft mc, int posX, int posY) 
	{
		if (this.visible)
        {
            mc.getTextureManager().bindTexture(texture);
            
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = posX >= this.xPosition && posY >= this.yPosition && posX < this.xPosition + this.width && posY < this.yPosition + this.height;
            int hoverState = this.getHoverState(this.hovered);
            
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            int v = hoverState == 1 ? 0 : 28;
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, v, this.width, this.height);

            this.mouseDragged(mc, posX, posY);
        }
	}
}
