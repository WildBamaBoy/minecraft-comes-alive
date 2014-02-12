/*******************************************************************************
 * Interactions.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util;

import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import mca.enums.EnumMoodChangeContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Defines all interactions performed under the "Interact" button.
 */
public final class Interactions 
{
	/**
	 * Calculate if a chat should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public static void doChat(AbstractEntity entity, EntityPlayer player)
	{	
		boolean chatWasGood = false;

		PlayerMemory memory = entity.playerMemoryMap.get(player.username);
		final int chanceModifier = -(memory.interactionFatigue * 7) + entity.mood.getChanceModifier("chat") + entity.trait.getChanceModifier("chat");
		int heartsModifier = entity.mood.getHeartsModifier("chat") + entity.trait.getHeartsModifier("chat");
		chatWasGood = Utility.getBooleanWithProbability(65 + chanceModifier);

		if (chatWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or entity.mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "chat.good"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity.worldObj.rand.nextInt(5) + 1 + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "chat.bad"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), -(entity.worldObj.rand.nextInt(5) + 1) + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.BadInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		memory.interactionFatigue++;
		entity.playerMemoryMap.put(player.username, memory);
	}

	/**
	 * Calculate if a joke should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public static void doJoke(AbstractEntity entity, EntityPlayer player)
	{
		boolean jokeWasGood = false;

		PlayerMemory memory = entity.playerMemoryMap.get(player.username);
		final int chanceModifier = -(memory.interactionFatigue * 7) + entity.mood.getChanceModifier("joke") + entity.trait.getChanceModifier("joke");
		int heartsModifier = entity.mood.getHeartsModifier("joke") + entity.trait.getHeartsModifier("joke");

		jokeWasGood = Utility.getBooleanWithProbability(65 + chanceModifier);

		if (jokeWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or entity.mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "joke.good"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity.worldObj.rand.nextInt(9) + 3 + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "joke.bad"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), -(entity.worldObj.rand.nextInt(9) + 3) + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.BadInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		memory.interactionFatigue++;
		entity.playerMemoryMap.put(player.username, memory);
	}

	/**
	 * Calculate if a greeting should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public static void doGreeting(AbstractEntity entity, EntityPlayer player)
	{
		boolean greetingWasGood = false;

		//This has a higher interaction fatigue, so that reactions are appropriate when the player "greets" someone multiple times.
		PlayerMemory memory = entity.playerMemoryMap.get(player.username);
		final int chanceModifier = -(memory.interactionFatigue * 20) + entity.mood.getChanceModifier("greeting") + entity.trait.getChanceModifier("greeting");
		int heartsModifier = entity.mood.getHeartsModifier("greeting") + entity.trait.getHeartsModifier("greeting");

		//Base 90% chance of success.
		greetingWasGood = Utility.getBooleanWithProbability(90 + chanceModifier);
		final String greetingType = memory.hearts >= 50 ? "highfive" : "handshake";

		if (greetingWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or entity.mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "greeting." + greetingType + ".good"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity.worldObj.rand.nextInt(3) + 3 + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "greeting." + greetingType + ".bad"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), -(entity.worldObj.rand.nextInt(3) + 3) + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.BadInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		memory.interactionFatigue++;
		entity.playerMemoryMap.put(player.username, memory);
	}

	/**
	 * Calculate if a story should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public static void doTellStory(AbstractEntity entity, EntityPlayer player)
	{
		boolean storyWasGood = false;

		PlayerMemory memory = entity.playerMemoryMap.get(player.username);
		final int chanceModifier = -(memory.interactionFatigue * 7) + entity.mood.getChanceModifier("story") + entity.trait.getChanceModifier("story");
		int heartsModifier = entity.mood.getHeartsModifier("story") + entity.trait.getHeartsModifier("story");

		storyWasGood = Utility.getBooleanWithProbability(65 + chanceModifier);

		if (storyWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or entity.mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "tellstory.good"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity.worldObj.rand.nextInt(9) + 3 + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "tellstory.bad"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), -(entity.worldObj.rand.nextInt(9) + 3) + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.BadInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		memory.interactionFatigue++;
		entity.playerMemoryMap.put(player.username, memory);
	}

	/**
	 * Calculate if play should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public static void doPlay(AbstractEntity entity, EntityPlayer player)
	{
		boolean playWasGood = false;

		PlayerMemory memory = entity.playerMemoryMap.get(player.username);
		final int chanceModifier = -(memory.interactionFatigue * 7) + entity.mood.getChanceModifier("play") + entity.trait.getChanceModifier("play");
		int heartsModifier = entity.mood.getHeartsModifier("play") + entity.trait.getHeartsModifier("play");

		playWasGood = Utility.getBooleanWithProbability(65 + chanceModifier);

		if (playWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or entity.mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "play.good"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity.worldObj.rand.nextInt(6) + 6 + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "play.bad"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), -(entity.worldObj.rand.nextInt(6) + 6) + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.BadInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		memory.interactionFatigue++;
		entity.playerMemoryMap.put(player.username, memory);
	}

	/**
	 * Calculate if a flirt should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public static void doFlirt(AbstractEntity entity, EntityPlayer player)
	{
		int hearts = entity.getHearts(player);
		boolean flirtWasGood = false;

		PlayerMemory memory = entity.playerMemoryMap.get(player.username);
		int chanceModifier = -(memory.interactionFatigue * 7) + entity.mood.getChanceModifier("flirt") + entity.trait.getChanceModifier("flirt");
		int heartsModifier = entity.mood.getHeartsModifier("flirt") + entity.trait.getHeartsModifier("flirt");

		//When hearts are above 50, add 35 to the chance modifier to make more sense.
		if (hearts > 50)
		{
			chanceModifier += 35;
		}

		//Base 10% chance of success.
		flirtWasGood = Utility.getBooleanWithProbability(10 + chanceModifier);

		if (flirtWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or entity.mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "flirt.good"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity.worldObj.rand.nextInt(8) + 4 + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "flirt.bad"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), -(entity.worldObj.rand.nextInt(8) + 4) + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.BadInteraction, (entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat()) / 2);
		}

		memory.interactionFatigue++;
		entity.playerMemoryMap.put(player.username, memory);
	}

	/**
	 * Calculate if a kiss should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public static void doKiss(AbstractEntity entity, EntityPlayer player)
	{
		final int hearts = entity.getHearts(player);
		boolean kissWasGood = false;

		//This has a higher interaction fatigue.
		PlayerMemory memory = entity.playerMemoryMap.get(player.username);
		int chanceModifier = -(memory.interactionFatigue * 10) + entity.mood.getChanceModifier("kiss") + entity.trait.getChanceModifier("kiss");
		int heartsModifier = entity.mood.getHeartsModifier("kiss") + entity.trait.getHeartsModifier("kiss");

		//When hearts are above 75, add 75 to the chance modifier to make more sense.
		if (hearts > 75)
		{
			chanceModifier += 75;
		}

		else
		{
			chanceModifier -= 25;
		}

		//Base 10% chance of success.
		kissWasGood = Utility.getBooleanWithProbability(10 + chanceModifier);

		if (kissWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or entity.mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "kiss.good"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity.worldObj.rand.nextInt(16) + 6 + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat());
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			entity.say(LanguageHelper.getString(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), entity, "kiss.bad"));
			entity.modifyHearts(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), -(entity.worldObj.rand.nextInt(16) + 6) + heartsModifier);
			entity.modifyMoodPoints(EnumMoodChangeContext.BadInteraction, entity.worldObj.rand.nextFloat() + entity.worldObj.rand.nextFloat());
		}

		memory.interactionFatigue++;
		entity.playerMemoryMap.put(player.username, memory);
	}
}
