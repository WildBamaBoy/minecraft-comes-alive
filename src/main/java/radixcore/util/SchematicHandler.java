package radixcore.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import radixcore.data.BlockObj;
import radixcore.math.Point3D;

public final class SchematicHandler 
{
	public static Point3D getPoint3DWithValue(Map<Point3D, BlockObj> schematicData, Point3D point)
	{
		for (Map.Entry<Point3D, BlockObj> entry : schematicData.entrySet())
		{
			if (entry.getKey().equals(point))
			{
				return entry.getKey();
			}
		}

		return null;
	}

	public static int countOccurencesOfBlockObj(Map<Point3D, BlockObj> schematicData, BlockObj searchBlock)
	{
		int count = 0;

		for (BlockObj block : schematicData.values())
		{
			if (block.equals(searchBlock))
			{
				count++;
			}
		}

		return count;
	}

	public static SortedMap<Point3D, BlockObj> readSchematic(String location)
	{
		Point3D origin = null;
		Point3D offset = null;

		SortedMap<Point3D, BlockObj> map = new TreeMap<Point3D, BlockObj>();

		try
		{
			NBTTagCompound nbtdata = CompressedStreamTools.readCompressed(SchematicHandler.class.getResourceAsStream(location));

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

			catch (Exception ignore) 
			{
				origin = Point3D.ZERO;
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
						BlockObj block = new BlockObj(Block.getBlockById(blocks[index]), data[index]);

						map.put(point, block);
					}
				}
			}
		}

		catch (IOException e)
		{
			RadixExcept.logFatalCatch(e, "Encountered a fatal error while reading a schematic.");
		}

		return map;
	}

	public static void spawnStructureRelativeToPlayer(String location, EntityPlayer player)
	{
		spawnStructureRelativeToPoint(location, new Point3D(player.posX, player.posY + 1, player.posZ), player.worldObj);
	}

	public static void spawnStructureRelativeToPoint(String location, Point3D point, World world)
	{
		Map<Point3D, BlockObj> schemBlocks = readSchematic(location);
		Map<Point3D, BlockObj> torchMap = new HashMap<Point3D, BlockObj>();

		for (Map.Entry<Point3D, BlockObj> entry : schemBlocks.entrySet())
		{
			if (entry.getValue().getBlock() == Blocks.torch)
			{
				torchMap.put(entry.getKey(), entry.getValue());
			}

			else
			{
				Point3D blockPoint = entry.getKey();

				int x = blockPoint.iPosX + point.iPosX;
				int y = blockPoint.iPosY + point.iPosY;
				int z = blockPoint.iPosZ + point.iPosZ;

				world.setBlock(x, y, z, entry.getValue().getBlock(), entry.getValue().getMeta(), 2);
			}
		}

		for (Map.Entry<Point3D, BlockObj> entry : torchMap.entrySet())
		{
			Point3D blockPoint = entry.getKey();

			int x = blockPoint.iPosX + point.iPosX;
			int y = blockPoint.iPosY + point.iPosY;
			int z = blockPoint.iPosZ + point.iPosZ;

			world.setBlock(x, y, z, entry.getValue().getBlock(), entry.getValue().getMeta(), 2);
		}
	}
}
