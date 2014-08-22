package com.adgdev.elemental_magic.content;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.adgdev.elemental_magic.EM;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RaceSubitemClass extends Item
{
	String race = "";
	
	public ItemStack is = new ItemStack(this);
	public NBTTagCompound nbt;
	
	public RaceSubitemClass(String r){
	    this.setUnlocalizedName(EM.MODID + "_" + "racesubitem");
		this.setCreativeTab(EM.tabEM);
		
		GameRegistry.registerItem(this, "Race classifier");
		
		race = r;
		nbt = new NBTTagCompound();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack par1ItemStack){
		return EnumRarity.epic;
	}
	
	@Override
	public boolean hasEffect(ItemStack par1ItemStack){
		return true;
	}
	
	@Override
	public void onUpdate(ItemStack itemStack, World world, Entity entity, int par4, boolean par5) 
	{
		is = itemStack;
		
		if(nbt.getString("race").isEmpty())
		{
			nbt.setString("race", race);
		}
	}
	
	public void addXP(int xp)
	{
		is.stackTagCompound = new NBTTagCompound();
		
		if(is.stackTagCompound.getInteger("lvp") == 0)
		{
			is.stackTagCompound.setInteger("lvp", 10);
		}
		else
		{
			is.stackTagCompound.setInteger("lvp", is.stackTagCompound.getInteger("lvp") + xp);
		}
	}
}
