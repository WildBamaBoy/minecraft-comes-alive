package mca.actions;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumBabyState;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumProgressionStep;
import mca.util.Either;
import mca.util.Utilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import radixcore.constant.Time;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class ActionStoryProgression extends AbstractAction
{
	private int ticksUntilNextProgress;
	private int babyAge;
	private int numChildren;
	private boolean isDominant;
	private EnumProgressionStep progressionStep;

	private boolean forceNextProgress;

	public ActionStoryProgression(EntityVillagerMCA entityHuman) 
	{
		super(entityHuman);

		isDominant = true;
		ticksUntilNextProgress = MCA.getConfig() != null ? MCA.getConfig().storyProgressionRate : 20;
		setProgressionStep(EnumProgressionStep.SEARCH_FOR_PARTNER);
	}

	@Override
	public void onUpdateServer() 
	{
		//This AI starts working once the story progression threshold defined in the configuration file has been met.
		if (MCA.getConfig().storyProgression && actor.attributes.getTicksAlive() >= MCA.getConfig().storyProgressionThreshold * Time.MINUTE && isDominant && !actor.attributes.getIsChild() && !actor.attributes.getIsEngaged())
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
		//If we have a baby and are told to reset (as in our spouse died), 
		//do not change anything. Resetting will take place properly once
		//the baby grows.
		if (actor.attributes.getBabyState() == EnumBabyState.NONE)
		{
			actor.attributes.setTicksAlive(0);
			ticksUntilNextProgress = MCA.getConfig().storyProgressionRate;
			setProgressionStep(EnumProgressionStep.SEARCH_FOR_PARTNER);
			isDominant = true;
		}
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
		EntityVillagerMCA partner = (EntityVillagerMCA) RadixLogic.getClosestEntityExclusive(actor, 15, EntityVillagerMCA.class);

		boolean partnerIsValid = partner != null 
				&& partner.attributes.getGender() != actor.attributes.getGender() 
				&& partner.attributes.getMarriageState() == EnumMarriageState.NOT_MARRIED 
				&& !partner.attributes.getIsChild() 
				&& (partner.attributes.getFatherUUID() != actor.attributes.getFatherUUID()) 
				&& (partner.attributes.getMotherUUID() != actor.attributes.getMotherUUID());

		if (partnerIsValid)
		{
			//Mark as married.
			actor.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(partner));
		}
	}

	private void doTryForBaby()
	{
		final EntityVillagerMCA mate = actor.attributes.getVillagerSpouseInstance();
		final int villagersInArea = RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, actor, 32).size();

		if (villagersInArea >= MCA.getConfig().storyProgressionCap && MCA.getConfig().storyProgressionCap != -1 && !forceNextProgress)
		{
			return;
		}

		if (RadixLogic.getBooleanWithProbability(50) && mate != null && RadixMath.getDistanceToEntity(actor, mate) <= 8.5D)
		{
			ActionStoryProgression mateAI = getMateAI(actor.attributes.getVillagerSpouseInstance());
			setProgressionStep(EnumProgressionStep.HAD_BABY);
			mateAI.setProgressionStep(EnumProgressionStep.HAD_BABY);

			Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, actor, 16);
			Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, mate, 16);

			//Father's part is done, mother is now dominant for the baby's progression.
			isDominant = false;
			mateAI.isDominant = true;

			//Set baby state for the mother.
			mate.attributes.setBabyState(EnumBabyState.getRandomGender());

			//Increase number of children.
			numChildren++;
			mateAI.numChildren++;

			//Notify parent players of achievement.
			for (Object obj : actor.world.playerEntities)
			{
				EntityPlayer onlinePlayer = (EntityPlayer)obj;

				if (actor.attributes.isPlayerAParent(onlinePlayer) || mate.attributes.isPlayerAParent(onlinePlayer))
				{
					//onlinePlayer.addStat(AchievementsMCA.childHasChildren);
				}
			}
		}
	}

	private void doAgeBaby()
	{
		final EntityVillagerMCA mate = actor.attributes.getVillagerSpouseInstance();		
		babyAge++;

		if (babyAge <= MCA.getConfig().babyGrowUpTime)
		{
			//Spawn the child.
			EntityVillagerMCA child = new EntityVillagerMCA(actor.world);
			child.attributes.setGender(actor.attributes.getBabyState().isMale() ? EnumGender.MALE : EnumGender.FEMALE);
			child.attributes.setIsChild(true);
			child.attributes.assignRandomPersonality();
			child.attributes.assignRandomProfession();
			child.attributes.assignRandomScale();
			child.attributes.assignRandomName();
			child.attributes.assignRandomSkin();

			boolean actorIsMother = actor.attributes.getGender() == EnumGender.FEMALE;

			if (actorIsMother)
			{
				child.attributes.setMother(Either.<EntityVillagerMCA,EntityPlayer>withL(actor));
				child.attributes.setFatherGender(actor.attributes.getSpouseGender());
				child.attributes.setFatherName(actor.attributes.getSpouseName());
				child.attributes.setFatherUUID(actor.attributes.getSpouseUUID());
			}

			else //The actor is the father
			{
				child.attributes.setFather(Either.<EntityVillagerMCA,EntityPlayer>withL(actor));
				child.attributes.setMotherGender(actor.attributes.getSpouseGender());
				child.attributes.setMotherName(actor.attributes.getSpouseName());
				child.attributes.setMotherUUID(actor.attributes.getSpouseUUID());				
			}

			child.setPosition(actor.posX, actor.posY, actor.posZ);
			child.world.spawnEntity(child);

			//Reset self and mate status, if mate is found
			actor.attributes.setBabyState(EnumBabyState.NONE);

			babyAge = 0;
			setProgressionStep(EnumProgressionStep.FINISHED);

			if (mate != null)
			{
				ActionStoryProgression mateAI = getMateAI(mate);
				mateAI.setProgressionStep(EnumProgressionStep.FINISHED);
				mate.attributes.setBabyState(EnumBabyState.NONE);
				
				//Generate chance of trying for another baby, if mate is found.
				if (numChildren < 4 && RadixLogic.getBooleanWithProbability(50))
				{
					mateAI.setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
					mateAI.isDominant = true;

					isDominant = false;
					setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
				}
			}
			
			//Resetting if we're no longer married (mate died?)
			else if (actor.attributes.getMarriageState() == EnumMarriageState.NOT_MARRIED)
			{
				reset();
			}
		}
	}

	private ActionStoryProgression getMateAI(EntityVillagerMCA human)
	{
		return human.getBehavior(ActionStoryProgression.class);
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

	public boolean getIsDominant() 
	{
		return this.isDominant;
	}
}
