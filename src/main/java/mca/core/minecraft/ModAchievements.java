package mca.core.minecraft;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import radixcore.util.RadixAchievement;


public final class ModAchievements 
{
	public static AchievementPage page;
	
	public static Achievement fullGoldHearts;
	public static Achievement engagement;
	public static Achievement marriage;
	public static Achievement babyBoy;
	public static Achievement babyGirl;
	public static Achievement twins;
	public static Achievement babyToChild;
	public static Achievement hunting;
	public static Achievement mining;
	public static Achievement woodcutting;
	public static Achievement farming;
	public static Achievement childToAdult;
	public static Achievement childMarried;
	public static Achievement childHasChildren;
	
	public static Achievement craftDiamondDust;
	public static Achievement craftColoredDiamondDust;
	public static Achievement smeltColoredDiamond;
	public static Achievement craftShapedDiamond;
	public static Achievement craftShapedRing;
	
	public static Achievement craftCrown;
	public static Achievement makeKnight;
	public static Achievement makePeasant;
	public static Achievement makeHeir;
	
	public ModAchievements()
	{
		int middle = 0;
		
		fullGoldHearts = RadixAchievement.register("fullGoldHearts", 0, middle, Blocks.yellow_flower, null);
		engagement = RadixAchievement.register("engagement",  2, middle + 2, ModItems.engagementRing, fullGoldHearts);
		marriage = RadixAchievement.register("marriage",  4, middle, ModItems.weddingRing, engagement);
		babyBoy = RadixAchievement.register("babyBoy", 6, middle + 1, ModItems.babyBoy, marriage);
		babyGirl = RadixAchievement.register("babyGirl", 6, middle - 1, ModItems.babyGirl, marriage);
		twins = RadixAchievement.register("twins", 6, middle, ModItems.diamondHeart, marriage);
		babyToChild = RadixAchievement.register("babyToChild", 8, middle, Items.cake, marriage);
		hunting = RadixAchievement.register("hunting", 9, middle + 1, Items.bow, babyToChild);
		mining = RadixAchievement.register("mining", 10, middle + 1, Items.iron_pickaxe, babyToChild);
		woodcutting = RadixAchievement.register("woodcutting", 9, middle - 1, Items.iron_axe, babyToChild);
		farming = RadixAchievement.register("farming", 10, middle - 1, Items.iron_hoe, babyToChild);
		childToAdult = RadixAchievement.register("childToAdult", 11, middle, Items.iron_sword, babyToChild);
		childMarried = RadixAchievement.register("childMarried", 13, middle, ModItems.matchmakersRing, childToAdult);
		childHasChildren = RadixAchievement.register("childHasChildren", 15, middle, ModItems.babyBoy, childMarried);
		
		middle = 5;
		
		craftDiamondDust = RadixAchievement.register("craftDiamondDust", 0, middle, ModItems.diamondDust, null);
		craftColoredDiamondDust = RadixAchievement.register("craftColoredDiamondDust", 2, middle + 1, new ItemStack(ModItems.coloredDiamondDust, 1, 3), craftDiamondDust);
		smeltColoredDiamond = RadixAchievement.register("smeltColoredDiamond", 4, middle - 1, new ItemStack(ModItems.coloredDiamond, 1, 8), craftColoredDiamondDust);
		craftShapedDiamond = RadixAchievement.register("craftShapedDiamond", 6, middle + 1, new ItemStack(ModItems.coloredDiamondStar, 1, 12), smeltColoredDiamond);
		craftShapedRing = RadixAchievement.register("craftShapedRing", 8, middle - 1, new ItemStack(ModItems.coloredEngagementRing, 1, 6), craftShapedDiamond);
		
		page = RadixAchievement.registerPage("Minecraft Comes Alive", this.getClass());
	}
}
