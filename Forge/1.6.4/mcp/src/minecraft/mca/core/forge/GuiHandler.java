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

		if (id == MCA.instance.guiInventoryID)
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
		if (id == MCA.instance.guiInventoryID)
		{
			AbstractEntity entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);
			return new GuiInventory(entity, player.inventory, entity.inventory, false);
		}

		else if (id == MCA.instance.guiGameOverID)
		{
			return new GuiGameOver(player);
		}

		else if (id == MCA.instance.guiInteractionPlayerChildID)
		{
			EntityPlayerChild entity = (EntityPlayerChild)LogicHelper.getEntityOfTypeAtXYZ(EntityPlayerChild.class, world, x, y, z);
			return new GuiInteractionPlayerChild(entity, player);
		}

		else if (id == MCA.instance.guiInteractionSpouseID)
		{
			AbstractEntity entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);
			return new GuiInteractionSpouse(entity, player);
		}

		else if (id == MCA.instance.guiInteractionVillagerAdultID)
		{
			AbstractEntity entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);
			return new GuiInteractionVillagerAdult(entity, player);
		}

		else if (id == MCA.instance.guiInteractionVillagerChildID)
		{
			EntityChild entity = (EntityChild)LogicHelper.getEntityOfTypeAtXYZ(EntityChild.class, world, x, y, z);
			return new GuiInteractionVillagerChild(entity, player);
		}

		else if (id == MCA.instance.guiNameChildID)
		{
			WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);
			return new GuiNameChild(player, manager.worldProperties.babyGender);
		}

		else if (id == MCA.instance.guiSetupID)
		{
			AbstractEntity entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);
			
			if (entity != null)
			{
				if (entity.profession == 1)
				{
					return new GuiSetup(player, true);
				}
				
				else
				{
					return new GuiSetup(player, false);
				}
			}
			
			else
			{
				return new GuiSetup(player, false);
			}
		}

		else if (id == MCA.instance.guiSpecialDivorceCoupleID)
		{
			return new GuiDivorceCouple(player);
		}

		else if (id == MCA.instance.guiTombstoneID)
		{
			TileEntityTombstone tombstone = (TileEntityTombstone)world.getBlockTileEntity(x, y, z);
			return new GuiTombstone(tombstone);
		}

		else if (id == MCA.instance.guiVillagerEditorID)
		{
			AbstractEntity entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, x, y, z);
			return new GuiVillagerEditor(entity, player);
		}
		
		else if (id == MCA.instance.guiLostRelativeDocumentID)
		{
			EntityVillagerAdult entity = (EntityVillagerAdult)LogicHelper.getEntityOfTypeAtXYZ(EntityVillagerAdult.class, world, x, y, z);
			return new GuiLostRelativeDocument(player, entity);
		}

		else
		{
			return null;
		}
	}
}
