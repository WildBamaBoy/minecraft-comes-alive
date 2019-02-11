package mca.entity;

import static mca.core.Constants.EMPTY_UUID;
import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.getPrivateValue;
import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.setPrivateValue;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mca.actions.AbstractAction;
import mca.actions.ActionAttackResponse;
import mca.actions.ActionCombat;
import mca.actions.ActionSleep;
import mca.actions.ActionUpdateMood;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.data.TransitiveVillagerData;
import mca.enums.EnumBabyState;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMovementState;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionSkinGroup;
import mca.enums.EnumRelation;
import mca.items.ItemBaby;
import mca.items.ItemMemorial;
import mca.items.ItemVillagerEditor;
import mca.packets.PacketOpenGUIOnEntity;
import mca.util.Either;
import mca.util.Utilities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Font.Color;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;

/**
 * The main class of MCA's villager. The class itself handles events, getters, setters, etc.
 * overridden from Minecraft. Also any events/actions that can be performed on a villager.
 * 
 * To avoid an absurdly large class, the rest of the villager is split into 2 components:
 * 
 * The VillagerBehaviors object handles custom villager behaviors that run each tick.
 * 
 * The VillagerAttributes object holds all villager data and their getters/setters.
 */
public class EntityVillagerMCA extends EntityVillager implements IEntityAdditionalSpawnData
{
	@SideOnly(Side.CLIENT)
	public boolean isInteractionGuiOpen;

	private int swingProgressTicks;
	public final VillagerAttributes attributes;
	private final VillagerBehaviors behaviors;
	private final Profiler profiler;

	private static final int FIELD_INDEX_BUYING_PLAYER = 6;
	private static final int FIELD_INDEX_BUYING_LIST = 7;
	private static final int FIELD_INDEX_TIME_UNTIL_RESET = 8;
	private static final int FIELD_INDEX_NEEDS_INITIALIZATION = 9;
	private static final int FIELD_INDEX_IS_WILLING_TO_MATE = 10;
	private static final int FIELD_INDEX_WEALTH = 11;
	private static final int FIELD_INDEX_LAST_BUYING_PLAYER = 12;
	private static final int FIELD_INDEX_CAREER_ID = 13;
	private static final int FIELD_INDEX_CAREER_LEVEL = 14;
	
	public EntityVillagerMCA(World world) 
	{
		super(world);
		
		profiler = world.profiler;
		attributes = new VillagerAttributes(this);
		attributes.initialize();
		behaviors = new VillagerBehaviors(this);
		
		addAI();
	}

	public void addAI()
	{
		this.tasks.taskEntries.clear();

        ((PathNavigateGround)this.getNavigator()).setCanSwim(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(4, new EntityAIOpenDoor(this, true));

		int maxHealth = attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard ? MCA.getConfig().guardMaxHealth : MCA.getConfig().villagerMaxHealth;
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);

		if (this.getHealth() > maxHealth || attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard)
		{
			this.setHealth(maxHealth);
		}

		if (attributes.getProfessionSkinGroup() != EnumProfessionSkinGroup.Guard)
		{
			this.tasks.addTask(2, new EntityAIMoveIndoors(this));
		}
	}

