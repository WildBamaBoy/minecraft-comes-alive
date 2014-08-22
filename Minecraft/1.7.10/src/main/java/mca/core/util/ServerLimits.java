/*******************************************************************************
 * ServerLimits.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumRelation;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Allows checking for whatever limits there may be on an MCA server.
 */
public final class ServerLimits
{
	/**
	 * Checks if the player has reached the baby limit.
	 * 
	 * @param 	player	The player whose number of children should be checked.
	 * 
	 * @return	True if the player is at or over the baby limit.
	 */
	public static boolean hasPlayerReachedBabyLimit(EntityPlayer player)
	{
		final int childLimit = MCA.getInstance().getModProperties().server_childLimit;
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
