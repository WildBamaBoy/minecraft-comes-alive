package mca.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Defines an inventory slot that contains armor.
 * 
 * @author ko2fan
 */
class SlotArmor extends Slot
{
	/**
	 * The armor type that can be placed on that slot, it uses the same values of armorType field on ItemArmor.
	 */
	final int armorType;

	/**
	 * The parent class of this slot, ContainerInventory, SlotArmor is an Anon inner class.
	 */
	final ContainerInventory parent;

	/**
	 * Constructor
	 * 
	 * @param playerContainer The parent container of this slot.
	 * @param inventory The inventory this slot will be part of.
	 * @param slotIndex The slot's index.
	 * @param posX The slot's X position.
	 * @param posY The slot's Y position.
	 * @param armorType The armor type this armor slot will contain.
	 */
	SlotArmor(ContainerInventory playerContainer, IInventory inventory, int slotIndex, int posX, int posY, int armorType)
	{
		super(inventory, slotIndex, posX, posY);
		parent = playerContainer;
		this.armorType = armorType;
	}

	/**
	 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case of armor slots)
	 */
	@Override
	public int getSlotStackLimit()
	{
		return 1;
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	 */
	@Override
	public boolean isItemValid(ItemStack itemStack)
	{
		final Item item = itemStack == null ? null : itemStack.getItem();
		return item != null && item.isValidArmor(itemStack, armorType, parent.entity);
	}

	/**
	 * Returns the icon name on items.png that is used as background image of the slot.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public String func_178171_c()
	{
		return ItemArmor.EMPTY_SLOT_NAMES[armorType];
	}
}