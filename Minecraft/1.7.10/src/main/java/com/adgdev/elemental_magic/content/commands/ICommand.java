package com.adgdev.elemental_magic.content.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityCommandBlock;

public abstract class ICommand extends CommandBase
{
	public abstract void ProcessPlayer(EntityPlayer player, String[] params);
	public abstract void ProcessCommandBlock(TileEntityCommandBlock commandBlock, String[] params);
	public abstract void ProcessServerConsole(ICommandSender console, String[] params);
	
	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring)
	{
		if(icommandsender instanceof EntityPlayer) // If the sender is a player
		{
			ProcessPlayer((EntityPlayer) icommandsender, astring); // Cast the sender into an EntityPlayer then call the ProcessPlayer method
		}
		else if(icommandsender instanceof TileEntityCommandBlock) // If the sender is a commandblock
		{
		    ProcessCommandBlock((TileEntityCommandBlock) icommandsender, astring);
		}
		else // If it's the Server console
		{
		    ProcessServerConsole(icommandsender, astring);
		}
	}
}