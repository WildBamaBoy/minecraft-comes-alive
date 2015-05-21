package mca.core.forge;

import radixcore.util.BlockHelper;
import mca.client.gui.GuiNameBaby;
import mca.client.gui.GuiSetup;
import mca.client.gui.GuiTombstone;
import mca.core.Constants;
import mca.entity.EntityHuman;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * Handles GUIs client and server side.
 */
public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int posX, int posY, int posZ)
	{
		return null;
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
		case Constants.GUI_ID_TOMBSTONE:
			return new GuiTombstone((TileTombstone)BlockHelper.getTileEntity(world, posX, posY, posZ));
		default: return null;
		}
	}
}
