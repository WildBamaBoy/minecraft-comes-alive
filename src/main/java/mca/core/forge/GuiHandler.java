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
import mca.entity.EntityVillagerMCA;
import mca.inventory.ContainerInventory;
import mca.tile.TileTombstone;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import radixcore.modules.RadixLogic;

/**
 * Handles GUIs client and server side.
 */
public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int posX, int posY, int posZ)
	{
		final EntityVillagerMCA entity = (EntityVillagerMCA) RadixLogic.getEntityOfTypeAtXYZ(EntityVillagerMCA.class, world, posX, posY, posZ);

		if (guiId == Constants.GUI_ID_INVENTORY)
		{
			return new ContainerInventory(player.inventory, entity.attributes.getInventory(), entity);
		}

		else
		{
			return null;
		}
	}

	@Override
	public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int posX, int posY, int posZ)
	{
		EntityVillagerMCA entity;
		
		switch (guiId)
		{
		case Constants.GUI_ID_NAMEBABY:
			return new GuiNameBaby(player, false);
		case Constants.GUI_ID_SETUP:
			return new GuiSetup(player);
		case Constants.GUI_ID_WHISTLE:
			return new GuiWhistle(player);
		case Constants.GUI_ID_TOMBSTONE:
			return new GuiTombstone((TileTombstone)world.getTileEntity(new BlockPos(posX, posY, posZ)));
		case Constants.GUI_ID_INTERACT: 
			entity = (EntityVillagerMCA) RadixLogic.getEntityOfTypeAtXYZ(EntityVillagerMCA.class, world, posX, posY, posZ);
			return new GuiInteraction(entity, player);
		case Constants.GUI_ID_EDITOR: 
			entity = (EntityVillagerMCA) RadixLogic.getEntityOfTypeAtXYZ(EntityVillagerMCA.class, world, posX, posY, posZ);
			return new GuiVillagerEditor(entity, player);
		case Constants.GUI_ID_INVENTORY: 
			entity = (EntityVillagerMCA) RadixLogic.getEntityOfTypeAtXYZ(EntityVillagerMCA.class, world, posX, posY, posZ);
			return new GuiInventory(entity, player.inventory, entity.attributes.getInventory(), false);
		case Constants.GUI_ID_GUIDEBOOK:
			return new GuiScreenBook(player, player.inventory.getCurrentItem(), false);
		default: 
			MCA.getLog().fatal("Failed to handle provided GUI ID: " + guiId +". This is a programming error, please report!");
			return null;
		}
	}
}
