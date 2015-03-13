package radixcore.util;

import java.lang.reflect.Field;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

public final class RadixAchievement 
{
	public static Achievement register(String name, int x, int y, Block block, Achievement prerequisite)
	{
		return new Achievement(name, name, x, y, block, prerequisite).registerStat();
	}

	public static Achievement register(String name, int x, int y, Item item, Achievement prerequisite)
	{
		return new Achievement(name, name, x, y, item, prerequisite).registerStat();
	}

	public static Achievement register(String name, int x, int y, ItemStack stack, Achievement prerequisite)
	{
		return new Achievement(name, name, x, y, stack, prerequisite).registerStat();
	}

	public static AchievementPage registerPage(String pageName, Class classContainingAchievements)
	{
		ArrayList<Achievement> achievements = new ArrayList<Achievement>();

		for (Field f : classContainingAchievements.getFields())
		{
			if (f.getType() == Achievement.class)
			{
				try
				{
					Achievement theAchievement = (Achievement) f.get(null);
					
					if (theAchievement != null)
					{
						achievements.add(theAchievement);
					}
				}

				catch (Exception e)
				{
					continue;
				}
			}
		}

		AchievementPage page = new AchievementPage(pageName, achievements.toArray(new Achievement[achievements.size()]));
		AchievementPage.registerAchievementPage(page);
		return page;
	}

	private RadixAchievement()
	{
	}
}
