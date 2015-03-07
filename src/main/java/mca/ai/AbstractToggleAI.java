package mca.ai;

import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import radixcore.helpers.LogicHelper;

public abstract class AbstractToggleAI extends AbstractAI
{	
	protected String assigningPlayer = "none";
	
	public AbstractToggleAI(EntityHuman owner)
	{
		super(owner);
	}
	
	public abstract void setIsActive(boolean value);
	
	public abstract boolean getIsActive();
	
	public final EntityPlayer getAssigningPlayer()
	{
		return LogicHelper.getPlayerByUUID(assigningPlayer, owner.worldObj);
	}
	
	public final void notifyAssigningPlayer(String message)
	{
		final EntityPlayer player = getAssigningPlayer();
		
		if (player != null)
		{
			player.addChatMessage(new ChatComponentText(message));
		}
	}
}
