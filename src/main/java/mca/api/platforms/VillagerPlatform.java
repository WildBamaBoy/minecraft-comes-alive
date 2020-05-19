package mca.api.platforms;

import javax.annotation.Nonnull;

import mca.api.objects.NPC;
import mca.api.objects.Pos;
import mca.api.wrappers.DataManagerWrapper;
import mca.api.wrappers.NBTWrapper;
import mca.api.wrappers.WorldWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

public abstract class VillagerPlatform extends EntityVillager {
    private static final int VANILLA_CAREER_ID_FIELD_INDEX = 13;
    private static final int VANILLA_CAREER_LEVEL_FIELD_INDEX = 14;
    
	public WorldWrapper world;
	protected DataManagerWrapper dataManager;

	public VillagerPlatform(World worldIn) {
		super(worldIn);
		this.world = new WorldWrapper(worldIn);
	}
	
	// Implementation of methods not reliant on villager-specific information.
	@Override 
	public final boolean hasCustomName() {
		return true;
	}
	
	// Superclass callers with renamed overriding methods
	@Override
	protected final void entityInit() {
		super.entityInit();
		initialize();
	}
	
	@Override
	protected final void applyEntityAttributes() {
		super.applyEntityAttributes();
		applyAttributes();
	}
	
	@Override 
	public final void onUpdate() {
		super.onUpdate();
		update();
	}
	
	@Override
    protected final void damageEntity(DamageSource damageSrc, float damageAmount) {
		damageAmount = applyDamageModifications(damageSrc, damageAmount);
		super.damageEntity(damageSrc, damageAmount);
		afterApplyingDamage(damageSrc, damageAmount);
	}
	
	@Override
	public final void swingArm(EnumHand hand) {
        this.setActiveHand(hand);
        super.swingArm(hand);
        swing(hand);
	}
	
    @Override
    public final void detachHome() {
        // no-op, skip EntityVillager's detaching homes which messes up MoveTowardsRestriction.
    }
    
    @Override
    public final void initEntityAI() {
    	super.initEntityAI();
    	initializeAI();
    }
    
	@Override 
	public final boolean attackEntityAsMob(Entity e) { 
		return this.attackNPC(new NPC(e)); 
	}
	
	@Override 
	public final void readEntityFromNBT(NBTTagCompound nbt) { 
		super.readEntityFromNBT(nbt);
		load(new NBTWrapper(nbt));
	};
	
	@Override 
	public final void writeEntityToNBT(NBTTagCompound nbt) { 
		super.writeEntityToNBT(nbt);
		save(new NBTWrapper(nbt));
	};
	
    @Override
    protected final SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected final SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_GENERIC_HURT;
    }
    
    @Override
    public final boolean processInteract(EntityPlayer player, @Nonnull EnumHand hand) {
        // No-op, handled by EventHooks
        return true;
    }
    
	@Override 
	public final BlockPos getHomePosition() {
		return getHomePos().getBlockPos();
	}
	
	// Helper methods
	public <T> T get(DataParameter<T> key) {
        return this.dataManager.getVanillaManager().get(key);
    }

    public <T> void set(DataParameter<T> key, T value) {
        this.dataManager.getVanillaManager().set(key, value);
    }
    
	public final void setCareerId(int careerId) {
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, careerId, VillagerPlatform.VANILLA_CAREER_ID_FIELD_INDEX);
	}

	public final int getCareerId() {
    	return ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, this, VillagerPlatform.VANILLA_CAREER_ID_FIELD_INDEX);
	}

	public final void setCareerLevel(int value) {
		ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, value, VANILLA_CAREER_LEVEL_FIELD_INDEX);
	}

	public final int getCareerLevel() {
    	return ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, this, VANILLA_CAREER_LEVEL_FIELD_INDEX);
	}

	// Methods required to be overridden
	protected abstract void initialize();
	protected abstract void applyAttributes();
	public abstract void update();
	public abstract void swing(EnumHand hand);
	protected abstract void initializeAI();
	protected abstract float applyDamageModifications(DamageSource source, float value);
	protected abstract void afterApplyingDamage(DamageSource source, float value);
	@Override protected abstract SoundEvent getDeathSound();
	@Override protected abstract void onGrowingAdult();
	@Override public abstract ITextComponent getDisplayName();
	@Override public abstract String getCustomNameTag();
	@Override public abstract ItemStack getItemStackFromSlot(EntityEquipmentSlot slot);
	@Override public abstract void onDeath(DamageSource source);
	
	//Platform methods
	public abstract boolean attackNPC(NPC e);
	public abstract void load(NBTWrapper nbt);
	public abstract void save(NBTWrapper nbt);
	public abstract Pos getHomePos();
	
	// New methods
    public VillagerRegistry.VillagerCareer getVanillaCareer() {
        return this.getProfessionForge().getCareer(ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, this, VANILLA_CAREER_ID_FIELD_INDEX));
    }

    public void setVanillaCareer(int careerId) {
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, careerId, VANILLA_CAREER_ID_FIELD_INDEX);
    }
    
    public void setSizePublic(float width, float height) {
    	super.setSize(width, height);
    }

    public double getDistanceSq(Pos pos) {
    	return getDistanceSq(pos.getBlockPos());
	}
}
