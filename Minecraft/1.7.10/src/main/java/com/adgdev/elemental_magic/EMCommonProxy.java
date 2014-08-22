package com.adgdev.elemental_magic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class EMCommonProxy implements IGuiHandler
{
	public void registerRenderInformation() //Client side texture registering
	{
		
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) { //For GUI's
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) { //For GUI's
		return null;
	}

	public void registerTiles()
	{ //For registering TileEntities
		
	}

	public void registerBlocks()
	{ //For registering Blocks
		
	}

	public void registerItems()
	{ //For registering Items

	}
}