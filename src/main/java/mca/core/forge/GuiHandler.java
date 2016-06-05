package mca.core.forge;

import mca.client.gui.GuiInteraction;
import mca.client.gui.GuiInventory;
import mca.client.gui.GuiNameBaby;
import mca.client.gui.GuiSetup;
import mca.client.gui.GuiTombstone;
import mca.client.gui.GuiVillagerEditor;
import mca.client.gui.GuiWhistle;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityHuman;
import mca.inventory.ContainerInventory;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import radixcore.util.BlockHelper;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;

/**
 * Handles GUIs client and server side.
 */
public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int posX, int posY, int posZ)
	{
		final EntityHuman entity = (EntityHuman) RadixLogic.getEntityOfTypeAtXYZ(EntityHuman.class, world, posX, posY, posZ);

		if (guiId == Constants.GUI_ID_INVENTORY)
		{
			return new ContainerInventory(player.inventory, entity.getVillagerInventory(), entity);
		}

		else
		{
			return null;
		}
	}

	@Override
	public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int posX, int posY, int posZ)
	{
		EntityHuman entity;
		
		switch (guiId)
		{
		case Constants.GUI_ID_NAMEBABY:
			return new GuiNameBaby(player, false);
		case Constants.GUI_ID_SETUP:
			return new GuiSetup(player);
		case Constants.GUI_ID_WHISTLE:
			return new GuiWhistle(player);
		case Constants.GUI_ID_TOMBSTONE:
			return new GuiTombstone((TileTombstone)BlockHelper.getTileEntity(world, posX, posY, posZ));
		case Constants.GUI_ID_INTERACT: 
			entity = (EntityHuman) RadixLogic.getEntityOfTypeAtXYZ(EntityHuman.class, world, posX, posY, posZ);
			return new GuiInteraction(entity, player);
		case Constants.GUI_ID_EDITOR: 
			entity = (EntityHuman) RadixLogic.getEntityOfTypeAtXYZ(EntityHuman.class, world, posX, posY, posZ);
			return new GuiVillagerEditor(entity, player);
		case Constants.GUI_ID_INVENTORY: 
			entity = (EntityHuman) RadixLogic.getEntityOfTypeAtXYZ(EntityHuman.class, world, posX, posY, posZ);
			return new GuiInventory(entity, player.inventory, entity.getVillagerInventory(), false);
		default: 
			MCA.getLog().fatal("Failed to handle provided GUI ID: " + guiId +". This is a programming error, please report!");
			return null;
		}
	}
}
