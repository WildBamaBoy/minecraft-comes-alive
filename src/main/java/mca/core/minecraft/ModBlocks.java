package mca.core.minecraft;

import cpw.mods.fml.common.registry.GameRegistry;
import mca.blocks.BlockMemorial;
import mca.blocks.BlockTombstone;
import mca.blocks.BlockVillagerBed;
import mca.blocks.BlockVillagerSpawner;
import mca.core.MCA;
import mca.enums.EnumBedColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;

public final class ModBlocks
{
	public static BlockVillagerBed bedRed;
	public static BlockVillagerBed bedBlue;
	public static BlockVillagerBed bedGreen;
	public static BlockVillagerBed bedPink;
	public static BlockVillagerBed bedPurple;
	public static Block roseGoldBlock;
	public static Block roseGoldOre;
	public static BlockTombstone tombstone;
	public static BlockVillagerSpawner spawner;
	public static BlockMemorial memorial;
	
	public ModBlocks()
	{
		memorial = new BlockMemorial();
		
		bedRed = new BlockVillagerBed(EnumBedColor.RED);
		bedBlue = new BlockVillagerBed(EnumBedColor.BLUE);
		bedGreen = new BlockVillagerBed(EnumBedColor.GREEN);
		bedPink = new BlockVillagerBed(EnumBedColor.PINK);
		bedPurple = new BlockVillagerBed(EnumBedColor.PURPLE);
		
		spawner = new BlockVillagerSpawner();
		roseGoldBlock = new BlockOre().setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setBlockName("roseGoldBlock").setBlockTextureName("mca:RoseGoldBlock").setCreativeTab(MCA.getCreativeTabMain());
		roseGoldOre = new BlockOre().setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setBlockName("roseGoldOre").setBlockTextureName("mca:RoseGoldOre").setCreativeTab(MCA.getCreativeTabMain());

		roseGoldBlock.setHarvestLevel("pickaxe", 2);
		roseGoldOre.setHarvestLevel("pickaxe", 2);
		
		tombstone = new BlockTombstone();
		tombstone.setHarvestLevel("pickaxe", 1);
		tombstone.setHardness(3.0F);
		
		GameRegistry.registerBlock(roseGoldBlock, roseGoldBlock.getUnlocalizedName());
		GameRegistry.registerBlock(roseGoldOre, roseGoldOre.getUnlocalizedName());
		GameRegistry.registerBlock(tombstone, tombstone.getUnlocalizedName());
		GameRegistry.registerBlock(memorial, memorial.getUnlocalizedName());
	}
}
