/*******************************************************************************
 * GuiHandler.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import mca.client.gui.GuiDivorceCouple;
import mca.client.gui.GuiGameOver;
import mca.client.gui.GuiInteractionPlayerChild;
import mca.client.gui.GuiInteractionSpouse;
import mca.client.gui.GuiInteractionVillagerAdult;
import mca.client.gui.GuiInteractionVillagerChild;
import mca.client.gui.GuiInventory;
import mca.client.gui.GuiLostRelativeDocument;
import mca.client.gui.GuiNameChild;
import mca.client.gui.GuiSetup;
import mca.client.gui.GuiTombstone;
import mca.client.gui.GuiVillagerEditor;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LogicHelper;
import mca.entity.AbstractEntity;
import mca.entity.EntityChild;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.inventory.ContainerInventory;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * Handles GUIs client and server side.
 */
public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
	{
		AbstractEntity entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);

		if (id == Constants.ID_GUI_INVENTORY)
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
		AbstractEntity entity;
		
		switch (id)
		{
		case Constants.ID_GUI_INVENTORY:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);
			return new GuiInventory(entity, player.inventory, entity.inventory, false);
			
		case Constants.ID_GUI_GAMEOVER:
			return new GuiGameOver(player);
			
		case Constants.ID_GUI_PLAYERCHILD:
			entity = (EntityPlayerChild)LogicHelper.getEntityOfTypeAtXYZ(EntityPlayerChild.class, world, x, y, z);
			return new GuiInteractionPlayerChild((EntityPlayerChild) entity, player);
			
		case Constants.ID_GUI_SPOUSE:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);
			return new GuiInteractionSpouse(entity, player);
			
		case Constants.ID_GUI_ADULT:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);
			return new GuiInteractionVillagerAdult(entity, player);
			
		case Constants.ID_GUI_CHILD:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(EntityChild.class, world, x, y, z);
			return new GuiInteractionVillagerChild((EntityChild) entity, player);
			
		case Constants.ID_GUI_NAMECHILD:
			WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);
			return new GuiNameChild(player, manager.worldProperties.babyIsMale);
			
		case Constants.ID_GUI_SETUP:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);
			if (entity != null && entity.profession == 1) return new GuiSetup(player, true);
			else return new GuiSetup(player, false);
			
		case Constants.ID_GUI_DIVORCECOUPLE:
			return new GuiDivorceCouple(player);
			
		case Constants.ID_GUI_TOMBSTONE:
			TileEntityTombstone tombstone = (TileEntityTombstone)world.getBlockTileEntity(x, y, z);
			return new GuiTombstone(tombstone);
			
		case Constants.ID_GUI_EDITOR:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);
			return new GuiVillagerEditor(entity, player);
			
		case Constants.ID_GUI_LOSTRELATIVE:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(EntityVillagerAdult.class, world, x, y, z);
			return new GuiLostRelativeDocument(player, entity);
			
		default:
			throw new IllegalArgumentException("Unknown GUI ID.");
		}
	}
}
