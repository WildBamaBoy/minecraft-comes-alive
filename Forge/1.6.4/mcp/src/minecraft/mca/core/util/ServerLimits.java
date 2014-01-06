package mca.core.util;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumRelation;
import net.minecraft.entity.player.EntityPlayer;

public final class ServerLimits
{
	public static boolean hasPlayerReachedBabyLimit(EntityPlayer player)
	{
		final int childLimit = MCA.getInstance().modPropertiesManager.modProperties.server_childLimit;
		final int playerId = MCA.getInstance().getIdOfPlayer(player);
		int numberOfChildren = 0;
		
		for (final AbstractEntity entity : MCA.getInstance().entitiesMap.values())
		{
			if (entity instanceof EntityPlayerChild && entity.familyTree.getRelationOf(playerId) == EnumRelation.Parent)
			{
				numberOfChildren++;
			}
		}
		
		return childLimit == -1 ? false : numberOfChildren >= childLimit;
	}
}
