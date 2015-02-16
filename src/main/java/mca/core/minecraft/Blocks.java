package mca.core.minecraft;

import mca.blocks.BlockVillagerBed;
import mca.core.MCA;
import mca.enums.EnumBedColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import cpw.mods.fml.common.registry.GameRegistry;

public final class Blocks
{
	public static BlockVillagerBed bedRed;
	public static BlockVillagerBed bedBlue;
	public static BlockVillagerBed bedGreen;
	public static BlockVillagerBed bedPink;
	public static BlockVillagerBed bedPurple;
	public static Block roseGoldBlock;
	public static Block roseGoldOre;

	public Blocks()
	{
		bedRed = new BlockVillagerBed(EnumBedColor.RED);
		bedBlue = new BlockVillagerBed(EnumBedColor.BLUE);
		bedGreen = new BlockVillagerBed(EnumBedColor.GREEN);
		bedPink = new BlockVillagerBed(EnumBedColor.PINK);
		bedPurple = new BlockVillagerBed(EnumBedColor.PURPLE);
		
		roseGoldBlock = new BlockOre().setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setBlockName("roseGoldBlock").setBlockTextureName("mca:roseGoldBlock").setCreativeTab(MCA.getCreativeTab());
		roseGoldOre = new BlockOre().setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setBlockName("roseGoldOre").setBlockTextureName("mca:RoseGoldOre").setCreativeTab(MCA.getCreativeTab());

		GameRegistry.registerBlock(roseGoldBlock, roseGoldBlock.getUnlocalizedName());
		GameRegistry.registerBlock(roseGoldOre, roseGoldOre.getUnlocalizedName());
	}
}
