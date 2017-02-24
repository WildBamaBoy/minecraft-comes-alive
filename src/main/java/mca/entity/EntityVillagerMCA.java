package mca.entity;

import static mca.core.Constants.EMPTY_UUID;
import static mca.core.Constants.EMPTY_UUID_OPT;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.sun.istack.internal.NotNull;

import io.netty.buffer.ByteBuf;
import mca.actions.AbstractAction;
import mca.actions.ActionAttackResponse;
import mca.actions.ActionBlink;
import mca.actions.ActionBuild;
import mca.actions.ActionCombat;
import mca.actions.ActionCook;
import mca.actions.ActionDefend;
import mca.actions.ActionFarm;
import mca.actions.ActionFish;
import mca.actions.ActionFollow;
import mca.actions.ActionGreet;
import mca.actions.ActionGrow;
import mca.actions.ActionHunt;
import mca.actions.ActionIdle;
import mca.actions.ActionManager;
import mca.actions.ActionMine;
import mca.actions.ActionPatrol;
import mca.actions.ActionProcreate;
import mca.actions.ActionRegenerate;
import mca.actions.ActionSleep;
import mca.actions.ActionStoryProgression;
import mca.actions.ActionUpdateMood;
import mca.actions.ActionWander;
import mca.actions.ActionWoodcut;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.data.PlayerMemoryHandler;
import mca.enums.EnumBabyState;
import mca.enums.EnumCombatBehaviors;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMovementState;
import mca.enums.EnumPersonality;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionSkinGroup;
import mca.enums.EnumProgressionStep;
import mca.enums.EnumRelation;
import mca.enums.EnumSleepingState;
import mca.inventory.VillagerInventory;
import mca.items.ItemBaby;
import mca.items.ItemMemorial;
import mca.items.ItemVillagerEditor;
import mca.packets.PacketOpenGUIOnEntity;
import mca.packets.PacketSetSize;
import mca.util.Either;
import mca.util.Utilities;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Font.Color;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixNettyIO;

/**
 * Main class of MCA's villager. Things such as AI are spread out into the mca.ai package.
 */
public class EntityVillagerMCA extends EntityCreature implements IEntityAdditionalSpawnData
{
	private static final DataParameter<String> NAME = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<String> HEAD_TEXTURE = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<String> CLOTHES_TEXTURE = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<Integer> PROFESSION = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> PERSONALITY = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> GENDER = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<String> SPOUSE_NAME = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<Optional<UUID>> SPOUSE_UUID = EntityDataManager.<Optional<UUID>>createKey(EntityVillagerMCA.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Integer> SPOUSE_GENDER = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<String> MOTHER_NAME = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<Optional<UUID>> MOTHER_UUID = EntityDataManager.<Optional<UUID>>createKey(EntityVillagerMCA.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Integer> MOTHER_GENDER = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<String> FATHER_NAME = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<Optional<UUID>> FATHER_UUID = EntityDataManager.<Optional<UUID>>createKey(EntityVillagerMCA.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Integer> FATHER_GENDER = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> BABY_STATE = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> MOVEMENT_STATE = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> AGE = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Float> SCALE_HEIGHT = EntityDataManager.<Float>createKey(EntityVillagerMCA.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> SCALE_WIDTH = EntityDataManager.<Float>createKey(EntityVillagerMCA.class, DataSerializers.FLOAT);
	private static final DataParameter<Boolean> DO_DISPLAY = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> IS_SWINGING = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> HELD_ITEM_SLOT = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> IS_INFECTED = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> DO_OPEN_INVENTORY = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> MARRIAGE_STATE = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	
	private final VillagerInventory inventory;

	@SideOnly(Side.CLIENT)
	public boolean isInteractionGuiOpen;

	private int timesWarnedForLowHearts;
	private int ticksAlive;
	private int swingProgressTicks;
	private ActionManager actionManager;
	private Map<String, PlayerMemory> playerMemories;

	/**
	 * Creates a new MCA villager in the given world.
	 * 
	 * @param world
	 */
	public EntityVillagerMCA(World world) 
	{
		super(world);
		
		playerMemories = new HashMap<String, PlayerMemory>();
		
		//Add custom AIs
		actionManager = new ActionManager(this);
		actionManager.addAction(new ActionIdle(this));
		actionManager.addAction(new ActionRegenerate(this));
		actionManager.addAction(new ActionSleep(this));
		actionManager.addAction(new ActionFollow(this));
		actionManager.addAction(new ActionGreet(this, playerMemories));
		actionManager.addAction(new ActionStoryProgression(this));
		actionManager.addAction(new ActionProcreate(this));
		actionManager.addAction(new ActionAttackResponse(this));
		actionManager.addAction(new ActionPatrol(this));
		actionManager.addAction(new ActionGrow(this));
		actionManager.addAction(new ActionUpdateMood(this));
		actionManager.addAction(new ActionBlink(this));
		actionManager.addAction(new ActionBuild(this));
		actionManager.addAction(new ActionMine(this));
		actionManager.addAction(new ActionWoodcut(this));
		actionManager.addAction(new ActionHunt(this));
		actionManager.addAction(new ActionCook(this));
		actionManager.addAction(new ActionFarm(this));
		actionManager.addAction(new ActionFish(this));
		actionManager.addAction(new ActionDefend(this));
		actionManager.addAction(new ActionWander(this));
		actionManager.addAction(new ActionCombat(this));
		
		setName("");
		setClothesTexture("");
		setHeadTexture("");
		setProfession(EnumProfession.Unassigned);
		setPersonality(EnumPersonality.UNASSIGNED);
		setGender(EnumGender.UNASSIGNED);
		setSpouse(null);
		setMother(null);
		setFather(null);

		addAI();

		inventory = new VillagerInventory();
	}

	public void addAI()
	{
		this.tasks.taskEntries.clear();

        ((PathNavigateGround)this.getNavigator()).setCanSwim(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(4, new EntityAIOpenDoor(this, true));

		int maxHealth = this.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard ? MCA.getConfig().guardMaxHealth : MCA.getConfig().villagerMaxHealth;
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);

		if (this.getHealth() > maxHealth || this.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard)
		{
			this.setHealth(maxHealth);
		}

		if (this.getProfessionSkinGroup() != EnumProfessionSkinGroup.Guard)
		{
			this.tasks.addTask(2, new EntityAIMoveIndoors(this));
		}
	}

	private void updateSwinging()
	{
		if (getIsSwinging())
		{
			swingProgressTicks++;

			if (swingProgressTicks >= 8)
			{
				swingProgressTicks = 0;
				setIsSwinging(false);
			}
		}

		else
		{
			swingProgressTicks = 0;
		}

		swingProgress = (float) swingProgressTicks / (float) 8;
	}

	@Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(NAME, "Steve");
        this.dataManager.register(HEAD_TEXTURE, "");
        this.dataManager.register(CLOTHES_TEXTURE, "");
        this.dataManager.register(PROFESSION, EnumProfession.Farmer.getId());
        this.dataManager.register(PERSONALITY, EnumPersonality.FRIENDLY.getId());
        this.dataManager.register(GENDER, EnumGender.MALE.getId());
        this.dataManager.register(SPOUSE_NAME, "N/A");
        this.dataManager.register(SPOUSE_UUID, Constants.EMPTY_UUID_OPT);
        this.dataManager.register(SPOUSE_GENDER, EnumGender.UNASSIGNED.getId());
        this.dataManager.register(MOTHER_NAME, "N/A");
        this.dataManager.register(MOTHER_UUID, Constants.EMPTY_UUID_OPT);
        this.dataManager.register(MOTHER_GENDER, EnumGender.UNASSIGNED.getId());
        this.dataManager.register(FATHER_NAME, "N/A");
        this.dataManager.register(FATHER_UUID, Constants.EMPTY_UUID_OPT);
        this.dataManager.register(FATHER_GENDER, EnumGender.UNASSIGNED.getId());
        this.dataManager.register(BABY_STATE, EnumBabyState.NONE.getId());
        this.dataManager.register(MOVEMENT_STATE, EnumMovementState.MOVE.getId());
        this.dataManager.register(IS_CHILD, Boolean.valueOf(false));
        this.dataManager.register(AGE, Integer.valueOf(0));
        this.dataManager.register(SCALE_HEIGHT, Float.valueOf(0));
        this.dataManager.register(SCALE_WIDTH, Float.valueOf(0));
        this.dataManager.register(DO_DISPLAY, Boolean.valueOf(false));
        this.dataManager.register(IS_SWINGING, Boolean.valueOf(false));
        this.dataManager.register(HELD_ITEM_SLOT, Integer.valueOf(0));
        this.dataManager.register(IS_INFECTED, Boolean.valueOf(false));
        this.dataManager.register(DO_OPEN_INVENTORY, Boolean.valueOf(false));
        this.dataManager.register(MARRIAGE_STATE, Integer.valueOf(0));
    }
    
	/*****************************
	 * Base Minecraft events
	 */
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		actionManager.onUpdate();
		
		if (!world.isRemote)
		{
			ticksAlive++;

			//Tick player memories
			for (PlayerMemory memory : this.playerMemories.values())
			{
				memory.doTick();
			}

			//Tick babies in inventory.
			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				ItemStack stack = inventory.getStackInSlot(i);

				if (stack != null && stack.getItem() instanceof ItemBaby)
				{
					ItemBaby item = (ItemBaby)stack.getItem();
					item.onUpdate(stack, world, this, 1, false);
				}
			}

			//Check if inventory should be opened for player.
			if (getDoOpenInventory())
			{
				final EntityPlayer player = world.getClosestPlayerToEntity(this, 10.0D);

				if (player != null)
				{
					player.openGui(MCA.getInstance(), Constants.GUI_ID_INVENTORY, world, (int)posX, (int)posY, (int)posZ);
				}

				setDoOpenInventory(false);
			}
		}

		else
		{
			updateSwinging();
		}
	}
	
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand)
	{
		if (getRidingEntity() == player) //Dismounts from a player on right-click
		{
			dismountRidingEntity();
			dismountEntity(player);
			return true;
		}

		if (!world.isRemote)
		{
			ItemStack heldItem = player.getHeldItem(hand);
			Item item = heldItem.getItem();
			
			if (player.capabilities.isCreativeMode && item instanceof ItemMemorial && !heldItem.hasTagCompound())
			{
				VillagerSaveData data = VillagerSaveData.fromVillager(this, null, player.getUniqueID());
				
				heldItem.setTagCompound(new NBTTagCompound());
				heldItem.getTagCompound().setString("ownerName", player.getName());
				heldItem.getTagCompound().setInteger("relation", getPlayerMemory(player).getRelation().getId());
				data.writeDataToNBT(heldItem.getTagCompound());
				
				this.setDead();
			}
			
			else
			{
				int guiId = item instanceof ItemVillagerEditor ? Constants.GUI_ID_EDITOR : Constants.GUI_ID_INTERACT;
				MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenGUIOnEntity(this.getEntityId(), guiId), (EntityPlayerMP) player);
			}
		}

