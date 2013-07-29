/*******************************************************************************
 * GuiHandler.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

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
		EntityBase entity = (EntityBase)Logic.getEntityOfTypeAtXYZ(EntityBase.class, world, x, y, z);

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
			EntityBase entity = (EntityBase)Logic.getEntityOfTypeAtXYZ(EntityBase.class, world, x, y, z);
			return new GuiInventory(entity, player.inventory, entity.inventory, false);
		}

		else if (id == MCA.instance.guiGameOverID)
		{
			return new GuiGameOver(player);
		}

		else if (id == MCA.instance.guiInteractionPlayerChildID)
		{
			EntityPlayerChild entity = (EntityPlayerChild)Logic.getEntityOfTypeAtXYZ(EntityPlayerChild.class, world, x, y, z);
			return new GuiInteractionPlayerChild(entity, player);
		}

		else if (id == MCA.instance.guiInteractionSpouseID)
		{
			EntityVillagerAdult entity = (EntityVillagerAdult)Logic.getEntityOfTypeAtXYZ(EntityVillagerAdult.class, world, x, y, z);
			return new GuiInteractionSpouse(entity, player);
		}

		else if (id == MCA.instance.guiInteractionVillagerAdultID)
		{
			EntityVillagerAdult entity = (EntityVillagerAdult)Logic.getEntityOfTypeAtXYZ(EntityVillagerAdult.class, world, x, y, z);
			return new GuiInteractionVillagerAdult(entity, player);
		}

		else if (id == MCA.instance.guiInteractionVillagerChildID)
		{
			EntityChild entity = (EntityChild)Logic.getEntityOfTypeAtXYZ(EntityChild.class, world, x, y, z);
			return new GuiInteractionVillagerChild(entity, player);
		}

		else if (id == MCA.instance.guiNameChildID)
		{
			WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);
			return new GuiNameChild(player, manager.worldProperties.babyGender);
		}

		else if (id == MCA.instance.guiSetupID)
		{
			EntityBase entity = (EntityBase)Logic.getEntityOfTypeAtXYZ(EntityBase.class, world, x, y, z);
			
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
			return new GuiSpecialDivorceCouple(player);
		}

		else if (id == MCA.instance.guiTombstoneID)
		{
			TileEntityTombstone tombstone = (TileEntityTombstone)world.getBlockTileEntity(x, y, z);
			return new GuiTombstone(tombstone);
		}

		else if (id == MCA.instance.guiVillagerEditorID)
		{
			EntityBase entity = (EntityBase)Logic.getEntityOfTypeAtXYZ(EntityBase.class, world, x, y, z);
			return new GuiVillagerEditor(entity, player);
		}
		
		else if (id == MCA.instance.guiLostRelativeDocumentID)
		{
			EntityVillagerAdult entity = (EntityVillagerAdult)Logic.getEntityOfTypeAtXYZ(EntityVillagerAdult.class, world, x, y, z);
			return new GuiLostRelativeDocument(player, entity);
		}

		else
		{
			return null;
		}
	}
}
