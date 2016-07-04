package mca.util;

import mca.ai.AIProgressStory;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumBabyState;
import mca.enums.EnumDialogueType;
import mca.enums.EnumProgressionStep;
import mca.enums.EnumRelation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import radixcore.util.RadixLogic;

/**
 * Centralized location for starting and ending marriages.
 */
public class MarriageHandler 
{
	public static void startEngagement(EntityPlayer player, EntityHuman human)
	{
		NBTPlayerData playerData = MCA.getPlayerData(player);
		PlayerMemory memory = human.getPlayerMemory(player);

		playerData.setSpouseName(human.getName());
		playerData.setSpousePermanentId(human.getPermanentId());
		playerData.setIsEngaged(true);

		human.setSpouseName(player.getName());
		human.setSpouseId(playerData.getPermanentId());
		human.setIsEngaged(true);

		human.getAI(AIProgressStory.class).setProgressionStep(EnumProgressionStep.FINISHED);
		memory.setDialogueType(EnumDialogueType.SPOUSE);
	}

	public static void startMarriage(EntityPlayer player, EntityHuman human)
	{
		boolean handleEngagement = human.getIsEngaged();
		NBTPlayerData playerData = MCA.getPlayerData(player);
		PlayerMemory memory = human.getPlayerMemory(player);

		playerData.setSpouseName(human.getName());
		playerData.setSpousePermanentId(human.getPermanentId());
		playerData.setIsEngaged(false);

		human.setSpouseName(player.getName());
		human.setSpouseId(playerData.getPermanentId());
		human.setIsEngaged(false);

		//Prevent any story progression on this villager.
		human.getAI(AIProgressStory.class).setProgressionStep(EnumProgressionStep.FINISHED);

		//Set the appropriate dialogue type.
		memory.setDialogueType(EnumDialogueType.SPOUSE);
		memory.setRelation(human.getIsMale() ? EnumRelation.HUSBAND : EnumRelation.WIFE);

		//Handle engagement gifts if we were engaged prior to marriage.
		if (handleEngagement)
		{
			for (Entity localEntity : RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityHuman.class, human, 50))
			{
				try
				{
					if (localEntity.getEntityId() != human.getEntityId())
					{
						EntityHuman localHuman = (EntityHuman)localEntity;
						PlayerMemory localMemory = localHuman.getPlayerMemory(player);
						localMemory.setHasGift(true);
					}
				}

				catch (ClassCastException e) //Something odd with Thaumcraft? Unable to reproduce.
				{
					continue;
				}
			}
		}
	}

	public static void startMarriage(EntityHuman human1, EntityHuman human2)
	{
		human1.setSpouseName(human2.getName());
		human1.setSpouseId(human2.getPermanentId());
		human1.setIsEngaged(false);

		human2.setSpouseName(human1.getName());
		human2.setSpouseId(human1.getPermanentId());
		human2.setIsEngaged(false);

		//Set up the story progression AI and set dominant progressor based on gender.
		AIProgressStory storyAI = human1.getAI(AIProgressStory.class);
		AIProgressStory partnerAI = human2.getAI(AIProgressStory.class);

		storyAI.setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
		partnerAI.setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);

		if (human1.getIsMale())
		{
			storyAI.setDominant(true);
			partnerAI.setDominant(false);
		}

		else if (human2.getIsMale())
		{
			partnerAI.setDominant(true);
			storyAI.setDominant(false);
		}
	}

	public static void startMarriage(EntityPlayer player1, EntityPlayer player2)
	{
		NBTPlayerData player1Data = MCA.getPlayerData(player1);
		NBTPlayerData player2Data = MCA.getPlayerData(player2);

		player1Data.setSpouseName(player2.getName());
		player1Data.setSpousePermanentId(player2Data.getPermanentId());
		player1Data.setIsEngaged(false);

		player2Data.setSpouseName(player1.getName());
		player2Data.setSpousePermanentId(player1Data.getPermanentId());
		player2Data.setIsEngaged(false);
	}

	public static void endMarriage(EntityPlayer player, EntityHuman human)
	{
		human.setBabyState(EnumBabyState.NONE);
		
		NBTPlayerData playerData = MCA.getPlayerData(player);
		PlayerMemory memory = human.getPlayerMemory(player);

		playerData.setSpouseName("none");
		playerData.setSpousePermanentId(0);
		playerData.setIsEngaged(false);

		human.setSpouseName("none");
		human.setSpouseId(0);
		human.setIsEngaged(false);
		memory.setDialogueType(EnumDialogueType.ADULT);
		memory.setRelation(EnumRelation.NONE);

		human.getAI(AIProgressStory.class).reset();
	}

	public static void endMarriage(EntityHuman human1, EntityHuman human2)
	{
		human1.setBabyState(EnumBabyState.NONE);
		human2.setBabyState(EnumBabyState.NONE);
		
		human1.setSpouseName("none");
		human1.setSpouseId(0);
		human1.setIsEngaged(false);

		human2.setSpouseName("none");
		human2.setSpouseId(0);
		human2.setIsEngaged(false);

		human1.getAI(AIProgressStory.class).reset();
		human2.getAI(AIProgressStory.class).reset();
	}

	public static void endMarriage(EntityPlayer player1, EntityPlayer player2)
	{
		NBTPlayerData player1Data = MCA.getPlayerData(player1);
		NBTPlayerData player2Data = MCA.getPlayerData(player2);

		player1Data.setSpouseName("none");
		player1Data.setSpousePermanentId(0);
		player1Data.setIsEngaged(false);

		player2Data.setSpouseName("none");
		player2Data.setSpousePermanentId(0);
		player2Data.setIsEngaged(false);
	}

	public static void forceEndMarriage(EntityPlayer player) 
	{
		forceEndMarriage(MCA.getPlayerData(player));
	}

	public static void forceEndMarriage(NBTPlayerData data) 
	{
		if (data != null)
		{
			data.setSpouseName("none");
			data.setSpousePermanentId(0);
			data.setIsEngaged(false);
		}
	}

	private MarriageHandler()
	{
	}
}
