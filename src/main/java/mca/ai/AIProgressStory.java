package mca.ai;

import mca.core.MCA;
import mca.core.minecraft.ModAchievements;
import mca.entity.EntityHuman;
import mca.enums.EnumBabyState;
import mca.enums.EnumProfessionGroup;
import mca.enums.EnumProgressionStep;
import mca.util.Utilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class AIProgressStory extends AbstractAI
{
	private int ticksUntilNextProgress;
	private int babyAge;
	private int numChildren;
	private boolean isDominant;
	private EnumProgressionStep progressionStep;

	private boolean forceNextProgress;
	
	public AIProgressStory(EntityHuman entityHuman) 
	{
		super(entityHuman);

		isDominant = true;
		ticksUntilNextProgress = MCA.getConfig() != null ? MCA.getConfig().storyProgressionRate : 20;
		setProgressionStep(EnumProgressionStep.SEARCH_FOR_PARTNER);
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
		//This AI starts working once the story progression threshold defined in the configuration file has been met.
		if (MCA.getConfig().storyProgression && owner.getTicksAlive() >= MCA.getConfig().storyProgressionThreshold * Time.MINUTE && isDominant && !owner.getIsChild() && !owner.getIsEngaged())
		{
			if (ticksUntilNextProgress <= 0 || forceNextProgress)
			{
				ticksUntilNextProgress = MCA.getConfig().storyProgressionRate * Time.MINUTE;

				if (RadixLogic.getBooleanWithProbability(75))
				{
					switch (progressionStep)
					{
					case FINISHED:
						break;
					case HAD_BABY:
						doAgeBaby();
						break;
					case TRY_FOR_BABY:
						doTryForBaby();
						break;
					case SEARCH_FOR_PARTNER:
						doPartnerSearch();
						break;
					case UNKNOWN:
						break;
					default:
						break;
					}
				}
			}

			else
			{
				ticksUntilNextProgress--;
			}
		}
	}

	@Override
	public void reset() 
	{
		owner.setTicksAlive(0);
		ticksUntilNextProgress = MCA.isTesting ? 20 : MCA.getConfig().storyProgressionRate;
		setProgressionStep(EnumProgressionStep.SEARCH_FOR_PARTNER);
		isDominant = true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("ticksUntilNextProgress", ticksUntilNextProgress);
		nbt.setInteger("babyAge", babyAge);
		nbt.setBoolean("isDominant", isDominant);
		nbt.setInteger("numChildren", numChildren);
		nbt.setInteger("progressionStep", progressionStep.getId());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		ticksUntilNextProgress = nbt.getInteger("ticksUntilNextProgress");
		babyAge = nbt.getInteger("babyAge");
		isDominant = nbt.getBoolean("isDominant");
		numChildren = nbt.getInteger("numChildren");
		setProgressionStep(EnumProgressionStep.getFromId(nbt.getInteger("progressionStep")));
	}

	private void doPartnerSearch()
	{
		EntityHuman partner = (EntityHuman) RadixLogic.getNearestEntityOfTypeWithinDistance(EntityHuman.class, owner, 15);

		boolean partnerIsValid = partner != null 
				&& partner.getIsMale() != owner.getIsMale() 
				&& !partner.getIsMarried() 
				&& !partner.getIsEngaged() 
				&& !partner.getIsChild() 
				&& partner.getProfessionGroup() != EnumProfessionGroup.Guard
				&& (partner.getFatherId() == -1 || partner.getFatherId() != owner.getFatherId()) 
				&& (partner.getMotherId() == -1 || partner.getMotherId() != owner.getMotherId());
		
		if (partnerIsValid)
		{
			//Set the other human's story progression appropriately.
			AIProgressStory mateAI = getMateAI(partner);
			setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
			mateAI.setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);

			//Set the dominant story progressor.
			if (owner.getIsMale())
			{
				this.isDominant = true;
				mateAI.isDominant = false;
			}

			else
			{
				this.isDominant = false;
				mateAI.isDominant = true;
			}

			//Mark both as married.
			owner.setMarriedTo(partner);
			partner.setMarriedTo(owner);
		}
	}

	private void doTryForBaby()
	{
		final EntityHuman mate = owner.getVillagerSpouse();
		final int villagersInArea = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityHuman.class, owner, 32).size();
		
		if (villagersInArea >= MCA.getConfig().storyProgressionCap && MCA.getConfig().storyProgressionCap != -1 && !forceNextProgress)
		{
			return;
		}
		
		if (RadixLogic.getBooleanWithProbability(50) && mate != null && RadixMath.getDistanceToEntity(owner, mate) <= 8.5D)
		{
			AIProgressStory mateAI = getMateAI(owner.getVillagerSpouse());
			setProgressionStep(EnumProgressionStep.HAD_BABY);
			mateAI.setProgressionStep(EnumProgressionStep.HAD_BABY);

			Utilities.spawnParticlesAroundEntityS("heart", owner, 16);
			Utilities.spawnParticlesAroundEntityS("heart", mate, 16);

			//Father's part is done, mother is now dominant for the baby's progression.
			isDominant = false;
			mateAI.isDominant = true;

			//Set baby state for the mother.
			mate.setBabyState(EnumBabyState.getRandomGender());
			
			//Increase number of children.
			numChildren++;
			mateAI.numChildren++;
			
			//Notify parent players of achievement.
			for (Object obj : owner.worldObj.playerEntities)
			{
				EntityPlayer onlinePlayer = (EntityPlayer)obj;
				
				if (owner.isPlayerAParent(onlinePlayer) || mate.isPlayerAParent(onlinePlayer))
				{
					onlinePlayer.triggerAchievement(ModAchievements.childHasChildren);	
				}
			}
		}
	}

	private void doAgeBaby()
	{
		final EntityHuman mate = owner.getVillagerSpouse();

		babyAge++;

		if (babyAge <= MCA.getConfig().babyGrowUpTime)
		{
			//Spawn the child.
			EntityHuman child;

			child = new EntityHuman(owner.worldObj, owner.getBabyState().isMale(), true, owner.getName(), owner.getSpouseName(), owner.getPermanentId(), owner.getSpouseId(), false);
			child.setPosition(owner.posX, owner.posY, owner.posZ);
			owner.worldObj.spawnEntityInWorld(child);

			//Reset self and mate status
			owner.setBabyState(EnumBabyState.NONE);
			mate.setBabyState(EnumBabyState.NONE);
			
			babyAge = 0;
			setProgressionStep(EnumProgressionStep.FINISHED);

			if (mate != null)
			{
				AIProgressStory mateAI = getMateAI(mate);
				mateAI.setProgressionStep(EnumProgressionStep.FINISHED);
			}

			//Generate chance of trying for another baby, if mate is found.
			if (numChildren < 4 && RadixLogic.getBooleanWithProbability(50) && mate != null)
			{
				AIProgressStory mateAI = getMateAI(mate);
				mateAI.setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
				mateAI.isDominant = true;

				isDominant = false;
				setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
			}
		}
	}

	private AIProgressStory getMateAI(EntityHuman human)
	{
		return human.getAI(AIProgressStory.class);
	}
	
	public void setTicksUntilNextProgress(int value)
	{
		this.ticksUntilNextProgress = value;
	}
	
	public void setProgressionStep(EnumProgressionStep step)
	{
		this.progressionStep = step;
		this.forceNextProgress = false;
	}
	
	public EnumProgressionStep getProgressionStep()
	{
		return this.progressionStep;
	}
	
	public void setDominant(boolean value)
	{
		this.isDominant = value;
	}
	
	public void setForceNextProgress(boolean value)
	{
		this.forceNextProgress = value;
	}
}
