/*******************************************************************************
 * GuiHandler.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import mca.client.gui.GuiDivorceCouple;
import mca.client.gui.GuiHardcoreGameOver;
import mca.client.gui.GuiInteractionPlayer;
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
import mca.entity.AbstractChild;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.enums.EnumRelation;
import mca.inventory.ContainerInventory;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.radixshock.radixcore.file.WorldPropertiesManager;
import com.radixshock.radixcore.logic.LogicHelper;

import cpw.mods.fml.common.network.IGuiHandler;

/**
 * Handles GUIs client and server side.
 */
public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int posX, int posY, int posZ) 
	{
		final AbstractEntity entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, posX, posY, posZ);

		if (guiId == Constants.ID_GUI_INVENTORY)
		{
			return new ContainerInventory(player.inventory, entity.inventory, entity);
		}

		else
		{
			return null;
		}
	}

	@Override
	public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int posX, int posY, int posZ) 
	{
		AbstractEntity entity;

		switch (guiId)
		{
		case Constants.ID_GUI_INVENTORY:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, posX, posY, posZ);
			return new GuiInventory(entity, player.inventory, entity.inventory, false);

		case Constants.ID_GUI_GAMEOVER:
			return new GuiHardcoreGameOver(player);

		case Constants.ID_GUI_PCHILD:
			entity = (EntityPlayerChild)LogicHelper.getEntityOfTypeAtXYZ(EntityPlayerChild.class, world, posX, posY, posZ);
			return new GuiInteractionPlayerChild((EntityPlayerChild) entity, player);

		case Constants.ID_GUI_SPOUSE:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, posX, posY, posZ);
			final EnumRelation relationOfPlayer = entity.familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player));

			if (relationOfPlayer == EnumRelation.Spouse)
			{
				return new GuiInteractionSpouse(entity, player);
			}
			
			return null;
			
		case Constants.ID_GUI_ADULT:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, posX, posY, posZ);
			return new GuiInteractionVillagerAdult(entity, player);

		case Constants.ID_GUI_VCHILD:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractChild.class, world, posX, posY, posZ);
			return new GuiInteractionVillagerChild((AbstractChild) entity, player);

		case Constants.ID_GUI_NAMECHILD:
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
			return new GuiNameChild(player, MCA.getInstance().getWorldProperties(manager).babyIsMale);

		case Constants.ID_GUI_SETUP:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, posX, posY, posZ);
			return new GuiSetup(player, entity != null && entity.profession == 1);

		case Constants.ID_GUI_DIVORCE:
			return new GuiDivorceCouple(player);

		case Constants.ID_GUI_TOMBSTONE:
			return new GuiTombstone((TileEntityTombstone)world.getTileEntity(posX, posY, posZ));

		case Constants.ID_GUI_EDITOR:
			return new GuiVillagerEditor((AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(AbstractEntity.class, world, posX, posY, posZ), player);

		case Constants.ID_GUI_LRD:
			entity = (AbstractEntity)LogicHelper.getEntityOfTypeAtXYZ(EntityVillagerAdult.class, world, posX, posY, posZ);
			return new GuiLostRelativeDocument(player, entity);

		case Constants.ID_GUI_PLAYER:
			return new GuiInteractionPlayer(player, (EntityPlayer)LogicHelper.getEntityOfTypeAtXYZ(EntityPlayer.class, world, posX, posY, posZ));
			
		default:
			throw new IllegalArgumentException("Unknown GUI ID.");
		}
	}
}
