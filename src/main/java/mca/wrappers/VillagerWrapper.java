package mca.wrappers;

import cobalt.enums.CEnumHand;
import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.inventory.CEquipmentSlotType;
import cobalt.minecraft.item.CItemStack;
import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.util.CDamageSource;
import cobalt.minecraft.util.math.CPos;
import cobalt.minecraft.world.CWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;


public abstract class VillagerWrapper extends VillagerEntity {
    public CWorld world;

    public VillagerWrapper(EntityType<? extends VillagerWrapper> entityType, World worldIn) {
        super(entityType, worldIn);
        this.world = CWorld.fromMC(worldIn);
    }

    // Implementation of methods not reliant on villager-specific information.
    @Override
    public final boolean hasCustomName() {
        return true;
    }

    // Superclass callers with renamed overriding methods
    @Override
    protected final void defineSynchedData() {
        super.defineSynchedData();
        initialize();
    }

    @Override
    public final void tick() {
        super.tick();
        onUpdate();
    }

    @Override
    public final boolean hurt(DamageSource damageSrc, float damageAmount) {
        CDamageSource wrappedSource = CDamageSource.fromMC(damageSrc);
        damageAmount = beforeDamaged(wrappedSource, damageAmount);
        super.hurt(damageSrc, damageAmount);
        return afterDamaged(wrappedSource, damageAmount);
    }

//    @Override
//    public final void swingArm(Hand hand) {
//        this.setActiveHand(hand);
//        super.swingArm(hand);
//        swing(CEnumHand.fromMC(hand));
//    }

//    @Override
//    public final boolean attackEntityAsMob(Entity e) {
//        return this.attack(CEntity.fromMC(e));
//    }


    @Override
    public final void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        load(CNBT.fromMC(nbt));
    }

    ;

    @Override
    public final void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        save(CNBT.fromMC(nbt));
    }

    ;

    @Override
    protected final SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected final SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    public final ActionResultType interactAt(PlayerEntity player, Vector3d pos, @Nonnull Hand hand) {
        onRightClick(CPlayer.fromMC(player), CEnumHand.fromMC(hand));
        return ActionResultType.sidedSuccess(true);
    }

    public final BlockPos getHomePosition() {
        return getHomePos().getMcPos();
    }

    public final ItemStack getItemStackFromSlot(EquipmentSlotType type) {
        return getEquipmentOfType(CEquipmentSlotType.fromMC(type)).getMcItemStack();
    }

    @Override
    public final SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

//    @Override public final void handleDeath(DamageSource source) {
//        handleDeath(CDamageSource.fromMC(source));
//    }


    @Override
    public final ITextComponent getDisplayName() {
        return new StringTextComponent(getNameForDisplay());
    }

    public final VillagerProfession getProfession() {
        return this.getVillagerData().getProfession();
    }

    public final void setProfession(VillagerProfession profession) {
        this.setVillagerData(this.getVillagerData().setProfession(profession));
    }

    public CPos getPos() {
        return CPos.fromMC(getOnPos());
    }

    @Override
    public final ITextComponent getCustomName() {
        return new StringTextComponent(getVillagerName());
    }

    // Methods required to be overridden
    protected abstract void initialize();

    public abstract void onUpdate();

    public abstract void swing(CEnumHand hand);

    protected abstract void initializeAI();

    protected abstract float beforeDamaged(CDamageSource source, float value);

    protected abstract boolean afterDamaged(CDamageSource source, float value);

    public abstract CItemStack getEquipmentOfType(CEquipmentSlotType type);

    protected abstract void onGrowingAdult();

    public abstract String getNameForDisplay();

    public abstract String getVillagerName();

    public abstract void handleDeath(CDamageSource source);

    public abstract void onRightClick(CPlayer player, CEnumHand hand);

    public abstract boolean attack(CEntity e);

    public abstract void load(CNBT nbt);

    public abstract void save(CNBT nbt);

    public abstract CPos getHomePos();

    public boolean isChild() {
        return false;
    }
}
