/*******************************************************************************
 * TileEntityTombstone.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.tileentity;

import mca.core.util.PacketHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * Defines the tombstone tile entity and how it behaves.
 */
public class TileEntityTombstone extends TileEntity
{
	/** The current line of the sign that is being edited. -1 for no selected line. */
	public int lineBeingEdited;
	
	/** Is the GUI open? */
	public boolean guiOpen = false;
	
	/** Has the tombstone synced with the server? */
	public boolean hasSynced = false;
	
	/** The text displayed on the tombstone. */
	public String signText[] =
		{
			"Here Lies", "", "", ""
		};
	
	/**
	 * Constructor
	 */
	public TileEntityTombstone()
	{
		lineBeingEdited = -1;
	}

	@Override
    public void updateEntity()
    {
    	if (worldObj.isRemote)
    	{
    		if(!hasSynced && !guiOpen)
    		{
    			PacketDispatcher.sendPacketToServer(PacketHelper.createTombstoneRequestPacket(this));
    			hasSynced = true;
    		}
    	}
    }
    
    @Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		
		nbttagcompound.setString("Text1", signText[0]);
		nbttagcompound.setString("Text2", signText[1]);
		nbttagcompound.setString("Text3", signText[2]);
		nbttagcompound.setString("Text4", signText[3]);
	}

    @Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);

		for (int i = 0; i < 4; i++)
		{
			signText[i] = nbttagcompound.getString((new StringBuilder()).append("Text").append(i + 1).toString());

			if (signText[i].length() > 15)
			{
				signText[i] = signText[i].substring(0, 15);
			}
		}
	}
}
