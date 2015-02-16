package radixcore.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.data.BlockWithMeta;
import radixcore.math.Point3D;

public final class SchematicReader 
{
	public static Map<Point3D, BlockWithMeta> readSchematic(String location) throws IOException
	{
		Point3D origin = null;
		Point3D offset = null;

		Map<Point3D, BlockWithMeta> map = new HashMap<Point3D, BlockWithMeta>();
		NBTTagCompound nbtdata = CompressedStreamTools.readCompressed(SchematicReader.class.getResourceAsStream(location));

		short width = nbtdata.getShort("Width");
		short height = nbtdata.getShort("Height");
		short length = nbtdata.getShort("Length");

		byte[] blockIds = nbtdata.getByteArray("Blocks");
		byte[] data	= nbtdata.getByteArray("Data");
		byte[] addIds = new byte[0];
		short[] blocks = new short[blockIds.length];

		if (nbtdata.hasKey("AddBlocks")) 
		{
			addIds = nbtdata.getByteArray("AddBlocks");
		}

		try 
		{
			int originX = nbtdata.getInteger("WEOriginX");
			int originY = nbtdata.getInteger("WEOriginY");
			int originZ = nbtdata.getInteger("WEOriginZ");
			Point3D min = new Point3D(originX, originY, originZ);

			int offsetX = nbtdata.getInteger("WEOffsetX");
			int offsetY = nbtdata.getInteger("WEOffsetY");
			int offsetZ = nbtdata.getInteger("WEOffsetZ");
			offset = new Point3D(offsetX, offsetY, offsetZ);
			
			origin = new Point3D(min.iPosX - offset.iPosX, min.iPosY - offset.iPosY, min.iPosZ - offset.iPosZ);
		} 
		
		catch (Exception ignored) 
		{
			origin = new Point3D(0, 0, 0);
		}

		for (int index = 0; index < blockIds.length; index++) 
		{
			if ((index >> 1) >= addIds.length) 
			{
				blocks[index] = (short) (blockIds[index] & 0xFF);
			} 

			else 
			{
				if ((index & 1) == 0) 
				{
					blocks[index] = (short) (((addIds[index >> 1] & 0x0F) << 8) + (blockIds[index] & 0xFF));
				} 

				else 
				{
					blocks[index] = (short) (((addIds[index >> 1] & 0xF0) << 4) + (blockIds[index] & 0xFF));
				}
			}
		}

		for (int x = 0; x < width; ++x) 
		{
			for (int y = 0; y < height; ++y) 
			{
				for (int z = 0; z < length; ++z) 
				{
					int index = y * width * length + z * width + x;
					Point3D point = new Point3D(x + offset.iPosX, y + offset.iPosY - 1, z + offset.iPosZ);
					BlockWithMeta block = new BlockWithMeta(Block.getBlockById(blocks[index]), data[index]);

					map.put(point, block);
				}
			}
		}

		return map;
	}
}
