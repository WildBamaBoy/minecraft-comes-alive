package mca.core.minecraft;

import mca.blocks.BlockMemorial;
import mca.blocks.BlockTombstone;
import mca.blocks.BlockVillagerBed;
import mca.blocks.BlockVillagerSpawner;
import mca.core.MCA;
import mca.enums.EnumBedColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		roseGoldBlock = new BlockOre().setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("RoseGoldBlock").setCreativeTab(MCA.getCreativeTabMain());
		roseGoldOre = new BlockOre().setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("RoseGoldOre").setCreativeTab(MCA.getCreativeTabMain());

		roseGoldBlock.setHarvestLevel("pickaxe", 2);
		roseGoldOre.setHarvestLevel("pickaxe", 2);
		
		tombstone = new BlockTombstone();
		tombstone.setHarvestLevel("pickaxe", 1);
		tombstone.setHardness(3.0F);
		
		GameRegistry.registerBlock(roseGoldBlock, "RoseGoldBlock");
		GameRegistry.registerBlock(roseGoldOre, "RoseGoldOre");
		GameRegistry.registerBlock(tombstone, "Tombstone");
		GameRegistry.registerBlock(memorial, "Memorial");
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerModelMeshers()
	{
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		mesher.register(Item.getItemFromBlock(roseGoldBlock), 0, new ModelResourceLocation("mca:RoseGoldBlock", "inventory"));
		mesher.register(Item.getItemFromBlock(roseGoldOre), 0, new ModelResourceLocation("mca:RoseGoldOre", "inventory"));
		mesher.register(Item.getItemFromBlock(spawner), 0, new ModelResourceLocation("mca:VillagerSpawner", "inventory"));

	}
}
