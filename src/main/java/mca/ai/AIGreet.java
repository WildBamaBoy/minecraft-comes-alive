package mca.ai;

import java.util.Map;

import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;
import radixcore.util.RadixLogic;

public class AIGreet extends AbstractAI
{
	public static final int GREETING_INTERVAL = Time.SECOND * 120;
	public static final int CHANCE_TO_GREET = 60;

	private Map<String, PlayerMemory> playerMemories;
	private int ticksUntilUpdate = 0;

	public AIGreet(EntityHuman owner, Map<String, PlayerMemory> playerMemories)
	{
		super(owner);
		this.playerMemories = playerMemories;
	}

	@Override
	public void onUpdateCommon() 
	{
	}

	@Override
	public void onUpdateClient() 
	{
	}

	@Override
	public void onUpdateServer() 
	{
		if (!owner.getAI(AISleep.class).getIsSleeping())
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
				for (Object obj : owner.worldObj.playerEntities)
				{
					EntityPlayer player = (EntityPlayer)obj;

					if (owner.hasMemoryOfPlayer(player))
					{
						PlayerMemory memory = owner.getPlayerMemory(player);
						float distanceToPlayer = owner.getDistanceToEntity(player);

						if (distanceToPlayer > memory.getDistanceTraveledFrom())
						{
							memory.setDistanceTraveledFrom(Math.round(distanceToPlayer));
						}
					}
				}

				//Get the closest player and try to greet them.
				EntityPlayer closestPlayer = owner.worldObj.getClosestPlayerToEntity(owner, 4);

				if (closestPlayer != null)
				{
					PlayerMemory memory = owner.getPlayerMemory(closestPlayer);
					AISleep AISleep = owner.getAI(AISleep.class);

					//memory.setTimeUntilGreeting(20);
					
					if (memory.getTimeUntilGreeting() <= 0 && RadixLogic.getBooleanWithProbability(CHANCE_TO_GREET) && owner.canEntityBeSeen(closestPlayer) && !AISleep.getIsSleeping())
					{
						if (owner.getIsInfected())
						{
							closestPlayer.addChatComponentMessage(new TextComponentString(Color.RED + owner.getName() + " bites you."));
							closestPlayer.attackEntityFrom(DamageSource.generic, 2.0F);
							closestPlayer.addPotionEffect(new PotionEffect(MobEffects.weakness, Time.MINUTE * 2, 1));
						}

						else
						{
							owner.say(memory.getDialogueType() + ".greeting", closestPlayer);
						}
						
						memory.setTimeUntilGreeting(GREETING_INTERVAL);
						memory.setDistanceTraveledFrom(0);
					}
				}
			}
		}
	}

	@Override
	public void reset() 
	{

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		//No relevant data needed to save.
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		//No relevant data needed to load.
	}
}
