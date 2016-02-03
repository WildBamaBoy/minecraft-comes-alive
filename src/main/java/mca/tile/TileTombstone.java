package mca.tile;

import mca.core.MCA;
import mca.packets.PacketTombstoneUpdateGet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileTombstone extends TileEntity
{
	public int lineBeingEdited;
	public boolean guiOpen;
	public boolean hasSynced;

	public String signText[] = { "Here Lies", "", "", "" };

	public TileTombstone()
	{
		lineBeingEdited = -1;
	}

	@Override
	public void onLoad()
	{
		if (worldObj.isRemote && !hasSynced && !guiOpen)
		{
			hasSynced = true;
			MCA.getPacketHandler().sendPacketToServer(new PacketTombstoneUpdateGet(this));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setString("Text1", signText[0]);
		nbt.setString("Text2", signText[1]);
		nbt.setString("Text3", signText[2]);
		nbt.setString("Text4", signText[3]);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		for (int i = 0; i < 4; i++)
		{
			signText[i] = nbt.getString(new StringBuilder().append("Text").append(i + 1).toString());

			if (signText[i].length() > 15)
			{
				signText[i] = signText[i].substring(0, 15);
			}
		}
	}
}
