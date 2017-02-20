package mca.core.minecraft;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;


public final class AchievementsMCA 
{
	private static final int middle = 0;

	//String statIdIn, String unlocalizedName, int column, int row, Item itemIn, Achievement parent)
	public static final Achievement fullGoldHearts = new Achievement("fullGoldHearts", "fullGoldHearts", 0, 0, Blocks.YELLOW_FLOWER, null);
	public static final Achievement engagement = new Achievement("engagement", "engagement",  2, 0 + 2, ItemsMCA.engagementRing, fullGoldHearts);
	public static final Achievement marriage = new Achievement("marriage", "marriage",4, 0, ItemsMCA.weddingRing, engagement);
	public static final Achievement babyBoy = new Achievement("babyBoy", "babyBoy", 6, middle + 1, ItemsMCA.babyBoy, marriage);
	public static final Achievement babyGirl = new Achievement("babyGirl", "babyGirl", 6, middle - 1, ItemsMCA.babyGirl, marriage);
	public static final Achievement twins = new Achievement("twins", "twins", 6, middle, ItemsMCA.toyTrain, marriage);
	public static final Achievement babyToChild = new Achievement("babyToChild", "babyToChild", 8, middle, Items.CAKE, marriage);
	public static final Achievement hunting = new Achievement("hunting", "hunting", 9, middle + 1, Items.BOW, babyToChild);
	public static final Achievement mining = new Achievement("mining", "mining", 10, middle + 1, Items.IRON_PICKAXE, babyToChild);
	public static final Achievement woodcutting = new Achievement("woodcutting", "woodcutting", 9, middle - 1, Items.IRON_AXE, babyToChild);
	public static final Achievement farming = new Achievement("farming", "farming", 10, middle - 1, Items.IRON_HOE, babyToChild);
	public static final Achievement childToAdult = new Achievement("childToAdult", "childToAdult", 11, middle, Items.IRON_SWORD, babyToChild);
	public static final Achievement childMarried = new Achievement("childMarried", "childMarried", 13, middle, ItemsMCA.matchmakersRing, childToAdult);
	public static final Achievement childHasChildren = new Achievement("childHasChildren", "childHasChildren", 15, middle, ItemsMCA.babyBoy, childMarried);

	public static final void initialize()
	{
		AchievementPage page = new AchievementPage("MCA", 
				fullGoldHearts, engagement, marriage, babyBoy, babyGirl, twins, babyToChild,
				hunting, mining, woodcutting, farming, childToAdult, childMarried, childHasChildren);
		AchievementPage.registerAchievementPage(page);
	}
}
