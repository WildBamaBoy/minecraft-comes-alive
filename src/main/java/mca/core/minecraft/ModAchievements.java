package mca.core.minecraft;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;


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

		fullGoldHearts = RadixAchievement.register("fullGoldHearts", 0, middle, Blocks.YELLOW_FLOWER, null);
		engagement = RadixAchievement.register("engagement",  2, middle + 2, ModItems.engagementRing, fullGoldHearts);
		marriage = RadixAchievement.register("marriage",  4, middle, ModItems.weddingRing, engagement);
		babyBoy = RadixAchievement.register("babyBoy", 6, middle + 1, ModItems.babyBoy, marriage);
		babyGirl = RadixAchievement.register("babyGirl", 6, middle - 1, ModItems.babyGirl, marriage);
		twins = RadixAchievement.register("twins", 6, middle, ModItems.diamondHeart, marriage);
		babyToChild = RadixAchievement.register("babyToChild", 8, middle, Items.CAKE, marriage);
		hunting = RadixAchievement.register("hunting", 9, middle + 1, Items.BOW, babyToChild);
		mining = RadixAchievement.register("mining", 10, middle + 1, Items.IRON_PICKAXE, babyToChild);
		woodcutting = RadixAchievement.register("woodcutting", 9, middle - 1, Items.IRON_AXE, babyToChild);
		farming = RadixAchievement.register("farming", 10, middle - 1, Items.IRON_HOE, babyToChild);
		childToAdult = RadixAchievement.register("childToAdult", 11, middle, Items.IRON_SWORD, babyToChild);
		childMarried = RadixAchievement.register("childMarried", 13, middle, ModItems.matchmakersRing, childToAdult);
		childHasChildren = RadixAchievement.register("childHasChildren", 15, middle, ModItems.babyBoy, childMarried);

		middle = 5;
		page = RadixAchievement.registerPage("Minecraft Comes Alive", this.getClass());
	}
}