	private void updateSwinging()
	{
		if (attributes.getIsSwinging())
		{
			swingProgressTicks++;

			if (swingProgressTicks >= 8)
			{
				swingProgressTicks = 0;
				attributes.setIsSwinging(false);
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
    }
    
	@Override
	public void onUpdate()
	{
		super.onUpdate();

		profiler.startSection("MCA Villager Update");
		behaviors.onUpdate();
		updateSwinging();
		
		if (!world.isRemote)
		{
			attributes.incrementTicksAlive();

			//Tick player memories
			for (PlayerMemory memory : attributes.getPlayerMemories().values())
			{
				memory.doTick();
			}

			//Tick babies in attributes.getInventory().
			for (int i = 0; i < attributes.getInventory().getSizeInventory(); i++)
			{
				ItemStack stack = attributes.getInventory().getStackInSlot(i);

				if (stack.getItem() instanceof ItemBaby)
				{
					ItemBaby item = (ItemBaby)stack.getItem();
					item.onUpdate(stack, world, this, 1, false);
				}
			}

			//Check if inventory should be opened for player.
			if (attributes.getDoOpenInventory())
			{
				final EntityPlayer player = world.getClosestPlayerToEntity(this, 10.0D);

				if (player != null)
				{
					player.openGui(MCA.getInstance(), Constants.GUI_ID_INVENTORY, world, (int)posX, (int)posY, (int)posZ);
				}

				attributes.setDoOpenInventory(false);
			}
		}
		
		profiler.endSection();
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
				TransitiveVillagerData transitiveData = new TransitiveVillagerData(attributes);
				NBTTagCompound stackNBT = new NBTTagCompound();
				stackNBT.setUniqueId("ownerUUID", player.getUniqueID());
				stackNBT.setString("ownerName", player.getName());
				stackNBT.setInteger("relation", attributes.getPlayerMemory(player).getRelation().getId());
				transitiveData.writeToNBT(stackNBT);
				
				heldItem.setTagCompound(stackNBT);
				
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
			behaviors.disableAllToggleActions();
			getBehavior(ActionSleep.class).transitionSkinState(true);
			
			//The death of a villager negatively modifies the mood of nearby villagers
			for (EntityVillagerMCA human : RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, this, 20))
			{
				human.getBehavior(ActionUpdateMood.class).modifyMoodLevel(-2.0F);
			}

			//Drop all items in the inventory
			for (int i = 0; i < attributes.getInventory().getSizeInventory(); i++)
			{
				ItemStack stack = attributes.getInventory().getStackInSlot(i);

				if (stack != null)
				{
					entityDropItem(stack, 1.0F);
				}
			}
			
			//Reset the marriage stats of the player/villager this one was married to
			//If married to a player, this player takes priority in receiving the memorial item for revival.
			boolean memorialDropped = false;
			
			if (attributes.isMarriedToAPlayer()) 	
			{
				NBTPlayerData playerData = MCA.getPlayerData(world, attributes.getSpouseUUID());
				
				playerData.setMarriageState(EnumMarriageState.NOT_MARRIED);
				playerData.setSpouseName("");
				playerData.setSpouseUUID(EMPTY_UUID);
				
				//Just in case something is added here later, be sure we're not false
				if (!memorialDropped)
				{
					createMemorialChest(attributes.getPlayerMemoryWithoutCreating(attributes.getSpouseUUID()), ItemsMCA.BROKEN_RING);
					memorialDropped = true;
				}
			}

			else if (attributes.isMarriedToAVillager())
			{
				EntityVillagerMCA partner = attributes.getVillagerSpouseInstance();

				if (partner != null)
				{
					partner.endMarriage();
				}
			}

			//Alert parents/spouse of the death if they are online and handle dropping memorials
			//Test against new iteration of player memory list each time to ensure the proper order
			//of handling notifications and memorial spawning
			
			for (PlayerMemory memory : attributes.getPlayerMemories().values())
			{
				//Alert parents and spouse of the death.
				if (memory.getUUID().equals(attributes.getSpouseUUID()) || attributes.isPlayerAParent(memory.getUUID()))
				{
					EntityPlayer player = world.getPlayerEntityByUUID(memory.getUUID());
					
					//If we hit a parent
					if (attributes.isPlayerAParent(memory.getUUID()) && !memorialDropped)
					{
						createMemorialChest(memory, attributes.getGender() == EnumGender.MALE ? ItemsMCA.TOY_TRAIN : ItemsMCA.CHILDS_DOLL);
						memorialDropped = true;
					}
					
					if (player != null) //The player may not be online
					{
						player.sendMessage(new TextComponentString(Color.RED + attributes.getTitle(player) + " has died."));
					}
				}
			}
		}
	}

