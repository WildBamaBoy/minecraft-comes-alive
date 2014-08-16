/*******************************************************************************
 * ItemVillagerBed.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import java.util.List;

import mca.block.BlockVillagerBed;
import mca.core.MCA;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ItemVillagerBed extends Item
{
    public ItemVillagerBed()
    {
        this.setCreativeTab(MCA.getInstance().tabMCA);
    }

    public abstract BlockVillagerBed getVillagerBedType();
    
    public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int posX, int posY, int posZ, int meta, float xOffset, float yOffset, float zOffset)
    {
        if (world.isRemote)
        {
            return true;
        }
        
        else if (meta != 1)
        {
            return false;
        }
        
        else
        {
            ++posY;
            BlockVillagerBed blockVillagerBed = getVillagerBedType();
            
            int metaCalc = MathHelper.floor_double((double)(entityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            
            byte movX = 0;
            byte movZ = 0;

            if (metaCalc == 0)
            {
                movZ = 1;
            }

            if (metaCalc == 1)
            {
                movX = -1;
            }

            if (metaCalc == 2)
            {
                movZ = -1;
            }

            if (metaCalc == 3)
            {
                movX = 1;
            }

            if (entityPlayer.canPlayerEdit(posX, posY, posZ, meta, itemStack) && entityPlayer.canPlayerEdit(posX + movX, posY, posZ + movZ, meta, itemStack))
            {
                if (world.isAirBlock(posX, posY, posZ) && world.isAirBlock(posX + movX, posY, posZ + movZ) && World.doesBlockHaveSolidTopSurface(world, posX, posY - 1, posZ) && World.doesBlockHaveSolidTopSurface(world, posX + movX, posY - 1, posZ + movZ))
                {
                    world.setBlock(posX, posY, posZ, blockVillagerBed, metaCalc, 3);

                    if (world.getBlock(posX, posY, posZ) == blockVillagerBed)
                    {
                        world.setBlock(posX + movX, posY, posZ + movZ, blockVillagerBed, metaCalc + 8, 3);
                    }

                    --itemStack.stackSize;
                    return true;
                }
                
                else
                {
                    return false;
                }
            }
            
            else
            {
                return false;
            }
        }
    }
    
	@Override
	public void registerIcons(IIconRegister IIconRegister)
	{
		itemIcon = IIconRegister.registerIcon("mca:VillagerBed");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.villagerbed.line1"));
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.villagerbed.line2"));
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.villagerbed.line3"));
	}
}
