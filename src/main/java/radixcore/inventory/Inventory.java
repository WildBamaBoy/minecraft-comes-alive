package radixcore.inventory;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Inventory extends InventoryBasic
{
	public Inventory(String name, boolean displayCustomName, int size)
	{
		super(name, displayCustomName, size);
	}

	public boolean contains(Class clazz)
	{
		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			final ItemStack stack = this.getStackInSlot(i);

			if (stack != null)
			{
				final Item item = stack.getItem();

				if (item.getClass() == clazz)
				{
					return true;
				}
			}
		}

		return false;
	}

	public boolean contains(Item item)
	{
		return contains(item.getClass());
	}

	public boolean contains(Block block)
	{
		return contains(block.getClass());
	}

	public void loadInventoryFromNBT(NBTTagList tagList)
	{
		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			this.setInventorySlotContents(i, (ItemStack)null);
		}

		for (int i = 0; i < tagList.tagCount(); ++i)
		{
			NBTTagCompound nbt = tagList.getCompoundTagAt(i);
			int slot = nbt.getByte("Slot") & 255;

			if (slot >= 0 && slot < this.getSizeInventory())
			{
				this.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(nbt));
			}
		}
	}

	public NBTTagList saveInventoryToNBT()
	{
		NBTTagList tagList = new NBTTagList();

		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			ItemStack itemstack = this.getStackInSlot(i);

			if (itemstack != null)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setByte("Slot", (byte)i);
				itemstack.writeToNBT(nbt);
				tagList.appendTag(nbt);
			}
		}

		return tagList;
	}
}
