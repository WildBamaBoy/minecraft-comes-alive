package mca.entity.ai;

import mca.entity.GrimReaperEntity;
import mca.enums.ReaperAttackState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

public class GrimReaperMeleeGoal extends Goal {
    private final static int COOLDOWN = 200;
    private final GrimReaperEntity reaper;
    private int blockDuration;
    private int attackDuration;
    private int retreatDuration;
    private int lastAttack = 0;

    public GrimReaperMeleeGoal(GrimReaperEntity reaper) {
        this.reaper = reaper;
    }

    @Override
    public boolean canUse() {
        LivingEntity entityToAttack = reaper.getTarget();
        return entityToAttack != null && reaper.distanceToSqr(entityToAttack) <= 144.0D && reaper.tickCount > lastAttack + COOLDOWN && reaper.getAttackState() != ReaperAttackState.REST;
    }

    @Override
    public boolean canContinueToUse() {
        return retreatDuration > 0 && reaper.getAttackState() != ReaperAttackState.REST;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        blockDuration = 60;
        attackDuration = 100;
        retreatDuration = 20;

        lastAttack = reaper.tickCount;
    }

    @Override
    public void stop() {
        super.stop();
        reaper.setAttackState(ReaperAttackState.IDLE);
    }

    //curse the player if he tries to block
    private void curse() {
        LivingEntity entityToAttack = reaper.getTarget();
        if (entityToAttack instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entityToAttack;

            // Check to see if the player's blocking, then teleport behind them.
            // Also randomly swap their selected item with something else in the hotbar and apply blindness.
            if (player.isBlocking()) {
                double dX = reaper.getX() - player.getX();
                double dZ = reaper.getZ() - player.getZ();

                reaper.teleportTo(player.getX() - (dX * 2), player.getY() + 2, reaper.getZ() - (dZ * 2));

                if (!reaper.level.isClientSide && reaper.getRandom().nextFloat() >= 0.20F) {
                    int currentItem = player.inventory.selected;
                    int randomItem = reaper.getRandom().nextInt(9);
                    ItemStack currentItemStack = player.inventory.getItem(currentItem);
                    ItemStack randomItemStack = player.inventory.getItem(randomItem);

                    player.inventory.setItem(currentItem, randomItemStack);
                    player.inventory.setItem(randomItem, currentItemStack);

                    entityToAttack.addEffect(new EffectInstance(Effects.BLINDNESS, 200));
                }
            }
        }
    }

    @Override
    public void tick() {
        LivingEntity entityToAttack = reaper.getTarget();
        if (entityToAttack == null) {
            retreatDuration = 0;
            return;
        }

        if (blockDuration > 0) {
            blockDuration--;
            reaper.setAttackState(ReaperAttackState.BLOCK);

            if (blockDuration == 0) {
                curse();
            }

            // We are blocking, let's retreat if someone comes too close
            if (reaper.distanceToSqr(entityToAttack) <= 4.0D) {
                int rX = reaper.getRandom().nextInt(10);
                int rY = reaper.getRandom().nextInt(6);
                int rZ = reaper.getRandom().nextInt(10);

                reaper.teleportTo(reaper.getX() - 5 + rX, reaper.getY() + rY, reaper.getZ() - 5 + rZ);
                reaper.getNavigation().stop();
            }

            //move up to get a nice overview
            Vector3d deltaMovement = reaper.getDeltaMovement();
            reaper.setDeltaMovement(new Vector3d(deltaMovement.x, 0.05, deltaMovement.z));
        } else if (attackDuration > 0) {
            attackDuration--;
            reaper.setAttackState(ReaperAttackState.PRE);

            //charge
            Vector3d dir = entityToAttack.position().subtract(reaper.position()).normalize().scale(0.15);
            reaper.push(dir.x, dir.y, dir.z);

            //attack
            if (reaper.distanceToSqr(entityToAttack) <= 1.0D) {
                reaper.swing(Hand.MAIN_HAND);
                attackDuration = 0;

                float damage = reaper.level.getDifficulty().getId() * 5.0F;
                entityToAttack.hurt(DamageSource.mobAttack(reaper), damage);

                //apply wither effect
                entityToAttack.addEffect(new EffectInstance(Effects.WITHER, 200));
            }
        } else {
            retreatDuration--;
            reaper.setAttackState(ReaperAttackState.POST);

            //retreat
            Vector3d dir = entityToAttack.position().subtract(reaper.position()).normalize().scale(-0.1);
            reaper.setDeltaMovement(dir.x, dir.y, dir.z);
        }
    }
}
