package radixcore.util;

import java.io.DataInputStream;
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
		Map<Point3D, BlockWithMeta> map = new HashMap<Point3D, BlockWithMeta>();
		NBTTagCompound nbtdata = CompressedStreamTools.readCompressed(SchematicReader.class.getResourceAsStream(location));

		short width = nbtdata.getShort("Width");
		short height = nbtdata.getShort("Height");
		short length = nbtdata.getShort("Length");

		byte[] blocks = nbtdata.getByteArray("Blocks");
		byte[] data	= nbtdata.getByteArray("Data");

		for (int z = 0; z < height; ++z)
		{
			for (int y = 0; y < width; ++y)
			{
				for (int x = 0; x < length; ++x)
				{
					int addr = (y * length + z) * width + x;
					
					map.put(new Point3D(x, y, z), new BlockWithMeta(Block.getBlockById(blocks[addr]), data[addr]));
				}
			}
		}

		return map;
	}
}
