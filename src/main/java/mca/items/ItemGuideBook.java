package mca.items;

import mca.core.minecraft.ItemsMCA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.world.World;

public class ItemGuideBook extends ItemWrittenBook 
{
	public ItemGuideBook()
	{
		super();
	}
	
	@Override
	public void onUpdate(ItemStack itemStack, World world, Entity entity, int unknownInt, boolean unknownBoolean)
	{
		super.onUpdate(itemStack, world, entity, unknownInt, unknownBoolean);

		if (!world.isRemote)
		{
			if (!itemStack.hasTagCompound())
			{
				ItemsMCA.setBookNBT(itemStack);
			}
		}
	}
	
	@Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        
        if (worldIn.isRemote)
        {
        	Minecraft.getMinecraft().displayGuiScreen(new GuiScreenBook(playerIn, itemstack, false));
        }
        
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }
	
	private void resolveContents(ItemStack stack, EntityPlayer player)
    {
        if (stack.getTagCompound() != null)
        {
            NBTTagCompound nbttagcompound = stack.getTagCompound();

            if (!nbttagcompound.getBoolean("resolved"))
            {
                nbttagcompound.setBoolean("resolved", true);

                if (validBookTagContents(nbttagcompound))
                {
                    NBTTagList nbttaglist = nbttagcompound.getTagList("pages", 8);

                    for (int i = 0; i < nbttaglist.tagCount(); ++i)
                    {
                        String s = nbttaglist.getStringTagAt(i);
                        ITextComponent itextcomponent;

                        try
                        {
                            itextcomponent = ITextComponent.Serializer.fromJsonLenient(s);
                            itextcomponent = TextComponentUtils.processComponent(player, itextcomponent, player);
                        }
                        catch (Exception var9)
                        {
                            itextcomponent = new TextComponentString(s);
                        }

                        nbttaglist.set(i, new NBTTagString(ITextComponent.Serializer.componentToJson(itextcomponent)));
                    }

                    nbttagcompound.setTag("pages", nbttaglist);

                    if (player instanceof EntityPlayerMP && player.getHeldItemMainhand() == stack)
                    {
                        Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
                        ((EntityPlayerMP)player).connection.sendPacket(new SPacketSetSlot(0, slot.slotNumber, stack));
                    }
                }
            }
        }
    }
}
