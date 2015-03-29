package mca.ai;

import mca.entity.EntityHuman;
import mca.enums.EnumPersonality;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import radixcore.util.RadixLogic;

public abstract class AbstractToggleAI extends AbstractAI
{	
	protected String assigningPlayer = "none";
	
	public AbstractToggleAI(EntityHuman owner)
	{
		super(owner);
	}
	
	public abstract void setIsActive(boolean value);
	
	public abstract boolean getIsActive();
	
	protected abstract String getName();
	
	public final EntityPlayer getAssigningPlayer()
	{
		return RadixLogic.getPlayerByUUID(assigningPlayer, owner.worldObj);
	}
	
	public final void notifyAssigningPlayer(String message)
	{
		final EntityPlayer player = getAssigningPlayer();
		
		if (player != null)
		{
			player.addChatMessage(new ChatComponentText(message));
		}
	}
	
	protected final boolean addItemStackToInventory(ItemStack stack)
	{
		if (owner.getPersonality() == EnumPersonality.CURIOUS && RadixLogic.getBooleanWithProbability(10))
		{
			owner.getInventory().addItemStackToInventory(stack);
			return owner.getInventory().addItemStackToInventory(stack.copy());
		}
		
		else if (owner.getPersonality() == EnumPersonality.GREEDY && RadixLogic.getBooleanWithProbability(10))
		{
			return false;
		}
		
		else if (owner.getPersonality() != EnumPersonality.GREEDY)
		{
			return owner.getInventory().addItemStackToInventory(stack);
		}
		
		return false;
	}
}
