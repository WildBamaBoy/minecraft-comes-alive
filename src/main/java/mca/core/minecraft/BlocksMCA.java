package mca.core.minecraft;

import java.lang.reflect.Field;

import mca.blocks.BlockMemorial;
import mca.blocks.BlockTombstone;
import mca.blocks.BlockVillagerSpawner;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

public final class BlocksMCA
{
	public static final Block roseGoldBlock = new BlockOre().setHardness(3.0F).setResistance(5.0F);
	public static final Block roseGoldOre = new BlockOre().setHardness(3.0F).setResistance(5.0F);
	public static final BlockTombstone tombstone = new BlockTombstone();
	public static final BlockVillagerSpawner villagerspawner = new BlockVillagerSpawner();
	public static final BlockMemorial memorial = new BlockMemorial();
	
	public BlocksMCA()
	{
		
	}
	
	public static void register(RegistryEvent.Register<Block> event)
	{
		roseGoldBlock.setHarvestLevel("pickaxe", 2);
		roseGoldOre.setHarvestLevel("pickaxe", 2);
		
		for (Field f : BlocksMCA.class.getFields())
		{
			try
			{
				Block block = (Block) f.get(null);
				setBlockName(block, f.getName().toLowerCase());
				block.setCreativeTab(MCA.getCreativeTab());
				event.getRegistry().register(block);
			}

			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public static void registerItemBlocks(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();
		ItemBlock[] items = {
			new ItemBlock(roseGoldBlock),
			new ItemBlock(roseGoldOre),
			new ItemBlock(villagerspawner)
		};
		
		for (ItemBlock item : items)
		{
			Block block = item.getBlock();
			ResourceLocation registryName = block.getRegistryName();
			registry.register(item.setRegistryName(registryName));
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerModelMeshers()
	{
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		mesher.register(Item.getItemFromBlock(roseGoldBlock), 0, new ModelResourceLocation("mca:rosegoldblock", "inventory"));
		mesher.register(Item.getItemFromBlock(roseGoldOre), 0, new ModelResourceLocation("mca:rosegoldore", "inventory"));
		mesher.register(Item.getItemFromBlock(villagerspawner), 0, new ModelResourceLocation("mca:villagerspawner", "inventory"));
	}
	
	public static void setBlockName(Block block, String blockName)
	{
		block.setRegistryName(MCA.ID, blockName);
		block.setUnlocalizedName(block.getRegistryName().toString());
	}
}