	private void createMemorialChest(PlayerMemory memory, ItemMemorial memorialItem)
	{
		Point3D nearestAir = RadixLogic.getNearestBlock(this, 3, Blocks.AIR);
    	
    	if (nearestAir == null)
    	{
    		MCA.getLog().warn("No available location to spawn villager death chest for " + this.getName());
    	}
    	
    	else
    	{
    		int y = nearestAir.iY();
    		Block block = Blocks.AIR;
    		
    		while (block == Blocks.AIR)
    		{
    			y--;
    			block = world.getBlockState(new BlockPos(nearestAir.iX(), y, nearestAir.iZ())).getBlock();
    		}
    		
    		y += 1;
    		world.setBlockState(new BlockPos(nearestAir.iX(), y, nearestAir.iZ()), Blocks.CHEST.getDefaultState());
    		
    		try
    		{
    			TileEntityChest chest = (TileEntityChest) world.getTileEntity(nearestAir.toBlockPos());
    			TransitiveVillagerData data = new TransitiveVillagerData(attributes);
    			ItemStack memorialStack = new ItemStack(memorialItem);
    			NBTTagCompound stackNBT = new NBTTagCompound();
    			
    			stackNBT.setString("ownerName", memory.getPlayerName());
    			stackNBT.setUniqueId("ownerUUID", memory.getUUID());
    			stackNBT.setInteger("ownerRelation", memory.getRelation().getId());
    			data.writeToNBT(stackNBT);
    			memorialStack.setTagCompound(stackNBT);
    			
    			chest.setInventorySlotContents(0, memorialStack);
    			MCA.getLog().info("Spawned villager death chest at: " + nearestAir.iX() + ", " + y + ", " + nearestAir.iZ());
    		}
    		
    		catch (Exception e)
    		{
    			MCA.getLog().error("Error spawning villager death chest: " + e.getMessage());
    			return;
    		}
    	}
	}
    
	@Override
	protected void updateAITasks()
	{
		ActionSleep sleepAI = getBehavior(ActionSleep.class);
		EnumMovementState moveState = attributes.getMovementState();
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
		
		behaviors.getAction(ActionAttackResponse.class).startResponse(damageSource.getImmediateSource());
		behaviors.getAction(ActionSleep.class).onDamage();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) 
	{
		super.writeEntityToNBT(nbt);
		behaviors.writeToNBT(nbt);
		attributes.writeToNBT(nbt);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		behaviors.readFromNBT(nbt);
		attributes.readFromNBT(nbt);
		addAI();
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) 
	{
		attributes.writeSpawnData(buffer);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) 
	{
		attributes.readSpawnData(buffer);
	}

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
	protected SoundEvent getHurtSound(DamageSource source) 
	{
		return attributes.getIsInfected() ? SoundEvents.ENTITY_ZOMBIE_HURT : null;
	}

	@Override
	protected SoundEvent getDeathSound() 
	{
		return attributes.getIsInfected() ? SoundEvents.ENTITY_ZOMBIE_DEATH : null;
	}

	@Override
	public boolean canBePushed()
	{
		final ActionSleep sleepAI = behaviors.getAction(ActionSleep.class);		
		return !sleepAI.getIsSleeping();
	}

	@Override
	protected boolean canDespawn() 
	{
		return false;
	}

	public void sayRaw(String text, EntityPlayer target)
	{
		final StringBuilder sb = new StringBuilder();

		if (MCA.getConfig().villagerChatPrefix != null && !MCA.getConfig().villagerChatPrefix.equals("null"))
		{
			sb.append(MCA.getConfig().villagerChatPrefix);
		}

		sb.append(attributes.getTitle(target));
		sb.append(": ");
		sb.append(text);

		if (target != null)
		{
			target.sendMessage(new TextComponentString(sb.toString()));
		}

		behaviors.onSay();
	}

