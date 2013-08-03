/*******************************************************************************
 * GuiHandlerInventory.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import mca.entity.AbstractEntity;
import mca.inventory.ContainerInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * Handles the inventory GUI.
 */
public class GuiHandlerInventory implements IGuiHandler
{
	/** The ID of the entity whose inventory is being edited. */
	private int entityId;
	
	/**
	 * Constructor
	 * 
	 * @param 	entityId	The ID of the entity whose inventory is being edited.
	 */
	public GuiHandlerInventory(int entityId)
	{
		this.entityId = entityId;
	}
	
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
	{
		AbstractEntity entity = (AbstractEntity)world.getEntityByID(entityId);
		
		if (id == 0)
		{
			return new ContainerInventory(player.inventory, entity.inventory);
		}
		
		else
		{
			return null;
		}
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
	{
		AbstractEntity entity = (AbstractEntity)world.getEntityByID(entityId);
		
		if (id == 0)
		{
			return new GuiInventory(entity, player.inventory, entity.inventory, false);
		}
		
		else
		{
			return null;
		}
	}
}
