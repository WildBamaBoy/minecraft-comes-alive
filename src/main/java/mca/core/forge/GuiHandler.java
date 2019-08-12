package mca.core.forge;

import mca.client.gui.*;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int entityId, int posY, int posZ) {
        switch (guiId) {
            case Constants.GUI_ID_INVENTORY:
                EntityVillagerMCA villager = (EntityVillagerMCA) world.getEntityByID(entityId);
                if (villager == null || villager.inventory == null) return null;
                return new ContainerChest(player.inventory, villager.inventory, player);
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int entityId, int unused1, int unused2) {
        switch (guiId) {
            case Constants.GUI_ID_INVENTORY:
                Entity entity = world.getEntityByID(entityId);
                if (entity == null) return null;
                return new GuiChest(player.inventory, ((EntityVillagerMCA) entity).inventory);
            case Constants.GUI_ID_INTERACT:
                return new GuiInteract((EntityVillagerMCA) world.getEntityByID(entityId), player);
            case Constants.GUI_ID_NAMEBABY:
                return new GuiNameBaby(player, player.inventory.getStackInSlot(player.inventory.currentItem));
            case Constants.GUI_ID_STAFFOFLIFE:
                return new GuiStaffOfLife(player);
            case Constants.GUI_ID_VILLAGEREDITOR:
                return new GuiVillagerEditor((EntityVillagerMCA) world.getEntityByID(entityId), player);
            case Constants.GUI_ID_GUIDEBOOK:
                return new GuiScreenBook(player, player.inventory.getCurrentItem(), false);
            case Constants.GUI_ID_WHISTLE:
                return new GuiWhistle();
            default:
                MCA.getLog().fatal("Failed to handle provided GUI ID on client: " + guiId);
                return null;
        }
    }
}