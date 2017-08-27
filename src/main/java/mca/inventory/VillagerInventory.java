package mca.inventory;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;

public class VillagerInventory extends InventoryBasic
{
	public VillagerInventory() 
	{
		super(new TextComponentString("Villager Inventory"), 41);
	}

	public int getFirstSlotContainingItem(Item item) 
	{
		for (int i = 0; i < this.getSizeInventory(); i++)
		{
			ItemStack stack = this.getStackInSlot(i);
			
			if (stack.getItem() == item)
			{
				return i;
			}
		}
		
		return -1;
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
	
	public boolean containsCountOf(Item item, int threshold)
	{
		int totalCount = 0;
		
		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			final ItemStack stack = this.getStackInSlot(i);

			if (stack != null)
			{
				final Item stackItem = stack.getItem();

				if (stackItem.getClass() == item.getClass())
				{
					totalCount += stack.getCount();
				}
			}
		}

		return totalCount >= threshold;
	}
	
	/**
	 * Gets the best quality (max damage) item of the specified type that is in the inventory.
	 *
	 * @param type The class of item that will be returned.
	 * @return The item stack containing the item of the specified type with the highest max damage.
	 */
	public ItemStack getBestItemOfType(Class type)
	{
		return getStackInSlot(getBestItemOfTypeSlot(type));
	}

	public int getBestItemOfTypeSlot(Class type)
	{
		int highestMaxDamage = 0;

		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			ItemStack stackInInventory = this.getStackInSlot(i);

			if (stackInInventory != null)
			{
				final String itemClassName = stackInInventory.getItem().getClass().getName();

				if (itemClassName.equals(type.getName()) && highestMaxDamage < stackInInventory.getMaxDamage())
				{
					highestMaxDamage = stackInInventory.getMaxDamage();					
					return i;
				}
			}
		}

		return -1;
	}

	public void removeCountOfItem(Item seedItem, int seedsRequired) 
	{
		//TODO
	}
	
	public void readInventoryFromNBT(NBTTagList tagList)
	{
		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			this.setInventorySlotContents(i, ItemStack.EMPTY);
		}

		for (int i = 0; i < tagList.tagCount(); ++i)
		{
			NBTTagCompound nbt = tagList.getCompoundTagAt(i);
			int slot = nbt.getByte("Slot") & 255;

			if (slot >= 0 && slot < this.getSizeInventory())
			{
				this.setInventorySlotContents(slot, new ItemStack(nbt));
			}
		}
	}

	public NBTTagList writeInventoryToNBT()
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
