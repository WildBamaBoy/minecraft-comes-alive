package mca.core.minecraft;

import mca.core.MCA;
import mca.enums.EnumBedColor;
import mca.items.ItemBaby;
import mca.items.ItemCrystalBall;
import mca.items.ItemSpawnEgg;
import mca.items.ItemVillagerBed;
import net.minecraft.item.Item;
import radixcore.helpers.StartupHelper;
import radixcore.item.ItemSingle;

public final class Items
{
	public static ItemSingle weddingRing;
	public static ItemSingle roseGoldWeddingRing;
	public static ItemSingle engagementRing;
	public static ItemSingle roseGoldEngagementRing;
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
	
	/*	public static final Item tombstone;
	
	public static final Item whistle;
	public static final Item villagerEditor;
	public static final Item lostRelativeDocument;
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

	public Items()
	{
		StartupHelper.initBaseItems(Items.class, MCA.getCreativeTab(), MCA.getMetadata());
		matchmakersRing.setMaxStackSize(2);
		
		babyBoy = new ItemBaby(true);
		babyGirl = new ItemBaby(false);
		eggMale = new ItemSpawnEgg(true);
		eggFemale = new ItemSpawnEgg(false);
		bedRed = new ItemVillagerBed(EnumBedColor.RED);
		bedBlue = new ItemVillagerBed(EnumBedColor.BLUE);
		bedGreen = new ItemVillagerBed(EnumBedColor.GREEN);
		bedPurple = new ItemVillagerBed(EnumBedColor.PURPLE);
		bedPink = new ItemVillagerBed(EnumBedColor.PINK);
		crystalBall = new ItemCrystalBall();
	}
}
