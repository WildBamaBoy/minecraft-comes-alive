package com.adgdev.elemental_magic.content.commands;

import com.adgdev.elemental_magic.EM;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.ChatComponentText;

public class CommandTalk extends ICommand
{
	@Override
	public String getCommandName()
	{
		return "talk";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender)
	{
		return "Talk to the game!";
	}

	@Override
	public void ProcessPlayer(EntityPlayer player, String[] params)
	{
		try {
			player.addChatMessage(new ChatComponentText(EM.bot.getRespond(params[1])));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void ProcessCommandBlock(TileEntityCommandBlock commandBlock, String[] params)
	{
		
	}

	@Override
	public void ProcessServerConsole(ICommandSender console, String[] params)
	{
		
	}
}