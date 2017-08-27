package mca.core.minecraft;

import java.lang.reflect.Field;
import java.util.ArrayList;

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
	private static final ArrayList<Item> ITEMS = new ArrayList<Item>();
	
	public static final Item WEDDING_RING = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to marry them.").setMaxStackSize(1);
	public static final Item WEDDING_RING_RG = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to marry them.").setMaxStackSize(1);
	public static final Item ENGAGEMENT_RING = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to become engaged.").setMaxStackSize(1);
	public static final Item ENGAGEMENT_RING_RG = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to become engaged.").setMaxStackSize(1);
	public static final Item MATCHMAKERS_RING = new ItemTooltipAppender().setTooltip("Gift two to villagers standing close to each other to arrange a marriage between them.").setMaxStackSize(2);
	public static final Item DIVORCE_PAPERS = new ItemTooltipAppender().setTooltip("Gift to your spouse to end your marriage.").setMaxStackSize(1);
	public static final ItemBaby BABY_BOY = new ItemBaby(true);
	public static final ItemBaby BABY_GIRL = new ItemBaby(false);
	public static final ItemSpawnEgg EGG_MALE = new ItemSpawnEgg(true);
	public static final ItemSpawnEgg EGG_FEMALE = new ItemSpawnEgg(false);
	public static final ItemSpawnGrimReaper EGG_GRIM_REAPER = new ItemSpawnGrimReaper();
	public static final ItemCrystalBall CRYSTAL_BALL = new ItemCrystalBall();
	public static final Item ROSE_GOLD_INGOT = new Item();
	public static final Item ROSE_GOLD_DUST = new ItemTooltipAppender().setTooltip("Dust from a crushed rose gold ingot.");
	public static final Item GOLD_DUST = new Item();
	public static final ItemNewOutfit NEW_OUTFIT = new ItemNewOutfit();
	public static final Item NEEDLE_AND_STRING = new ItemTooltipAppender().setTooltip("Use with some wool to create cloth.").setMaxDamage(16).setMaxStackSize(1);
	public static final Item CLOTH = new ItemTooltipAppender().setTooltip("This can be used to craft new clothes for your villagers.");

	public static final ItemTombstone TOMBSTONE = new ItemTombstone();
	public static final ItemWhistle WHISTLE = new ItemWhistle();
	public static final ItemVillagerEditor VILLAGER_EDITOR = new ItemVillagerEditor();
	
	public static final ItemMemorial BROKEN_RING = new ItemMemorial(EnumMemorialType.BROKEN_RING);
	public static final ItemMemorial CHILDS_DOLL = new ItemMemorial(EnumMemorialType.DOLL);
	public static final ItemMemorial TOY_TRAIN = new ItemMemorial(EnumMemorialType.TRAIN);
	public static final ItemStaffOfLife STAFF_OF_LIFE = new ItemStaffOfLife();

	public static void register(RegistryEvent.Register<Item> event)
	{
		final Item[] items = {
				WEDDING_RING,
				WEDDING_RING_RG,
				ENGAGEMENT_RING,
				ENGAGEMENT_RING_RG,
				MATCHMAKERS_RING,
				DIVORCE_PAPERS,
				BABY_BOY,
				BABY_GIRL,
				EGG_MALE,
				EGG_FEMALE,
				EGG_GRIM_REAPER,
				CRYSTAL_BALL,
				ROSE_GOLD_INGOT,
				ROSE_GOLD_DUST,
				GOLD_DUST,
				NEW_OUTFIT,
				NEEDLE_AND_STRING,
				CLOTH,
				TOMBSTONE,
				WHISTLE,
				VILLAGER_EDITOR,
				BROKEN_RING,
				CHILDS_DOLL,
				TOY_TRAIN,
				STAFF_OF_LIFE
		};
		
		setItemName(WEDDING_RING, "wedding_ring");
		setItemName(WEDDING_RING_RG, "wedding_ring_rg");
		setItemName(ENGAGEMENT_RING, "engagement_ring");
		setItemName(ENGAGEMENT_RING_RG, "engagement_ring_rg");
		setItemName(MATCHMAKERS_RING, "matchmakers_ring");
		setItemName(DIVORCE_PAPERS, "divorce_papers");
		setItemName(BABY_BOY, "baby_boy");
		setItemName(BABY_GIRL, "baby_girl");
		setItemName(EGG_MALE, "egg_male");
		setItemName(EGG_FEMALE, "egg_female");
		setItemName(EGG_GRIM_REAPER, "egg_grim_reaper");
		setItemName(CRYSTAL_BALL, "crystal_ball");
		setItemName(ROSE_GOLD_INGOT, "rose_gold_ingot");
		setItemName(ROSE_GOLD_DUST, "rose_gold_dust");
		setItemName(GOLD_DUST, "gold_dust");
		setItemName(NEW_OUTFIT, "new_outfit");
		setItemName(NEEDLE_AND_STRING, "needle_and_string");
		setItemName(CLOTH, "cloth");
		setItemName(TOMBSTONE, "tombstone");
		setItemName(WHISTLE, "whistle");
		setItemName(VILLAGER_EDITOR, "villager_editor");
		setItemName(BROKEN_RING, "broken_ring");
		setItemName(CHILDS_DOLL, "childs_doll");
		setItemName(TOY_TRAIN, "toy_train");
		setItemName(STAFF_OF_LIFE, "staff_of_life");
		
		for (Item item : items)
		{
			item.setCreativeTab(MCA.getCreativeTab());
			event.getRegistry().register(item);
			ITEMS.add(item);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void registerModelMeshers()
	{
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		for (Item item : ITEMS)
		{
			mesher.register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
	}
	
	private static void setItemName(Item item, String itemName)
	{
		item.setUnlocalizedName(itemName);
		item.setRegistryName(new ResourceLocation(MCA.ID + ":" + itemName));
	}
}
