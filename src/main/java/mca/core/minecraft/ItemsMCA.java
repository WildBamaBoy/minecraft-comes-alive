package mca.core.minecraft;

import java.lang.reflect.Field;

import mca.core.MCA;
import mca.enums.EnumBedColor;
import mca.enums.EnumMemorialType;
import mca.items.ItemBaby;
import mca.items.ItemCrystalBall;
import mca.items.ItemMemorial;
import mca.items.ItemNewOutfit;
import mca.items.ItemSpawnEgg;
import mca.items.ItemSpawnGrimReaper;
import mca.items.ItemStaffOfLife;
import mca.items.ItemTombstone;
import mca.items.ItemVillagerBed;
import mca.items.ItemVillagerEditor;
import mca.items.ItemWhistle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ItemsMCA
{
	public static final Item weddingRing = new Item().setUnlocalizedName("WeddingRing").setMaxStackSize(1);
	public static final Item weddingRingRG = new Item().setUnlocalizedName("WeddingRingRG").setMaxStackSize(1);
	public static final Item engagementRing = new Item().setUnlocalizedName("EngagementRing").setMaxStackSize(1);
	public static final Item engagementRingRG = new Item().setUnlocalizedName("EngagementRingRG").setMaxStackSize(1);
	public static final Item matchmakersRing = new Item().setUnlocalizedName("MatchmakersRing").setMaxStackSize(1);
	public static final Item divorcePapers = new Item().setUnlocalizedName("DivorcePapers").setMaxStackSize(1);
	public static final ItemBaby babyBoy = new ItemBaby(true);
	public static final ItemBaby babyGirl = new ItemBaby(false);
	public static final ItemSpawnEgg eggMale = new ItemSpawnEgg(true);
	public static final ItemSpawnEgg eggFemale = new ItemSpawnEgg(false);
	public static final ItemSpawnGrimReaper eggReaper = new ItemSpawnGrimReaper();
	public static final ItemVillagerBed bedRed = new ItemVillagerBed(EnumBedColor.RED);
	public static final ItemVillagerBed bedBlue = new ItemVillagerBed(EnumBedColor.BLUE);
	public static final ItemVillagerBed bedGreen = new ItemVillagerBed(EnumBedColor.GREEN);
	public static final ItemVillagerBed bedPurple = new ItemVillagerBed(EnumBedColor.PURPLE);
	public static final ItemVillagerBed bedPink = new ItemVillagerBed(EnumBedColor.PINK);
	public static final ItemCrystalBall crystalBall = new ItemCrystalBall();
	public static final Item roseGoldIngot = new Item().setUnlocalizedName("RoseGoldIngot");
	public static final Item roseGoldDust = new Item().setUnlocalizedName("RoseGoldDust");
	public static final Item goldDust = new Item().setUnlocalizedName("GoldDust");
	public static final ItemNewOutfit newOutfit = new ItemNewOutfit();
	public static final Item needle = new Item().setUnlocalizedName("Needle").setMaxStackSize(1);
	public static final Item needleAndString = new Item().setUnlocalizedName("NeedleAndString").setMaxDamage(16).setMaxStackSize(1);
	public static final Item cloth = new Item().setUnlocalizedName("Cloth");

	public static final ItemTombstone tombstone = new ItemTombstone();
	public static final ItemWhistle whistle = new ItemWhistle();
	public static final ItemVillagerEditor villagerEditor = new ItemVillagerEditor();
	
	public static final ItemMemorial brokenRing = new ItemMemorial(EnumMemorialType.BROKEN_RING);
	public static final ItemMemorial childsDoll = new ItemMemorial(EnumMemorialType.DOLL);
	public static final ItemMemorial toyTrain = new ItemMemorial(EnumMemorialType.TRAIN);
	public static final ItemStaffOfLife staffOfLife = new ItemStaffOfLife();

	public static void initialize()
	{
		for (Field f : ItemsMCA.class.getFields())
		{
			try
			{
				Item item = (Item) f.get(null);
				item.setCreativeTab(MCA.getCreativeTabMain());
				item.setRegistryName(MCA.ID, item.getUnlocalizedName().substring(5)); //huehue
				GameRegistry.register(item);
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

		for (Field f : ItemsMCA.class.getFields())
		{
			try
			{
				Item item = (Item) f.get(null);
				String name = item.getUnlocalizedName().substring(5);
				mesher.register(item, 0, new ModelResourceLocation("mca:" + name, "inventory"));
			}

			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
