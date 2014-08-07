/*******************************************************************************
 * LanguageLoaderHook.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.network.packets.PacketSayLocalized;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.radixshock.radixcore.lang.ILanguageLoaderHook;

import cpw.mods.fml.common.FMLCommonHandler;

/**
 * MCA's language loader hook.
 */
public class LanguageLoaderHook implements ILanguageLoaderHook
{
	@Override
	public boolean processEntrySet(Map.Entry<Object, Object> entrySet) 
	{
		if (entrySet.getKey().toString().contains("name.male"))
		{
			MCA.getInstance().maleNames.add(entrySet.getValue().toString());
			return true;
		}

		else if (entrySet.getKey().toString().contains("name.female"))
		{
			MCA.getInstance().femaleNames.add(entrySet.getValue().toString());
			return true;
		}

		return false;
	}

	@Override
	public String onGetString(String elementId, Object... arguments) 
	{	
		EntityPlayer player = null;
		AbstractEntity entity = null;
		boolean useCharacterType = false;
		String prefix = null;
		String suffix = null;
		
		if (arguments != null && arguments.length > 0)
		{	
			player = (EntityPlayer) (arguments.length >= 1 ? arguments[0] : null);
			entity = (AbstractEntity) (arguments.length >= 2 ? arguments[1] : null);
			useCharacterType = (Boolean) (arguments.length >= 3 ? arguments[2] : null);
			prefix = (String) (arguments.length >= 4 ? arguments[3] : null);
			suffix = (String) (arguments.length >= 5 ? arguments[4] : null);
		}
		
		final List<String> matchingValues = new ArrayList();
		String outputString = "";
		elementId = elementId.toLowerCase();

		//Check for call to getString on a server. Invalid as the player will receive an untranslated string.
		if (FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			if (player == null && entity != null)
			{
				player = entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer);
			}
			
			if (entity != null)
			{
				MCA.packetHandler.sendPacketToPlayer(new PacketSayLocalized(player, entity.getEntityId(), elementId, useCharacterType, prefix, suffix), (EntityPlayerMP)player);
			}
			
			else
			{
				MCA.packetHandler.sendPacketToPlayer(new PacketSayLocalized(player, null, elementId, useCharacterType, prefix, suffix), (EntityPlayerMP)player);
			}
			
			return "";
		}

		if (useCharacterType)
		{
			elementId = entity.getCharacterType(MCA.getInstance().getIdOfPlayer(player)) + "." + elementId;
		}

		//Loop through each item in the string translations map.
		for (final Map.Entry<String, String> entrySet : MCA.getInstance().getLanguageLoader().getTranslations().entrySet())
		{
			//Check if the entry's key contains the ID.
			if (entrySet.getKey().contains(elementId))
			{
				//Then check if it completely equals the ID.
				if (entrySet.getKey().equals(elementId))
				{
					//In this case, clear the values list and add only the value that equals the ID.
					matchingValues.clear();
					matchingValues.add(entrySet.getValue());
					break;
				}

				else //Otherwise just add the matching ID's value to the matching values list.
				{
					matchingValues.add(entrySet.getValue());
				}
			}
		}

		if (matchingValues.isEmpty())
		{
			outputString = "(" + elementId + " not found)";
		}

		else
		{
			prefix = prefix == null ? "" : prefix;
			suffix = suffix == null ? "" : suffix;
			outputString = prefix + MCA.getInstance().getLanguageParser().parseString(matchingValues.get(MCA.rand.nextInt(matchingValues.size())), player, entity, useCharacterType, prefix, suffix) + suffix;
		}

		return outputString;
	}

	@Override
	public boolean shouldReceiveGetStringCalls() 
	{
		return true;
	}
}
