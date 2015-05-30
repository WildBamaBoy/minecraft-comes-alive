package mca.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.client.render.RenderHelper;

public final class TutorialMessage implements Serializable
{
	public final String line1;
	public final String line2;
	
	public TutorialMessage(String line1, String line2)
	{
		this.line1 = line1;
		this.line2 = line2;
	}
	
	@SideOnly(Side.CLIENT)
	public void draw(int animationProgress)
	{
		if (line2.isEmpty())
		{
			RenderHelper.drawTextPopup(line1, 4, -20 + animationProgress);
		}
		
		else
		{
			List<String> text = new ArrayList<String>();
			text.add(line1);
			text.add(line2);
			
			RenderHelper.drawTextPopup(text, 4, -20 + animationProgress);
		}
	}
}
