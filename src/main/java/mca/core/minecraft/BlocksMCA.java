package mca.core.minecraft;

import java.util.ArrayList;

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
	private static final ArrayList<Block> BLOCKS = new ArrayList<Block>();
	
	public static final Block rose_gold_block = new BlockOre();
	public static final Block rose_gold_ore = new BlockOre();
	public static final BlockTombstone tombstone = new BlockTombstone();
	public static final BlockVillagerSpawner villager_spawner = new BlockVillagerSpawner();
	public static final BlockMemorial memorial = new BlockMemorial();
	
	public BlocksMCA() { }
	
	public static void register(RegistryEvent.Register<Block> event)
	{
		rose_gold_block.setHardness(3.0F);
		rose_gold_block.setResistance(5.0F);
		rose_gold_block.setHarvestLevel("pickaxe", 2);
		rose_gold_ore.setHardness(3.0F);
		rose_gold_ore.setResistance(5.0F);
		rose_gold_ore.setHarvestLevel("pickaxe", 2);
		
		Block[] blocks = {
				rose_gold_block,
				rose_gold_ore,
				tombstone,
				villager_spawner,
				memorial
		};
		
		setBlockName(rose_gold_block, "rose_gold_block");
		setBlockName(rose_gold_ore, "rose_gold_ore");
		setBlockName(tombstone, "tombstone");
		setBlockName(villager_spawner, "villager_spawner");
		setBlockName(memorial, "memorial");
		
		for (Block block : blocks)
		{
			block.setCreativeTab(MCA.getCreativeTab());
			event.getRegistry().register(block);
			BLOCKS.add(block);
		}
	}
	
	public static void registerItemBlocks(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();
		ItemBlock[] items = {
			new ItemBlock(rose_gold_block),
			new ItemBlock(rose_gold_ore),
			new ItemBlock(villager_spawner)
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
		
		for (Block block : BLOCKS)
		{
			mesher.register(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
		}
	}
	
	private static void setBlockName(Block block, String blockName)
	{
		block.setRegistryName(MCA.ID, blockName);
		block.setUnlocalizedName(block.getRegistryName().toString());
	}
}