		return true;
	}

	@Override
	public void onDeath(DamageSource damageSource) 
	{
		super.onDeath(damageSource);

		if (!world.isRemote)
		{
			//Switch to the sleeping skin and disable all chores/toggle AIs so they won't move
			actionManager.disableAllToggleActions();
			getAI(ActionSleep.class).transitionSkinState(true);
			
			//The death of a villager negatively modifies the mood of nearby villagers
			for (EntityVillagerMCA human : RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, this, 20))
			{
				human.getAI(ActionUpdateMood.class).modifyMoodLevel(-2.0F);
			}

			//Drop all items in the inventory
			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				ItemStack stack = inventory.getStackInSlot(i);

				if (stack != null)
				{
					entityDropItem(stack, 1.0F);
				}
			}
			
			//Reset the marriage stats of the player/villager this one was married to
			if (isMarriedToAPlayer()) 	
			{
				NBTPlayerData playerData = MCA.getPlayerData(world, getSpouseUUID());
				
				playerData.setMarriageState(EnumMarriageState.NOT_MARRIED);
				playerData.setSpouseName("");
				playerData.setSpouseUUID(EMPTY_UUID);
			}

			else if (isMarriedToAVillager())
			{
				EntityVillagerMCA partner = getVillagerSpouseInstance();

				if (partner != null)
				{
					partner.setSpouse(null);
				}
			}

			//Alert parents/spouse of the death if they are online and handle dropping memorials
			//Test against new iteration of player memory list each time to ensure the proper order
			//of handling notifications and memorial spawning
			boolean memorialDropped = false;
			
			for (PlayerMemory memory : playerMemories.values())
			{
				//Alert parents and spouse of the death.
				if (memory.getUUID().equals(getSpouseUUID()) || isPlayerAParent(memory.getUUID()))
				{
					EntityPlayer player = world.getPlayerEntityByUUID(memory.getUUID());
					
					if (player != null) //The player may not be online
					{
						player.sendMessage(new TextComponentString(Color.RED + getTitle(player) + " has died."));
					}
				}
			}
			
			//TODO dropping memorials
		}
	}

	@Override
	protected void updateAITasks()
	{
		ActionSleep sleepAI = getAI(ActionSleep.class);
		EnumMovementState moveState = getMovementState();
		boolean isSleeping = sleepAI.getIsSleeping();

		if (isSleeping)
		{
			// Minecraft 1.8 moved the execution of tasks out of updateAITasks and into EntityAITasks.updateTasks().
			// Get the 'tickCount' value per tick and set it to 1 when we don't want tasks to execute. This prevents
			// The AI tasks from ever triggering an update.
			ObfuscationReflectionHelper.setPrivateValue(EntityAITasks.class, tasks, 1, 4);
		}

		if (!isSleeping && (moveState == EnumMovementState.MOVE || moveState == EnumMovementState.FOLLOW))
		{
			super.updateAITasks();
		}

		if (moveState == EnumMovementState.STAY && !isSleeping)
		{
			tasks.onUpdateTasks();
			getLookHelper().onUpdateLook();
		}

		if (moveState == EnumMovementState.STAY || isSleeping)
		{
			getNavigator().clearPathEntity();
		}
	}
	
	@Override
	protected void damageEntity(DamageSource damageSource, float damageAmount)
	{
		super.damageEntity(damageSource, damageAmount);
	
		final ActionAttackResponse aiRespondToAttack = getAI(ActionAttackResponse.class);
	
		if (!aiRespondToAttack.getIsRetaliating())
		{
			aiRespondToAttack.startResponse(damageSource.getEntity());
		}
	
		//Reset sleeping if appropriate.
		ActionSleep aiSleep = actionManager.getAction(ActionSleep.class);
	
		if (aiSleep.getIsSleeping())
		{
			aiSleep.setSleepingState(EnumSleepingState.INTERRUPTED);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) 
	{
		super.writeEntityToNBT(nbt);
		actionManager.writeToNBT(nbt);

		//Auto save data manager values to NBT by reflection
		for (Field f : this.getClass().getDeclaredFields())
		{
			try
			{
				if (f.getType() == DataParameter.class)
				{
					Type genericType = f.getGenericType();
					String typeName = genericType.getTypeName();
					DataParameter param = (DataParameter) f.get(this);
					String paramName = f.getName();
					
					if (typeName.contains("Boolean"))
					{
						DataParameter<Boolean> bParam = (DataParameter<Boolean>)param;
						nbt.setBoolean(paramName, dataManager.get(bParam).booleanValue());
					}
					
					else if (typeName.contains("Integer"))
					{
						DataParameter<Integer> iParam = (DataParameter<Integer>)param;
						nbt.setInteger(paramName, dataManager.get(iParam).intValue());
					}
					
					else if (typeName.contains("String"))
					{
						DataParameter<String> sParam = (DataParameter<String>)param;
						nbt.setString(paramName, dataManager.get(sParam));
					}
					
					else if (typeName.contains("Float"))
					{
						DataParameter<Float> fParam = (DataParameter<Float>)param;
						nbt.setFloat(paramName, dataManager.get(fParam).floatValue());
					}
					
					else if (typeName.contains("Optional<java.util.UUID>"))
					{
						DataParameter<Optional<UUID>> uuParam = (DataParameter<Optional<UUID>>)param;
						nbt.setUniqueId(paramName, dataManager.get(uuParam).get());
					}
					
					else
					{
						throw new RuntimeException("Field type not handled while saving to NBT: " + f.getName());
					}
				}
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		nbt.setInteger("ticksAlive", ticksAlive);
		nbt.setInteger("timesWarnedForLowHearts", timesWarnedForLowHearts);
		
		PlayerMemoryHandler.writePlayerMemoryToNBT(playerMemories, nbt);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		actionManager.readFromNBT(nbt);

		//Auto read data manager values
		for (Field f : this.getClass().getDeclaredFields())
		{
			try
			{
				if (f.getType() == DataParameter.class)
				{
					Type genericType = f.getGenericType();
					String typeName = genericType.getTypeName();
					DataParameter param = (DataParameter) f.get(this);
					String paramName = f.getName();
					
					if (typeName.contains("Boolean"))
					{
						DataParameter<Boolean> bParam = (DataParameter<Boolean>)param;
						dataManager.set(bParam, nbt.getBoolean(paramName));
					}
					
					else if (typeName.contains("Integer"))
					{
						DataParameter<Integer> iParam = (DataParameter<Integer>)param;
						dataManager.set(iParam, nbt.getInteger(paramName));
					}
					
					else if (typeName.contains("String"))
					{
						DataParameter<String> sParam = (DataParameter<String>)param;
						dataManager.set(sParam, nbt.getString(paramName));
					}
					
					else if (typeName.contains("Float"))
					{
						DataParameter<Float> fParam = (DataParameter<Float>)param;
						dataManager.set(fParam, nbt.getFloat(paramName));
					}
					
					else if (typeName.contains("Optional<java.util.UUID>"))
					{
						DataParameter<Optional<UUID>> uuParam = (DataParameter<Optional<UUID>>)param;
						dataManager.set(uuParam, Optional.of(nbt.getUniqueId(paramName)));
					}
					
					else
					{
						throw new RuntimeException("Field type not handled while saving to NBT: " + f.getName());
					}
				}
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		ticksAlive = nbt.getInteger("ticksAlive");
		timesWarnedForLowHearts = nbt.getInteger("timesWarnedForLowHearts");
		
		PlayerMemoryHandler.readPlayerMemoryFromNBT(this, playerMemories, nbt);
		addAI();
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) 
	{
		RadixNettyIO.writeObject(buffer, playerMemories);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) 
	{
		Map<String, PlayerMemory> recvMemories = (Map<String, PlayerMemory>) RadixNettyIO.readObject(additionalData);
		this.playerMemories = recvMemories;
		setDoDisplay(true);
	}

	/******************************
	 * Base Minecraft getters
	 */
	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(getName());
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return null;
	}

	@Override
	protected SoundEvent getHurtSound() 
	{
		return getIsInfected() ? SoundEvents.ENTITY_ZOMBIE_HURT : null;
	}

	@Override
	protected SoundEvent getDeathSound() 
	{
		return getIsInfected() ? SoundEvents.ENTITY_ZOMBIE_DEATH : null;
	}

	@Override
	public boolean canBePushed()
	{
		final ActionSleep sleepAI = actionManager.getAction(ActionSleep.class);		
		return !sleepAI.getIsSleeping();
	}

	@Override
	protected boolean canDespawn() 
	{
		return false;
	}

	/************************************
	 * Methods relating to speech with a player
	 */
	public void sayRaw(String text, EntityPlayer target)
	{
		final StringBuilder sb = new StringBuilder();

		if (MCA.getConfig().villagerChatPrefix != null && !MCA.getConfig().villagerChatPrefix.equals("null"))
		{
			sb.append(MCA.getConfig().villagerChatPrefix);
		}

		sb.append(getTitle(target));
		sb.append(": ");
		sb.append(text);

		if (target != null)
		{
			target.sendMessage(new TextComponentString(sb.toString()));
		}

		actionManager.getAction(ActionIdle.class).reset();
	}

	public void say(String phraseId, EntityPlayer target, Object... arguments)
	{
		if (target == null)
		{
			return;
		}

		if (getIsInfected()) //Infected villagers moan when they speak, and will not say anything else.
		{
			String zombieMoan = RadixLogic.getBooleanWithProbability(33) ? "Raagh..." : RadixLogic.getBooleanWithProbability(33) ? "Ughh..." : "Argh-gur...";
			target.sendMessage(new TextComponentString(getTitle(target) + ": " + zombieMoan));
			this.playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, 0.5F, rand.nextFloat() + 0.5F);
		}

		else
		{
			final StringBuilder sb = new StringBuilder();

			//Handle chat prefix.
			if (MCA.getConfig().villagerChatPrefix != null && !MCA.getConfig().villagerChatPrefix.equals("null"))
			{
				sb.append(MCA.getConfig().villagerChatPrefix);
			}

			//Add title and text.
			sb.append(getTitle(target));
			sb.append(": ");
			sb.append(MCA.getLanguageManager().getString(phraseId, arguments));

			target.sendMessage(new TextComponentString(sb.toString()));

			actionManager.getAction(ActionIdle.class).reset();
			actionManager.getAction(ActionSleep.class).setSleepingState(EnumSleepingState.INTERRUPTED);
		}
	}

	public void say(String phraseId, EntityPlayer target)
	{
		say(phraseId, target, this, target);
	}

	public String getTitle(EntityPlayer player)
	{
		PlayerMemory memory = getPlayerMemory(player);

		if (memory.isRelatedToPlayer())
		{
			return MCA.getLanguageManager().getString(getGender() == EnumGender.MALE ? "title.relative.male" : "title.relative.female", this, player);
		}

		else
		{
			return MCA.getLanguageManager().getString(getGender() == EnumGender.MALE ? "title.nonrelative.male" : "title.nonrelative.female", this, player);
		}
	}

	/**************************************
	 * Player memory methods
	 */
	public void setPlayerMemory(EntityPlayer player, PlayerMemory memory)
	{
		playerMemories.put(player.getName(), memory);
	}

	public PlayerMemory getPlayerMemory(EntityPlayer player)
	{
		String playerName = player.getName();
		PlayerMemory returnMemory = playerMemories.get(playerName);

		if (returnMemory == null)
		{
			returnMemory = new PlayerMemory(this, player);
			playerMemories.put(playerName, returnMemory);
		}

		return returnMemory;
	}

	public PlayerMemory getPlayerMemoryWithoutCreating(EntityPlayer player) 
	{
		String playerName = player.getName();
		PlayerMemory returnMemory = playerMemories.get(playerName);
		return returnMemory;
	}

	public boolean hasMemoryOfPlayer(EntityPlayer player)
	{
		return playerMemories.containsKey(player.getName());
	}

	public Map<String, PlayerMemory> getAllPlayerMemories()
	{
		return playerMemories;
	}

	/***********
	 * AI, movement, and interaction methods
	 */
	public ActionManager getAIManager() 
	{
		return actionManager;
	}

	public <T extends AbstractAction> T getAI(Class<T> clazz)
	{
		return this.actionManager.getAction(clazz);
	}

	public float getSpeed()
	{
		return getPersonality() == EnumPersonality.ATHLETIC ? Constants.SPEED_RUN : Constants.SPEED_WALK;
	}

	public void halt()
	{
		getNavigator().clearPathEntity();

		moveForward = 0.0F;
		moveStrafing = 0.0F;
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
	}
	
	public void facePosition(Point3D position)
	{
		double midX = position.dX() - this.posX;
	    double midZ = position.dZ() - this.posZ;
	    double d1 = 0;
	
	    double d3 = (double)MathHelper.sqrt(midX * midX + midZ * midZ);
	    float f2 = (float)(Math.atan2(midZ, midX) * 180.0D / Math.PI) - 90.0F;
	    float f3 = (float)(-(Math.atan2(d1, d3) * 180.0D / Math.PI));
	    this.rotationPitch = this.updateRotation(this.rotationPitch, f3, 16.0F);
	    this.rotationYaw = this.updateRotation(this.rotationYaw, f2, 16.0F);
	}

	private float updateRotation(float p_70663_1_, float p_70663_2_, float p_70663_3_)
	{
	    float f3 = MathHelper.wrapDegrees(p_70663_2_ - p_70663_1_);
	
	    if (f3 > p_70663_3_)
	    {
	        f3 = p_70663_3_;
	    }
	
	    if (f3 < -p_70663_3_)
	    {
	        f3 = -p_70663_3_;
	    }
	
	    return p_70663_1_ + f3;
	}

	public boolean allowsHiring(EntityPlayer player) 
	{
		return getPlayerSpouseInstance() != player && (getProfessionSkinGroup() == EnumProfessionSkinGroup.Farmer || 
				getProfessionSkinGroup() == EnumProfessionSkinGroup.Miner || 
				getProfessionSkinGroup() == EnumProfessionSkinGroup.Warrior);
	}

	public boolean allowsWorkInteractions(EntityPlayer player)
	{
		final NBTPlayerData data = MCA.getPlayerData(player);
		final PlayerMemory memory = getPlayerMemory(player);
	
		if (data.getIsSuperUser())
		{
			return true;
		}
	
		else if (getIsInfected()) //Infected villagers can't use an inventory or do chores.
		{
			return false;
		}
	
		else if (memory.getIsHiredBy())
		{
			return true;
		}
	
		else if (isPlayerAParent(player))
		{
			return true;
		}
	
		return false;
	}

	public boolean allowsControllingInteractions(EntityPlayer player)
	{
		final NBTPlayerData data = MCA.getPlayerData(player);
	
		if (data.getIsSuperUser())
		{
			return true;
		}
	
		//Married to a player, and this player is not their spouse.
		else if (isMarriedToAPlayer() && !getSpouseUUID().equals(data.getUUID()))
		{
			return false;
		}
	
		else if (getIsChild())
		{
			if (isPlayerAParent(player))
			{
				return true;
			}
	
			else if (isChildOfAVillager())
			{
				return true;
			}
	
			else
			{
				return false;
			}
		}
	
		return true;
	}

	public boolean allowsIntimateInteractions(EntityPlayer player)
	{
		return !getIsChild() && !isPlayerAParent(player);
	}

	/******************************
	 * The villager's inventory and methods handling their equipment
	 */
	public VillagerInventory getVillagerInventory() 
	{
		return inventory;
	}

	public void openInventory(EntityPlayer player)
	{
		MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenGUIOnEntity(this.getEntityId(), Constants.GUI_ID_INVENTORY), (EntityPlayerMP) player);
	}
	
	@Override
	public ItemStack getHeldItem(EnumHand hand)
	{
		EnumBabyState babyState = getBabyState();
		
		if (getIsInfected())
		{
			return ItemStack.EMPTY;
		}

		else if (babyState != EnumBabyState.NONE)
		{
			switch (babyState)
			{
			case MALE: return new ItemStack(ItemsMCA.babyBoy);
			case FEMALE: return new ItemStack(ItemsMCA.babyGirl);
			}
		}

		else if (getProfessionEnum() == EnumProfession.Guard)
		{
			return new ItemStack(Items.IRON_SWORD);
		}

		else if (getProfessionEnum() == EnumProfession.Archer)
		{
			return new ItemStack(Items.BOW);
		}

		//FIXME
//		else if (heldItem.getInt() != -1 && aiManager.isToggleAIActive())
//		{
//			return new ItemStack(Item.getItemById(heldItem.getInt()));
//		}
//
//		else if (inventory.contains(ModItems.babyBoy) || inventory.contains(ModItems.babyGirl))
//		{
//			int slot = inventory.getFirstSlotContainingItem(ModItems.babyBoy);
//			slot = slot == -1 ? inventory.getFirstSlotContainingItem(ModItems.babyGirl) : slot;
//
//			if (slot != -1)
//			{
//				return inventory.getStackInSlot(slot);
//			}
//		}

		//Warriors, spouses, and player children all use weapons from the combat AI.
		else if (getProfessionEnum() == EnumProfession.Warrior || this.isMarriedToAPlayer() || getProfessionEnum() == EnumProfession.Child)
		{
			ActionCombat combat = getAI(ActionCombat.class);
			
			if (combat.getMethodBehavior() == EnumCombatBehaviors.METHOD_RANGED_ONLY)
			{
				return inventory.getBestItemOfType(ItemBow.class);
			}
			
			else
			{
				return inventory.getBestItemOfType(ItemSword.class);	
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	public void setHeldItem(Item item)
	{
		setHeldItem(EnumHand.MAIN_HAND, new ItemStack(item));
	}
	
	public boolean damageHeldItem(int amount)
	{
		try
		{
			ItemStack heldItem = getHeldItem(EnumHand.MAIN_HAND);

			if (heldItem != null)
			{
				Item item = heldItem.getItem();
				int slot = inventory.getFirstSlotContainingItem(item);

				ItemStack itemInSlot = inventory.getStackInSlot(slot);

				if (itemInSlot != null)
				{
					itemInSlot.damageItem(amount, this);

					if (itemInSlot.getCount() == 0)
					{
						actionManager.disableAllToggleActions();
						inventory.setInventorySlotContents(slot, null);
						return true;
					}

					else
					{
						inventory.setInventorySlotContents(slot, itemInSlot);
						return false;
					}
				}
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	
	public Iterable<ItemStack> getHeldEquipment()
	{
		List<ItemStack> heldEquipment = new ArrayList<ItemStack>();
		heldEquipment.add(getHeldItem(EnumHand.MAIN_HAND));
		return heldEquipment;
	}

	public Iterable<ItemStack> getArmorInventoryList()
	{
		List<ItemStack> armorInventory = new ArrayList<ItemStack>();
		armorInventory.add(inventory.getStackInSlot(39));
		armorInventory.add(inventory.getStackInSlot(38));
		armorInventory.add(inventory.getStackInSlot(37));
		armorInventory.add(inventory.getStackInSlot(36));

		return armorInventory;
	}

	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn)
	{
		switch (slotIn)
		{
		case HEAD: return inventory.getStackInSlot(36);
		case CHEST: return inventory.getStackInSlot(37);
		case LEGS: return inventory.getStackInSlot(38);
		case FEET: return inventory.getStackInSlot(39);
		case MAINHAND: return getHeldItem(EnumHand.MAIN_HAND);
		case OFFHAND: return ItemStack.EMPTY;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public int getTotalArmorValue()
	{
		int value = 0;

		for (int i = 36; i < 40; i++)
		{
			final ItemStack stack = inventory.getStackInSlot(i);

			if (stack != null && stack.getItem() instanceof ItemArmor)
			{
				value += ((ItemArmor)stack.getItem()).damageReduceAmount;
			}
		}

		return value;
	}

	@Override
	public void damageArmor(float amount)
	{
		for (int i = 36; i < 40; i++)
		{
			final ItemStack stack = inventory.getStackInSlot(i);

			if (stack != null && stack.getItem() instanceof ItemArmor)
			{
				stack.damageItem((int) amount, this);
			}
		}	
	}

	public double getBaseAttackDamage() 
	{
		switch (getPersonality())
		{
		case STRONG: return 2.0D;
		case CONFIDENT: return 1.0D;
		default: 
			if (getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard)
			{
				return 5.0D;
			}

			else
			{
				return 0.5D;
			}
		}
	}
	
	public void assignRandomName()
	{
		if (getGender() == EnumGender.MALE)
		{
			setName(MCA.getLanguageManager().getString("name.male"));
		}

		else
		{
			setName(MCA.getLanguageManager().getString("name.female"));
		}
	}
	
	public void assignRandomGender()
	{
		setGender(world.rand.nextBoolean() ? EnumGender.MALE : EnumGender.FEMALE);
	}
	
	public void assignRandomProfession()
	{
		setProfession(EnumProfession.getAtRandom());
	}
	
    /*********************************************************
     *	Getters / setters and methods relating to them 
     */
	public String getName()
	{
		return dataManager.get(NAME);
	}
	
	public void setName(String name)
	{
		dataManager.set(NAME, name);
	}
	
	public String getHeadTexture()
	{
		return dataManager.get(HEAD_TEXTURE);
	}
	
	public void setHeadTexture(String texture)
	{
		dataManager.set(HEAD_TEXTURE, texture);
	}
	
	public String getClothesTexture()
	{
		return dataManager.get(CLOTHES_TEXTURE);
	}
	
	public void setClothesTexture(String texture)
	{
		dataManager.set(CLOTHES_TEXTURE, texture);
	}
	
	public void assignRandomSkin()
	{
		if (this.getGender() == EnumGender.UNASSIGNED)
		{
			Throwable t = new Throwable();
			MCA.getLog().error("Attempted to randomize skin on unassigned gender villager.");
			MCA.getLog().error(t);
		}
		
		else
		{
			EnumProfessionSkinGroup skinGroup = this.getProfessionSkinGroup();
			String skin = this.getGender() == EnumGender.MALE ? skinGroup.getRandomMaleSkin() : skinGroup.getRandomFemaleSkin();
			setHeadTexture(skin);
			setClothesTexture(skin);
		}
	}
	
	public void assignRandomScale()
	{
		
	}
	
	public EnumProfession getProfessionEnum()
	{
		return EnumProfession.getProfessionById(dataManager.get(PROFESSION));
	}
	
	public EnumProfessionSkinGroup getProfessionSkinGroup()
	{
		return EnumProfession.getProfessionById(dataManager.get(PROFESSION).intValue()).getSkinGroup();
	}
	
	public void setProfession(EnumProfession profession)
	{
		dataManager.set(PROFESSION, profession.getId());
	}
	
	public EnumPersonality getPersonality()
	{
		return EnumPersonality.getById(dataManager.get(PERSONALITY));
	}
	
	public void setPersonality(EnumPersonality personality)
	{
		dataManager.set(PERSONALITY, personality.getId());
	}
	
	public EnumGender getGender()
	{
		return EnumGender.byId(dataManager.get(GENDER));
	}
	
	public void setGender(EnumGender gender)
	{
		dataManager.set(GENDER, gender.getId());
	}
	
	public String getSpouseName()
	{
		return dataManager.get(SPOUSE_NAME);
	}
	
	public UUID getSpouseUUID()
	{
		return dataManager.get(SPOUSE_UUID).or(EMPTY_UUID);
	}
	
	public EnumGender getSpouseGender()
	{
		return EnumGender.byId(dataManager.get(SPOUSE_GENDER));
	}
	
	/**
	 * Sets the given entity to be the spouse of the current villager. This is symmetric against the provided entity.
	 * If null is provided, this villager's spouse information will be reset. This is **NOT** symmetric.
	 * 
	 * @param 	either	Either object containing an MCA villager or a player. May be null.
	 */
	public void setSpouse(@Nullable Either<EntityVillagerMCA, EntityPlayer> either)
	{
		if (either == null)
		{
			//Reset spouse information back to default
			dataManager.set(SPOUSE_NAME, "");
			dataManager.set(SPOUSE_UUID, EMPTY_UUID_OPT);
			dataManager.set(SPOUSE_GENDER, EnumGender.UNASSIGNED.getId());
			setMarriageState(EnumMarriageState.NOT_MARRIED);
			
			//Reset our own story progression so that it may run again.
			getAI(ActionStoryProgression.class).reset();
		}
		
		else if (either.getLeft() != null)
		{
			EntityVillagerMCA spouse = either.getLeft();
			
			dataManager.set(SPOUSE_NAME, spouse.getName());
			dataManager.set(SPOUSE_UUID, Optional.of(spouse.getUniqueID()));
			dataManager.set(SPOUSE_GENDER, spouse.getGender().getId());
			setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);
			
			spouse.dataManager.set(SPOUSE_NAME, this.getName());
			spouse.dataManager.set(SPOUSE_UUID, Optional.of(this.getUniqueID()));
			spouse.dataManager.set(SPOUSE_GENDER, this.getGender().getId());
			spouse.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);
			
			getAI(ActionStoryProgression.class).setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
			spouse.getAI(ActionStoryProgression.class).setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
		}
		
		else if (either.getRight() != null)
		{
			EntityPlayer player = either.getRight();
			NBTPlayerData playerData = MCA.getPlayerData(player);
			
			dataManager.set(SPOUSE_NAME, player.getName());
			dataManager.set(SPOUSE_UUID, Optional.of(player.getUniqueID()));
			dataManager.set(SPOUSE_GENDER, playerData.getGender().getId());
			setMarriageState(EnumMarriageState.MARRIED_TO_PLAYER);
			
			playerData.setSpouseName(this.getName());
			playerData.setSpouseGender(this.getGender());
			playerData.setSpouseUUID(this.getUniqueID());
			playerData.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);
			
			//Prevent story progression when married to a player
			getAI(ActionStoryProgression.class).setProgressionStep(EnumProgressionStep.FINISHED);
		}
	}
	
	/* Performs an engagement between this villager and provided player. 
	 * DOES NOT handle nulls. To end an engagement, call setSpouse with null.
	 * */
	public void setFiancee(@NotNull EntityPlayer player) 
	{
		if (player == null) throw new RuntimeException("Engagement player cannot be null");
		
		NBTPlayerData playerData = MCA.getPlayerData(player);
		
		dataManager.set(SPOUSE_NAME, player.getName());
		dataManager.set(SPOUSE_UUID, Optional.of(player.getUniqueID()));
		dataManager.set(SPOUSE_GENDER, playerData.getGender().getId());
		
		setMarriageState(EnumMarriageState.ENGAGED);
		
		playerData.setSpouseName(this.getName());
		playerData.setSpouseGender(this.getGender());
		playerData.setSpouseUUID(this.getUniqueID());
		playerData.setMarriageState(EnumMarriageState.ENGAGED);
		
		//Prevent story progression when engaged to a player
		getAI(ActionStoryProgression.class).setProgressionStep(EnumProgressionStep.FINISHED);
	}
	
	public boolean isMarriedToAPlayer()
	{
		return getMarriageState() == EnumMarriageState.MARRIED_TO_PLAYER;
	}
	
	public boolean isMarriedToAVillager()
	{
		return getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER;
	}
	
	public boolean getIsEngaged()
	{
		return getMarriageState() == EnumMarriageState.ENGAGED;
	}
	
	public EntityVillagerMCA getVillagerSpouseInstance()
	{
		for (Object obj : world.loadedEntityList)
		{
			if (obj instanceof EntityVillagerMCA)
			{
				EntityVillagerMCA villager = (EntityVillagerMCA)obj;

				if (villager.getUniqueID().equals(getSpouseUUID()))
				{
					return villager;
				}
			}
		}

		return null;
	}

	public EntityPlayer getPlayerSpouseInstance()
	{
		for (Object obj : world.playerEntities)
		{
			final EntityPlayer player = (EntityPlayer)obj;
			
			if (player.getUniqueID().equals(this.getSpouseUUID()))
			{
				return player;
			}
		}
			
		return null;
	}
	
	public String getMotherName()
	{
		return dataManager.get(MOTHER_NAME);
	}
	
	public UUID getMotherUUID()
	{
		return dataManager.get(MOTHER_UUID).or(EMPTY_UUID);
	}
	
	public EnumGender getMotherGender()
	{
		return EnumGender.byId(dataManager.get(MOTHER_GENDER));
	}
	
	public void setMother(@Nullable Either<EntityVillagerMCA, EntityPlayer> either)
	{
		if (either == null)
		{
			dataManager.set(MOTHER_NAME, "");
			dataManager.set(MOTHER_UUID, EMPTY_UUID_OPT);
			dataManager.set(MOTHER_GENDER, EnumGender.UNASSIGNED.getId());
		}
		
		else if (either.getLeft() != null)
		{
			EntityVillagerMCA mother = either.getLeft();
			dataManager.set(MOTHER_NAME, mother.getName());
			dataManager.set(MOTHER_UUID, Optional.of(mother.getUniqueID()));
			dataManager.set(MOTHER_GENDER, mother.getGender().getId());
		}
		
		else if (either.getRight() != null)
		{
			EntityPlayer player = either.getRight();
			NBTPlayerData data = MCA.getPlayerData(player);
			
			dataManager.set(MOTHER_NAME, player.getName());
			dataManager.set(MOTHER_UUID, Optional.of(player.getUniqueID()));
			dataManager.set(MOTHER_GENDER, data.getGender().getId());
		}
	}
	
	public String getFatherName()
	{
		return dataManager.get(FATHER_NAME);
	}
	
	public UUID getFatherUUID()
	{
		return dataManager.get(FATHER_UUID).or(EMPTY_UUID);
	}
	
	public EnumGender getFatherGender()
	{
		return EnumGender.byId(dataManager.get(FATHER_GENDER));
	}
	
	public void setFather(@Nullable Either<EntityVillagerMCA, EntityPlayer> either)
	{
		if (either == null)
		{
			dataManager.set(FATHER_NAME, "");
			dataManager.set(FATHER_UUID, EMPTY_UUID_OPT);
			dataManager.set(FATHER_GENDER, EnumGender.UNASSIGNED.getId());
		}
		
		else if (either.getLeft() != null)
		{
			EntityVillagerMCA mother = either.getLeft();
			dataManager.set(FATHER_NAME, mother.getName());
			dataManager.set(FATHER_UUID, Optional.of(mother.getUniqueID()));
			dataManager.set(FATHER_GENDER, mother.getGender().getId());
		}
		
		else if (either.getRight() != null)
		{
			EntityPlayer player = either.getRight();
			NBTPlayerData data = MCA.getPlayerData(player);
			
			dataManager.set(FATHER_NAME, player.getName());
			dataManager.set(FATHER_UUID, Optional.of(player.getUniqueID()));
			dataManager.set(FATHER_GENDER, data.getGender().getId());
		}
	}

	public boolean isPlayerAParent(EntityPlayer player)
	{
		final NBTPlayerData data = MCA.getPlayerData(player);

		if (data != null)
		{
			return getMotherUUID() == data.getUUID() || getFatherUUID() == data.getUUID();
		}

		else
		{
			return false;
		}
	}
	
	public boolean isPlayerAParent(UUID uuid)
	{
		final NBTPlayerData data = MCA.getPlayerData(world, uuid);

		if (data != null)
		{
			return getMotherUUID() == data.getUUID() || getFatherUUID() == data.getUUID();
		}

		else
		{
			return false;
		}
	}
	
	public EnumBabyState getBabyState()
	{
		return EnumBabyState.fromId(dataManager.get(BABY_STATE));
	}
	
	public void setBabyState(EnumBabyState state)
	{
		dataManager.set(BABY_STATE, state.getId());
	}
	
	public EnumMovementState getMovementState()
	{
		return EnumMovementState.fromId(dataManager.get(MOVEMENT_STATE));
	}
	
	public void setMovementState(EnumMovementState state)
	{
		dataManager.set(MOVEMENT_STATE, state.getId());
	}
	
	public boolean getIsChild()
	{
		return dataManager.get(IS_CHILD);
	}
	
	public void setIsChild(boolean isChild)
	{
		dataManager.set(IS_CHILD, isChild);
		
		EnumDialogueType newDialogueType = isChild ? EnumDialogueType.CHILD : EnumDialogueType.ADULT;
		EnumDialogueType targetReplacementType = isChild ? EnumDialogueType.ADULT : EnumDialogueType.CHILD;
			
		for (PlayerMemory memory : playerMemories.values())
		{
			if (memory.getDialogueType() == targetReplacementType)
			{
				memory.setDialogueType(newDialogueType);
			}
		}
	}
	
	public boolean isChildOfAVillager() 
	{
		// If we can't find data for the mother and father, the child's parents
		// must be other villagers.
		NBTPlayerData motherData = MCA.getPlayerData(world, getMotherUUID());
		NBTPlayerData fatherData = MCA.getPlayerData(world, getFatherUUID());
		
		return motherData == null && fatherData == null;
	}

	public int getAge()
	{
		return dataManager.get(AGE).intValue();
	}
	
	public void setAge(int age)
	{
		dataManager.set(AGE, age);
	}
	
	public float getScaleHeight()
	{
		return dataManager.get(SCALE_HEIGHT);
	}
	
	public void setScaleHeight(float value)
	{
		dataManager.set(SCALE_HEIGHT, value);
	}
	
	public float getScaleWidth()
	{
		return dataManager.get(SCALE_WIDTH);
	}
	
	public void setScaleWidth(float value)
	{
		dataManager.set(SCALE_WIDTH, value);
	}
	
	public boolean getDoDisplay()
	{
		return dataManager.get(DO_DISPLAY);
	}
	
	public void setDoDisplay(boolean value)
	{
		dataManager.set(DO_DISPLAY, value);
	}
	
	public boolean getIsSwinging()
	{
		return dataManager.get(IS_SWINGING);
	}
	
	public void setIsSwinging(boolean value)
	{
		dataManager.set(IS_SWINGING, value);
	}
	
	public void swingItem() 
	{
		this.swingArm(EnumHand.MAIN_HAND);
	}
	
	@Override
	public void swingArm(EnumHand hand)
	{
		if (!getIsSwinging() || swingProgressTicks >= 8 / 2 || swingProgressTicks < 0)
		{
			swingProgressTicks = -1;
			setIsSwinging(true);
		}
	}
	
	public int getHeldItemSlot()
	{
		return dataManager.get(HELD_ITEM_SLOT);
	}
	
	public void setHeldItemSlot(int value)
	{
		dataManager.set(HELD_ITEM_SLOT, value);
	}
	
	public boolean getIsInfected()
	{
		return dataManager.get(IS_INFECTED);
	}
	
	public void setIsInfected(boolean value)
	{
		dataManager.set(IS_INFECTED, value);
	}
	
	public void cureInfection()
	{
		setIsInfected(false);
		addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, 0));
        world.playEvent((EntityPlayer)null, 1027, new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 0);
		Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.VILLAGER_HAPPY, this, 16);
	}
	
	public boolean getDoOpenInventory()
	{
		return dataManager.get(DO_OPEN_INVENTORY);
	}
	
	public void setDoOpenInventory(boolean value)
	{
		dataManager.set(DO_OPEN_INVENTORY, value);
	}
	
	public EnumMarriageState getMarriageState()
	{
		return EnumMarriageState.byId(dataManager.get(MARRIAGE_STATE));
	}
	
	/*package-private*/ void setSpouseUUID(UUID uuid)
	{
		dataManager.set(SPOUSE_UUID, Optional.of(uuid));
	}
	
	/*package-private*/ void setSpouseName(String value)
	{
		dataManager.set(SPOUSE_NAME, value);
	}
	
	/*package-private*/ void setSpouseGender(EnumGender gender)
	{
		dataManager.set(SPOUSE_GENDER, gender.getId());
	}
	
	/*package-private*/ void setParentName(boolean mother, String value)
	{
		DataParameter field = mother ? MOTHER_NAME : FATHER_NAME;
		dataManager.set(field, value);
	}

	/*package-private*/ void setParentUUID(boolean mother, UUID uuid)
	{
		DataParameter field = mother ? MOTHER_UUID: FATHER_UUID;
		dataManager.set(field, Optional.of(uuid));
	}
	
	/*package-private*/ void setParentGender(boolean mother, EnumGender gender)
	{
		DataParameter field = mother ? MOTHER_GENDER : FATHER_GENDER;
		dataManager.set(field, gender.getId());
	}
	
	/*package-private*/ void setMarriageState(EnumMarriageState state)
	{
		dataManager.set(MARRIAGE_STATE, state.getId());
	}
	
	/*****************************
	 * Other methods that have no particular category
	 */
	private void createMemorialChestForChild(PlayerMemory memory)
	{
		VillagerSaveData data = VillagerSaveData.fromVillager(this, null, memory.getUUID());
		ItemStack memorialStack = new ItemStack(getGender() == EnumGender.MALE ? ItemsMCA.toyTrain : ItemsMCA.childsDoll);
		
		memorialStack.setTagCompound(new NBTTagCompound());
		memorialStack.getTagCompound().setString("ownerName", memory.getPlayerName());
		memorialStack.getTagCompound().setInteger("relation", memory.getRelation().getId());
		data.writeDataToNBT(memorialStack.getTagCompound());
		
		createMemorialChest(memorialStack);
	}
	
	private void createMemorialChestForMarriedAdult()
	{		
		UUID ownerUUID = null;
		String ownerName = null;
		EnumRelation ownerRelation = null;
		Item memorialItem = null;
		EntityPlayer playerPartner = getPlayerSpouseInstance();
		
		if (playerPartner == null)
		{
			return;
		}
		
		ownerUUID = playerPartner.getPersistentID();
		ownerName = playerPartner.getName();
		ownerRelation = getPlayerMemory(playerPartner).getRelation();
		
		switch (ownerRelation)
		{
		case HUSBAND:
		case WIFE: memorialItem = ItemsMCA.brokenRing; break;
		case SON: memorialItem = ItemsMCA.toyTrain; break;
		case DAUGHTER: memorialItem = ItemsMCA.childsDoll; break;
		}
		
		if (memorialItem != null)
		{
			VillagerSaveData data = VillagerSaveData.fromVillager(this, null, ownerUUID);
			ItemStack memorialStack = new ItemStack(memorialItem);
			
			memorialStack.setTagCompound(new NBTTagCompound());
			memorialStack.getTagCompound().setString("ownerName", ownerName);
			memorialStack.getTagCompound().setInteger("relation", ownerRelation.getId());
			data.writeDataToNBT(memorialStack.getTagCompound());
			
			createMemorialChest(memorialStack);
		}
	}
	
	private void createMemorialChest(ItemStack memorialItem)
	{
		Point3D nearestAir = RadixLogic.getNearestBlock(this, 3, Blocks.AIR);
		
		if (nearestAir == null)
		{
			MCA.getLog().warn("No available location to spawn villager death chest for " + this.getName());
		}
		
		else
		{
			int y = nearestAir.iX();
			Block block = Blocks.AIR;
			
			while (block == Blocks.AIR)
			{
				y--;
				block = RadixBlocks.getBlock(world, nearestAir.iX(), y, nearestAir.iY());
			}
			
			y += 1;
			RadixBlocks.setBlock(world, nearestAir.iX(), y, nearestAir.iZ(), Blocks.CHEST);
			
			try
			{
				TileEntityChest chest = (TileEntityChest) world.getTileEntity(new BlockPos(nearestAir.iX(), y, nearestAir.iZ()));
				chest.setInventorySlotContents(0, memorialItem);
				MCA.getLog().info("Spawned villager death chest at: " + nearestAir.iX() + ", " + y + ", " + nearestAir.iZ());
			}
			
			catch (Exception e)
			{
				MCA.getLog().error("Error spawning villager death chest: " + e.getMessage());
				return;
			}
		}
	}

	public void setSizeOverride(float width, float height)
	{
		this.setSize(width, height);
		//this.setScale(0.935F); //FIXME
		
		//Sync size with all clients once set on the server.
		if (!world.isRemote)
		{
			MCA.getPacketHandler().sendPacketToAllPlayers(new PacketSetSize(this, width, height));
		}
	}
	
	public boolean isInOverworld()
	{
		return world.provider.getDimension() == 0;
	}
	
	public int getTicksAlive()
	{
		return ticksAlive;
	}

	public void setTicksAlive(int value)
	{
		this.ticksAlive = value;
	}
	
	public int getLowHeartWarnings()
	{
		return timesWarnedForLowHearts;
	}
	
	public void incrementLowHeartWarnings()
	{
		timesWarnedForLowHearts++;
	}
	
	public void resetLowHeartWarnings()
	{
		timesWarnedForLowHearts = 0;
	}
	
	@Deprecated
	public boolean getIsMale()
	{
		return getGender() == EnumGender.MALE;
	}

	@Deprecated
	public String getParentNames() 
	{
		return this.getMotherName() + "|" + this.getFatherName();
	}
	
	@Deprecated
	public boolean getIsMarried()
	{
		return getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER || getMarriageState() == EnumMarriageState.MARRIED_TO_PLAYER;
	}
	
	public boolean getCanBeHired(EntityPlayer player) 
	{
		return getPlayerSpouseInstance() != player && (getProfessionSkinGroup() == EnumProfessionSkinGroup.Farmer || 
				getProfessionSkinGroup() == EnumProfessionSkinGroup.Miner || 
				getProfessionSkinGroup() == EnumProfessionSkinGroup.Warrior);
	}

	public void assignRandomPersonality() 
	{
		setPersonality(EnumPersonality.getAtRandom());
	}
}
