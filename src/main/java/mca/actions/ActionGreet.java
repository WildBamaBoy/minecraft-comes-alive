package mca.actions;

import java.util.Map;

import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumDialogueType;
import mca.enums.EnumInteraction;
import mca.packets.PacketOpenVillagerPrompt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;
import radixcore.modules.RadixLogic;

public class ActionGreet extends AbstractAction
{
	public static final int GREETING_INTERVAL = Time.SECOND * 120;
	public static final int CHANCE_TO_GREET = 60;

	private Map<String, PlayerMemory> playerMemories;
	private int ticksUntilUpdate = 0;

	public ActionGreet(EntityVillagerMCA actor, Map<String, PlayerMemory> playerMemories)
	{
		super(actor);
		this.playerMemories = playerMemories;
	}

	@Override
	public void onUpdateServer() 
	{
		if (!actor.getAI(ActionSleep.class).getIsSleeping())
		{
			//Update ticks until actual update. This AI runs once per second for performance.
			ticksUntilUpdate = ticksUntilUpdate <= 0 ? Time.SECOND : ticksUntilUpdate - 1;

			//Update greeting ticks if necessary. This part of the AI runs every tick.
			for (PlayerMemory memory : playerMemories.values())
			{
				int timeUntilGreeting = memory.getTimeUntilGreeting();

				if (timeUntilGreeting > 0)
				{
					memory.setTimeUntilGreeting(timeUntilGreeting - 1);
				}
			}

			//Perform the greeting attempt if time for the update.
			if (ticksUntilUpdate <= 0)
			{
				//Update the distance that each known player has traveled from this entity.
				for (Object obj : actor.world.playerEntities)
				{
					EntityPlayer player = (EntityPlayer)obj;

					if (actor.hasMemoryOfPlayer(player))
					{
						PlayerMemory memory = actor.getPlayerMemory(player);
						float distanceToPlayer = actor.getDistanceToEntity(player);

						if (distanceToPlayer > memory.getDistanceTraveledFrom())
						{
							memory.setDistanceTraveledFrom(Math.round(distanceToPlayer));
						}
					}
				}

				//Get the closest player and try to greet them.
				EntityPlayer closestPlayer = actor.world.getClosestPlayerToEntity(actor, 4);

				if (closestPlayer != null)
				{
					PlayerMemory memory = actor.getPlayerMemory(closestPlayer);
					ActionSleep AISleep = actor.getAI(ActionSleep.class);
					
					if (memory.getTimeUntilGreeting() <= 0 && RadixLogic.getBooleanWithProbability(CHANCE_TO_GREET) && actor.canEntityBeSeen(closestPlayer) && !AISleep.getIsSleeping())
					{
						if (actor.getIsInfected() && !closestPlayer.capabilities.isCreativeMode)
						{
							closestPlayer.sendMessage(new TextComponentString(Color.RED + actor.getName() + " bites you."));
							closestPlayer.attackEntityFrom(DamageSource.GENERIC, 2.0F);
							closestPlayer.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, Time.MINUTE * 2, 1));
						}

						else
						{
							//Check for low hearts on spouses.
							if (memory.getDialogueType() == EnumDialogueType.SPOUSE && memory.getHearts() <= -25)
							{
								actor.say(memory.getDialogueType() + ".lowhearts.greeting", closestPlayer);
								actor.incrementLowHeartWarnings();
							}
							
							else
							{
								//Check for nobility greeting.
								NBTPlayerData data = MCA.getPlayerData(closestPlayer);

								if (data.getHappinessThresholdMet() && RadixLogic.getBooleanWithProbability(10))
								{
									MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenVillagerPrompt(closestPlayer, actor, EnumInteraction.NOBILITY), (EntityPlayerMP)closestPlayer);
								}
								
								else
								{
									actor.say(memory.getDialogueType() + ".greeting", closestPlayer);
									actor.resetLowHeartWarnings(); //Make sure we reset when hearts are back to normal.
								}
							}
						}
						
						memory.setTimeUntilGreeting(GREETING_INTERVAL);
						memory.setDistanceTraveledFrom(0);
					}
				}
			}
		}
	}
}
