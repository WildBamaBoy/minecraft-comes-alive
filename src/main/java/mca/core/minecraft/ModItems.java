package mca.core.minecraft;

import java.lang.reflect.Field;

import mca.core.MCA;
import mca.enums.EnumBedColor;
import mca.items.ItemBaby;
import mca.items.ItemCrystalBall;
import mca.items.ItemGemCutter;
import mca.items.ItemSpawnEgg;
import mca.items.ItemTombstone;
import mca.items.ItemVillagerBed;
import mca.items.ItemVillagerEditor;
import mca.items.ItemWhistle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.item.ItemSingle;
import radixcore.util.RadixStartup;

public final class ModItems
{
	//First tab
	public static ItemSingle weddingRing;
	public static ItemSingle weddingRingRG;
	public static ItemSingle engagementRing;
	public static ItemSingle engagementRingRG;
	public static ItemSingle matchmakersRing;
	public static ItemSingle divorcePapers;
	public static ItemBaby babyBoy;
	public static ItemBaby babyGirl;
	public static ItemSpawnEgg eggMale;
	public static ItemSpawnEgg eggFemale;
	public static ItemVillagerBed bedRed;
	public static ItemVillagerBed bedBlue;
	public static ItemVillagerBed bedGreen;
	public static ItemVillagerBed bedPurple;
	public static ItemVillagerBed bedPink;
	public static ItemCrystalBall crystalBall;
	public static Item roseGoldIngot;
	public static Item roseGoldDust;
	public static Item goldDust;
	public static Item newOutfit;
	
	//Second tab
	public static ItemGemCutter gemCutter;
	public static Item diamondHeart;
	public static Item diamondTiny;
	public static Item diamondOval;
	public static Item diamondSquare;
	public static Item diamondTriangle;
	public static Item diamondStar;

	public static Item heartMold;
	public static Item tinyMold;
	public static Item ovalMold;
	public static Item squareMold;
	public static Item triangleMold;
	public static Item starMold;

	public static ItemSingle engagementRingHeart;
	public static ItemSingle engagementRingHeartRG;
	public static ItemSingle engagementRingTiny;
	public static ItemSingle engagementRingTinyRG;
	public static ItemSingle engagementRingOval;
	public static ItemSingle engagementRingOvalRG;
	public static ItemSingle engagementRingSquare;
	public static ItemSingle engagementRingSquareRG;
	public static ItemSingle engagementRingTriangle;
	public static ItemSingle engagementRingTriangleRG;
	public static ItemSingle engagementRingStar;
	public static ItemSingle engagementRingStarRG;

	public static ItemTombstone tombstone;
	public static ItemWhistle whistle;
	public static ItemVillagerEditor villagerEditor;

	//	public static final Item lostRelativeDocument;

	/*
	public static final Item crown;
	public static final Item heirCrown;
	public static final Item monarchCoat;
	public static final Item monarchPants;
	public static final Item monarchBoots;
	public static final Item redCrown;
	public static final Item greenCrown;
	public static final Item blueCrown;
	public static final Item pinkCrown;
	public static final Item purpleCrown;
	 */

	public ModItems()
	{
		RadixStartup.initBaseItems(ModItems.class, MCA.getCreativeTabMain(), MCA.getMetadata());
		matchmakersRing.setMaxStackSize(2);
		engagementRingHeart.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingHeartRG.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingTiny.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingTinyRG.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingOval.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingOvalRG.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingSquare.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingSquareRG.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingTriangle.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingTriangleRG.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingStar.setCreativeTab(MCA.getCreativeTabGemCutting());
		engagementRingStarRG.setCreativeTab(MCA.getCreativeTabGemCutting());

		gemCutter = new ItemGemCutter();
		heartMold.setCreativeTab(MCA.getCreativeTabGemCutting());
		tinyMold.setCreativeTab(MCA.getCreativeTabGemCutting());
		ovalMold.setCreativeTab(MCA.getCreativeTabGemCutting());
		squareMold.setCreativeTab(MCA.getCreativeTabGemCutting());
		triangleMold.setCreativeTab(MCA.getCreativeTabGemCutting());
		starMold.setCreativeTab(MCA.getCreativeTabGemCutting());

		diamondHeart.setCreativeTab(MCA.getCreativeTabGemCutting());
		diamondTiny.setCreativeTab(MCA.getCreativeTabGemCutting());
		diamondOval.setCreativeTab(MCA.getCreativeTabGemCutting());
		diamondSquare.setCreativeTab(MCA.getCreativeTabGemCutting());
		diamondTriangle.setCreativeTab(MCA.getCreativeTabGemCutting());
		diamondStar.setCreativeTab(MCA.getCreativeTabGemCutting());

		babyBoy = new ItemBaby(true);
		babyGirl = new ItemBaby(false);
		eggMale = new ItemSpawnEgg(true);
		eggFemale = new ItemSpawnEgg(false);
		whistle = new ItemWhistle();

		bedRed = new ItemVillagerBed(EnumBedColor.RED);
		bedBlue = new ItemVillagerBed(EnumBedColor.BLUE);
		bedGreen = new ItemVillagerBed(EnumBedColor.GREEN);
		bedPurple = new ItemVillagerBed(EnumBedColor.PURPLE);
		bedPink = new ItemVillagerBed(EnumBedColor.PINK);
		crystalBall = new ItemCrystalBall();

		tombstone = new ItemTombstone();
		villagerEditor = new ItemVillagerEditor();
	}

	@SideOnly(Side.CLIENT)
	public static void registerModelMeshers()
	{
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		for (Field f : ModItems.class.getFields())
		{
			try
			{
				Item item = (Item) f.get(null);
				String name = item.getUnlocalizedName().substring(5);
				mesher.register(item, 0, new ModelResourceLocation("mca:" + name, "inventory"));
			}

			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
