package com.adgdev.elemental_magic.content;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import com.adgdev.elemental_magic.EM;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class RaceHandler 
{
	public static String race = "";
	
	public RaceHandler(String r)
	{
		race = r;
	}
	
	@SubscribeEvent
	public void livingDeathEvent(LivingDeathEvent event)
	{
		if(event.source.getSourceOfDamage() instanceof EntityPlayer) 
		{
		    EntityPlayer player = (EntityPlayer)event.source.getSourceOfDamage();	
			ItemStack inv = player.inventory.getStackInSlot(9);
			
			if(ItemStack.areItemStacksEqual(EM.raceSubItem.is, inv))
			{
				RaceSubitemClass item = (RaceSubitemClass) inv.getItem();
				
				if(item.is.stackTagCompound.getInteger("lvp") == 0)
				{
					item.is.stackTagCompound.setInteger("lvp", 10);
				}
				else
				{
					item.is.stackTagCompound.setInteger("lvp", item.is.stackTagCompound.getInteger("lvp") + 10);
				}
				
				player.addChatMessage(new ChatComponentText("Current Experience: " + item.is.stackTagCompound.getInteger("lvp")));
			}
		}
	}
}