	public void say(String phraseId, EntityPlayer target, Object... arguments)
	{
		if (target == null)
		{
			return;
		}

		if (attributes.getIsInfected()) //Infected villagers moan when they speak, and will not say anything else.
		{
			String zombieMoan = RadixLogic.getBooleanWithProbability(33) ? "Raagh..." : RadixLogic.getBooleanWithProbability(33) ? "Ughh..." : "Argh-gur...";
			target.sendMessage(new TextComponentString(attributes.getTitle(target) + ": " + zombieMoan));
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
			sb.append(attributes.getTitle(target));
			sb.append(": ");
			sb.append(MCA.getLocalizer().getString(phraseId, arguments));

			target.sendMessage(new TextComponentString(sb.toString()));

			behaviors.onSay();
		}
	}

	public void say(String phraseId, EntityPlayer target)
	{
		say(phraseId, target, this, target);
	}

	/**
	 * Sets the given entity to be the spouse of the current villager. This is symmetric against the provided entity.
	 * If null is provided, this villager's spouse information will be reset. This is **NOT** symmetric.
	 * 
	 * @param 	either	Either object containing an MCA villager or a player.
	 */
	public void startMarriage(Either<EntityVillagerMCA, EntityPlayer> either)
	{
		if (either.getLeft() != null)
		{
			EntityVillagerMCA spouse = either.getLeft();

			attributes.setSpouseName(spouse.attributes.getName());
			attributes.setSpouseUUID(spouse.getUniqueID());
			attributes.setSpouseGender(spouse.attributes.getGender());
			attributes.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);

			spouse.attributes.setSpouseName(this.attributes.getName());
			spouse.attributes.setSpouseUUID(this.getUniqueID());
			spouse.attributes.setSpouseGender(this.attributes.getGender());
			spouse.attributes.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);
			
