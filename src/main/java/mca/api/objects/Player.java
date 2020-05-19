package mca.api.objects;

import lombok.Getter;
import mca.api.wrappers.WorldWrapper;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Player extends NPC {
	@Getter private EntityPlayer player;
	public InventoryPlayer inventory;
	public WorldWrapper world;
	
	public Player(EntityPlayer player) {
		super(player);
		this.player = player;
		this.world = new WorldWrapper(player.world);
		this.inventory = player.inventory;
	}
	
	public void displayVillagerTradeGui(EntityVillagerMCA villager) {
		player.displayVillagerTradeGui(villager);
	}

	public void openGui(MCA instance, int modGuiId, World world, int x, int y, int z) {
		player.openGui(instance, modGuiId, world, x, y, z);
	}

	public void addItemStackToInventory(ItemStack stack) {
		player.addItemStackToInventory(stack);
	}

    public boolean isCreative() {
		return player.isCreative();
    }
}
