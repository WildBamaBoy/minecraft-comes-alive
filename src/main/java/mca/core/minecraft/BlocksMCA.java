package mca.core.minecraft;

import java.lang.reflect.Field;

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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class BlocksMCA
{
	public static final BlockVillagerBed bedRed = new BlockVillagerBed(EnumBedColor.RED);
	public static final BlockVillagerBed bedBlue = new BlockVillagerBed(EnumBedColor.BLUE);
	public static final BlockVillagerBed bedGreen = new BlockVillagerBed(EnumBedColor.GREEN);
	public static final BlockVillagerBed bedPink = new BlockVillagerBed(EnumBedColor.PINK);
	public static final BlockVillagerBed bedPurple = new BlockVillagerBed(EnumBedColor.PURPLE);
	public static final Block roseGoldBlock = new BlockOre().setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("RoseGoldBlock").setCreativeTab(MCA.getCreativeTab());
	public static final Block roseGoldOre = new BlockOre().setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("RoseGoldOre").setCreativeTab(MCA.getCreativeTab());
	public static final BlockTombstone tombstone = new BlockTombstone();
	public static final BlockVillagerSpawner spawner = new BlockVillagerSpawner();
	public static final BlockMemorial memorial = new BlockMemorial();
	
	public BlocksMCA()
	{
		roseGoldBlock.setHarvestLevel("pickaxe", 2);
		roseGoldOre.setHarvestLevel("pickaxe", 2);
	}
	
	public static void register(RegistryEvent.Register<Block> event)
	{
		for (Field f : BlocksMCA.class.getFields())
		{
			try
			{
				Block block = (Block) f.get(null);
				block.setRegistryName(block.getUnlocalizedName().substring(5).toLowerCase());
				event.getRegistry().register(block);
			}

			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
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