			getBehaviors().onMarriageToVillager();
		}

		else if (either.getRight() != null)
		{
			EntityPlayer player = either.getRight();
			NBTPlayerData playerData = MCA.getPlayerData(player);
			PlayerMemory memory = attributes.getPlayerMemory(player);
			
			attributes.setSpouseName(player.getName());
			attributes.setSpouseUUID(player.getUniqueID());
			attributes.setSpouseGender(playerData.getGender());
			attributes.setMarriageState(EnumMarriageState.MARRIED_TO_PLAYER);
			memory.setDialogueType(EnumDialogueType.SPOUSE);
			memory.setRelation(attributes.getGender() == EnumGender.MALE ? EnumRelation.HUSBAND : EnumRelation.WIFE);
			
			playerData.setSpouseName(this.getName());
			playerData.setSpouseGender(attributes.getGender());
			playerData.setSpouseUUID(this.getUniqueID());
			playerData.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);

			getBehaviors().onMarriageToPlayer();
		}
		
		else
		{
			throw new IllegalArgumentException("Marriage target cannot be null");
		}
	}

	public void endMarriage()
	{
		//Reset spouse information back to default
		attributes.setSpouseName("");
		attributes.setSpouseUUID(EMPTY_UUID);
		attributes.setSpouseGender(EnumGender.UNASSIGNED);
		attributes.setMarriageState(EnumMarriageState.NOT_MARRIED);

		getBehaviors().onMarriageEnded();
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

	public VillagerBehaviors getBehaviors() 
	{
		return behaviors;
	}

	public <T extends AbstractAction> T getBehavior(Class<T> clazz)
	{
		return this.behaviors.getAction(clazz);
	}
	
	@Override
	public ItemStack getHeldItem(EnumHand hand)
	{
		EnumBabyState babyState = attributes.getBabyState();
		EnumProfession profession = attributes.getProfessionEnum();
		
		if (attributes.getIsInfected())
		{
			return ItemStack.EMPTY;
		}

		else if (babyState != EnumBabyState.NONE)
		{
			return new ItemStack(babyState == EnumBabyState.MALE ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
		}

		else if (profession == EnumProfession.Guard)
		{
			return new ItemStack(Items.IRON_SWORD);
		}

		else if (profession == EnumProfession.Archer)
		{
			return new ItemStack(Items.BOW);
		}

		else if (attributes.getHeldItemSlot() != -1 && behaviors.isToggleActionActive())
		{
			return attributes.getInventory().getStackInSlot(attributes.getHeldItemSlot());
		}

		else if (attributes.getInventory().contains(ItemsMCA.BABY_BOY) || attributes.getInventory().contains(ItemsMCA.BABY_GIRL))
		{
			int slot = attributes.getInventory().getFirstSlotContainingItem(ItemsMCA.BABY_BOY);
			slot = slot == -1 ? attributes.getInventory().getFirstSlotContainingItem(ItemsMCA.BABY_GIRL) : slot;

			if (slot != -1)
			{
				return attributes.getInventory().getStackInSlot(slot);
			}
		}

		//Warriors, spouses, and player children all use weapons from the combat AI.
		else if (profession == EnumProfession.Warrior || attributes.isMarriedToAPlayer() || profession == EnumProfession.Child)
		{
			return getBehavior(ActionCombat.class).getHeldItem();
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
				int slot = attributes.getInventory().getFirstSlotContainingItem(item);

				ItemStack itemInSlot = attributes.getInventory().getStackInSlot(slot);

				if (itemInSlot != null)
				{
					itemInSlot.damageItem(amount, this);

					if (itemInSlot.getCount() == 0)
					{
						behaviors.disableAllToggleActions();
						attributes.getInventory().setInventorySlotContents(slot, ItemStack.EMPTY);
						return true;
					}

					else
					{
						attributes.getInventory().setInventorySlotContents(slot, itemInSlot);
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
	
	@Override
	public Iterable<ItemStack> getHeldEquipment()
	{
		List<ItemStack> heldEquipment = new ArrayList<ItemStack>();
		heldEquipment.add(getHeldItem(EnumHand.MAIN_HAND));
		return heldEquipment;
	}

	@Override
	public Iterable<ItemStack> getArmorInventoryList()
	{
		List<ItemStack> armorInventory = new ArrayList<ItemStack>();
		armorInventory.add(attributes.getInventory().getStackInSlot(39));
		armorInventory.add(attributes.getInventory().getStackInSlot(38));
		armorInventory.add(attributes.getInventory().getStackInSlot(37));
		armorInventory.add(attributes.getInventory().getStackInSlot(36));

		return armorInventory;
	}

	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn)
	{
		switch (slotIn)
		{
		case HEAD: return attributes.getInventory().getStackInSlot(36);
		case CHEST: return attributes.getInventory().getStackInSlot(37);
		case LEGS: return attributes.getInventory().getStackInSlot(38);
		case FEET: return attributes.getInventory().getStackInSlot(39);
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
			final ItemStack stack = attributes.getInventory().getStackInSlot(i);

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
			final ItemStack stack = attributes.getInventory().getStackInSlot(i);

			if (stack != null && stack.getItem() instanceof ItemArmor)
			{
				stack.damageItem((int) amount, this);
			}
		}	
	}
	
	public void swingItem() 
	{
		this.swingArm(EnumHand.MAIN_HAND);
	}
	
	@Override
	public void swingArm(EnumHand hand)
	{
		if (!attributes.getIsSwinging() || swingProgressTicks >= 8 / 2 || swingProgressTicks < 0)
		{
			swingProgressTicks = -1;
			attributes.setIsSwinging(true);
		}
	}
	
	public void cureInfection()
	{
		attributes.setIsInfected(false);
		addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, 0));
        world.playEvent((EntityPlayer)null, 1027, new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 0);
		Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.VILLAGER_HAPPY, this, 16);
	}

	public boolean isInOverworld()
	{
		return world.provider.getDimension() == 0;
	}

	public Profiler getProfiler()
	{
		return profiler;
	}

	public void setHitboxSize(float width, float height)
	{
		this.setSize(width, height);
	}
	
	@Override
	public String getName()
	{
		return this.attributes.getName();
	}
    
    public void setCareer(int career)
    {    	   	
    	setEntityVillagerField(FIELD_INDEX_CAREER_ID, Integer.valueOf(career));
    	
    	if ((Integer)getEntityVillagerField(FIELD_INDEX_CAREER_LEVEL) == 0)
    	{
    		setEntityVillagerField(FIELD_INDEX_CAREER_LEVEL, 1);
    	}
    	
    	repopulateBuyingList();
    }
    
	public void repopulateBuyingList()
	{
		MerchantRecipeList buyingList = new MerchantRecipeList();
		
		setEntityVillagerField(FIELD_INDEX_BUYING_LIST, buyingList);
		
		for (int i = 1; i <= (Integer)getEntityVillagerField(FIELD_INDEX_CAREER_LEVEL); i++)
		{
			java.util.List<EntityVillager.ITradeList> trades = this.getProfessionForge().getCareer((Integer)getEntityVillagerField(FIELD_INDEX_CAREER_ID)-1).getTrades(i-1);
			
	        if (trades != null)
	        {
	            for (EntityVillager.ITradeList entityvillager$itradelist : trades)
	            {
	                entityvillager$itradelist.addMerchantRecipe(this, buyingList, this.getRNG());
	            }
	        }
	        else
	        	MCA.getLog().warn("Failed to retrieve default trade list for villager " + this.getName());
		}
	}

    @Override
	public void useRecipe(MerchantRecipe recipe)
	{
        recipe.incrementToolUses();
        int i = 3 + this.rand.nextInt(4);

        EntityPlayer buyingPlayer = getPrivateValue(EntityVillager.class, this, FIELD_INDEX_BUYING_PLAYER);
        
        if (recipe.getToolUses() == 1 || this.rand.nextInt(5) == 0)
        {
        	//timeUntilReset = 40;
        	setEntityVillagerField(FIELD_INDEX_TIME_UNTIL_RESET, Integer.valueOf(40));
        	//needsInitialization = true;
        	setEntityVillagerField(FIELD_INDEX_NEEDS_INITIALIZATION, true);
        	//isWillingToMate = true; (replaced with false to prevent any possible vanilla villager mating)
        	setEntityVillagerField(FIELD_INDEX_IS_WILLING_TO_MATE, false);

            if (buyingPlayer != null) //this.buyingPlayer != null
            {
                //this.lastBuyingPlayer = this.buyingPlayer.getUniqueID();
            	setEntityVillagerField(FIELD_INDEX_LAST_BUYING_PLAYER, buyingPlayer.getUniqueID());
            }
            else
            {
            	//this.lastBuyingPlayer = null;
            	setEntityVillagerField(FIELD_INDEX_LAST_BUYING_PLAYER, null);
            }

            i += 5;
        }

        if (recipe.getItemToBuy().getItem() == Items.EMERALD)
        {
        	//wealth += recipe.getItemToBuy().getCount();
        	int wealth = getEntityVillagerField(FIELD_INDEX_WEALTH);
        	setEntityVillagerField(FIELD_INDEX_WEALTH, wealth + recipe.getItemToBuy().getCount());
        }

        if (recipe.getRewardsExp())
        {
            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY + 0.5D, this.posZ, i));
        }

        if (buyingPlayer instanceof EntityPlayerMP)
        {
            CriteriaTriggers.VILLAGER_TRADE.trigger((EntityPlayerMP)buyingPlayer, this, recipe.getItemToSell());
        }
	}
	
	private <T, E> void setEntityVillagerField(int fieldIndex, Object value) {
		setPrivateValue(EntityVillager.class, this, value, fieldIndex);
	}
	
	private <T, E> T getEntityVillagerField(int fieldIndex) {
		return getPrivateValue(EntityVillager.class, this, fieldIndex);
	}
}
