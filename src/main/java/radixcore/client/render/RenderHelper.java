package radixcore.client.render;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RenderHelper 
{
	public static void drawTexturedRectangle(ResourceLocation texture, int x, int y, int u, int v, int width, int height)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		float f = 0.00390625F;
		float f1 = 0.00390625F;

		final Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x + 0, 		y + height, 0.0D, (u + 0) 		* f, ((v + height) * f1));
		tessellator.addVertexWithUV(x + width, 	y + height, 0.0D, (u + width) 	* f, ((v + height) * f1));
		tessellator.addVertexWithUV(x + width, 	y + 0, 		0.0D, (u + width) 	* f, ((v + 0) * f1));
		tessellator.addVertexWithUV(x + 0, 		y + 0, 		0.0D, (u + 0) 		* f, ((v + 0) * f1));

		tessellator.draw();
	}

	public static void drawTextPopup(String text, int posX, int posY)
	{
		int k = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
		int i1 = 8;
		int color = 0xFEFFFEE * -1;

		drawGradientRect(posX - 3, posY - 4, posX + k + 3, posY - 3, color, color);
		drawGradientRect(posX - 3, posY + i1 + 3, posX + k + 3, posY + i1 + 4, color, color);
		drawGradientRect(posX - 3, posY - 3, posX + k + 3, posY + i1 + 3, color, color);
		drawGradientRect(posX - 4, posY - 3, posX - 3, posY + i1 + 3, color, color);
		drawGradientRect(posX + k + 3, posY - 3, posX + k + 4, posY + i1 + 3, color, color);

		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, posX, posY, 0xFFFFFF);

		int borderColor = 0x505000FF;
		int borderShade = (borderColor & 0xFEFEFE) >> 1 | borderColor & color;
		drawGradientRect(posX - 3, posY - 3 + 1, posX - 3 + 1, posY + i1 + 3 - 1, borderColor, borderShade);
		drawGradientRect(posX + k + 2, posY - 3 + 1, posX + k + 3, posY + i1 + 3 - 1, borderColor, borderShade);
		drawGradientRect(posX - 3, posY - 3, posX + k + 3, posY - 3 + 1, borderColor, borderColor);
		drawGradientRect(posX - 3, posY + i1 + 2, posX + k + 3, posY + i1 + 3, borderShade, borderShade);
	}

	public static void drawTextPopup(List<String> textList, int posX, int posY)
	{
		int longestTextLength = 0;
		
		int modY = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2 * textList.size();
		
		for (String text : textList)
		{
			int textLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
			
			if (textLength > longestTextLength)
			{
				longestTextLength = textLength;
			}
		}
		
		int padding = 8;
		int color = 0xFEFFFEE * -1;

		drawGradientRect(posX - 3, posY - 4, posX + longestTextLength + 3, posY - 3 + modY, color, color);
		drawGradientRect(posX - 3, posY + padding + 3, posX + longestTextLength + 3, posY + padding + 4 + modY, color, color);
		drawGradientRect(posX - 3, posY - 3, posX + longestTextLength + 3, posY + padding + 3 + modY, color, color);
		drawGradientRect(posX - 4, posY - 3, posX - 3, posY + padding + 3+ modY, color, color);
		drawGradientRect(posX + longestTextLength + 3, posY - 3, posX + longestTextLength + 4, posY + padding + 3 + modY, color, color);

		for (int i = 0; i < textList.size(); i++)
		{
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(textList.get(i), posX, posY + (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * i), 0xFFFFFF);
		}

		int borderColor = 0x505000FF;
		int borderShade = (borderColor & 0xFEFEFE) >> 1 | borderColor & color;
		drawGradientRect(posX - 3, posY - 3 + 1, posX - 3 + 1, posY + padding + 3 - 1 + modY, borderColor, borderShade);
		drawGradientRect(posX + longestTextLength + 2, posY - 3 + 1, posX + longestTextLength + 3, posY + padding + 3 - 1 + modY, borderColor, borderShade);
		drawGradientRect(posX - 3, posY - 3, posX + longestTextLength + 3, posY - 3 + 1, borderColor, borderColor);
		drawGradientRect(posX - 3, posY + padding + 2 + modY, posX + longestTextLength + 3, posY + padding + 3 + modY, borderShade, borderShade);
	}
	
	public static void drawGradientRect(int xTop, int xBottom, int yTop, int yBottom, int color1, int color2)
	{
		float color1A = (color1 >> 24 & 255) / 255.0F;
		float color1R = (color1 >> 16 & 255) / 255.0F;
		float color1B = (color1 >> 8 & 255) / 255.0F;
		float color1G = (color1 & 255) / 255.0F;
		float color2A = (color2 >> 24 & 255) / 255.0F;
		float color2R = (color2 >> 16 & 255) / 255.0F;
		float color2B = (color2 >> 8 & 255) / 255.0F;
		float color2G = (color2 & 255) / 255.0F;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(color1R, color1B, color1G, color1A);
		tessellator.addVertex(yTop, xBottom, 0.0D);
		tessellator.addVertex(xTop, xBottom, 0.0D);
		tessellator.setColorRGBA_F(color2R, color2B, color2G, color2A);
		tessellator.addVertex(xTop, yBottom, 0.0D);
		tessellator.addVertex(yTop, yBottom, 0.0D);
		tessellator.draw();
		
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private RenderHelper()
	{
	}
}
