/*******************************************************************************
 * BlockVillagerBedPink.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.block;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockVillagerBedPink extends BlockVillagerBed
{
    public BlockVillagerBedPink()
    {
    	super();
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        this.textureTop = new IIcon[] {iconRegister.registerIcon("mca:VillagerBed-Feet-Top-Pink"), iconRegister.registerIcon("mca:VillagerBed-Head-Top-Pink")};
        this.textureEnd = new IIcon[] {iconRegister.registerIcon("mca:VillagerBed-Feet-End-Pink"), iconRegister.registerIcon("mca:VillagerBed-Head-End")};
        this.textureSide = new IIcon[] {iconRegister.registerIcon("mca:VillagerBed-Feet-Side-Pink"), iconRegister.registerIcon("mca:VillagerBed-Head-Side-Pink")};
    }

    @SideOnly(Side.CLIENT)
    public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_)
    {
        return Items.bed;
    }
}
