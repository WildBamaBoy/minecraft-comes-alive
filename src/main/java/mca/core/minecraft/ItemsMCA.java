package mca.core.minecraft;

import java.lang.reflect.Field;

import mca.core.MCA;
import mca.enums.EnumMemorialType;
import mca.items.ItemBaby;
import mca.items.ItemCrystalBall;
import mca.items.ItemMemorial;
import mca.items.ItemNewOutfit;
import mca.items.ItemSpawnEgg;
import mca.items.ItemSpawnGrimReaper;
import mca.items.ItemStaffOfLife;
import mca.items.ItemTombstone;
import mca.items.ItemTooltipAppender;
import mca.items.ItemVillagerEditor;
import mca.items.ItemWhistle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ItemsMCA
{
	public static final Item weddingRing = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to marry them.").setUnlocalizedName("WeddingRing").setMaxStackSize(1);
	public static final Item weddingRingRG = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to marry them.").setUnlocalizedName("WeddingRingRG").setMaxStackSize(1);
	public static final Item engagementRing = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to become engaged.").setUnlocalizedName("EngagementRing").setMaxStackSize(1);
	public static final Item engagementRingRG = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to become engaged.").setUnlocalizedName("EngagementRingRG").setMaxStackSize(1);
	public static final Item matchmakersRing = new ItemTooltipAppender().setTooltip("Gift two to villagers standing close to each other to arrange a marriage between them.").setUnlocalizedName("MatchmakersRing").setMaxStackSize(2);
	public static final Item divorcePapers = new ItemTooltipAppender().setTooltip("Gift to your spouse to end your marriage.").setUnlocalizedName("DivorcePapers").setMaxStackSize(1);
	public static final ItemBaby babyBoy = new ItemBaby(true);
	public static final ItemBaby babyGirl = new ItemBaby(false);
	public static final ItemSpawnEgg eggMale = new ItemSpawnEgg(true);
	public static final ItemSpawnEgg eggFemale = new ItemSpawnEgg(false);
	public static final ItemSpawnGrimReaper eggGrimReaper = new ItemSpawnGrimReaper();
	public static final ItemCrystalBall crystalBall = new ItemCrystalBall();
	public static final Item roseGoldIngot = new Item().setUnlocalizedName("RoseGoldIngot");
	public static final Item roseGoldDust = new ItemTooltipAppender().setTooltip("Dust from a crushed rose gold ingot.").setUnlocalizedName("RoseGoldDust");
	public static final Item goldDust = new Item().setUnlocalizedName("GoldDust");
	public static final ItemNewOutfit newOutfit = new ItemNewOutfit();
	public static final Item needleAndString = new ItemTooltipAppender().setTooltip("Use with some wool to create cloth.").setUnlocalizedName("NeedleAndString").setMaxDamage(16).setMaxStackSize(1);
	public static final Item cloth = new ItemTooltipAppender().setTooltip("This can be used to craft new clothes for your villagers.").setUnlocalizedName("Cloth");

	public static final ItemTombstone itemTombstone = new ItemTombstone();
	public static final ItemWhistle whistle = new ItemWhistle();
	public static final ItemVillagerEditor villagerEditor = new ItemVillagerEditor();
	
	public static final ItemMemorial brokenRing = new ItemMemorial(EnumMemorialType.BROKEN_RING);
	public static final ItemMemorial childsDoll = new ItemMemorial(EnumMemorialType.DOLL);
	public static final ItemMemorial toyTrain = new ItemMemorial(EnumMemorialType.TRAIN);
	public static final ItemStaffOfLife staffOfLife = new ItemStaffOfLife();

	public static void register(RegistryEvent.Register<Item> event)
	{
		for (Field f : ItemsMCA.class.getFields())
		{
			try
			{
				Item item = (Item) f.get(null);
				item.setCreativeTab(MCA.getCreativeTab());
				item.setRegistryName(new ResourceLocation("mca:" + f.getName()));
				event.getRegistry().register(item);
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
