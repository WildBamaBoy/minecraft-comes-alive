package mca.entity.ai.goal;

import mca.entity.GrimReaperEntity;
import mca.entity.ReaperAttackState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

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
    public boolean canStart() {
        LivingEntity entityToAttack = reaper.getTarget();
        return entityToAttack != null && reaper.squaredDistanceTo(entityToAttack) <= 144.0D && reaper.age > lastAttack + COOLDOWN && reaper.getAttackState() != ReaperAttackState.REST;
    }

    @Override
    public boolean shouldContinue() {
        return retreatDuration > 0 && reaper.getAttackState() != ReaperAttackState.REST;
    }

    @Override
    public boolean canStop() {
        return false;
    }

    @Override
    public void start() {
        blockDuration = 60;
        attackDuration = 100;
        retreatDuration = 20;

        lastAttack = reaper.age;
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

                reaper.requestTeleport(player.getX() - (dX * 2), player.getY() + 2, reaper.getZ() - (dZ * 2));

                if (!reaper.world.isClient && reaper.getRandom().nextFloat() >= 0.20F) {
                    int currentItem = player.inventory.selectedSlot;
                    int randomItem = reaper.getRandom().nextInt(9);
                    ItemStack currentItemStack = player.inventory.getStack(currentItem);
                    ItemStack randomItemStack = player.inventory.getStack(randomItem);

                    player.inventory.setStack(currentItem, randomItemStack);
                    player.inventory.setStack(randomItem, currentItemStack);

                    entityToAttack.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200));
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
            if (reaper.squaredDistanceTo(entityToAttack) <= 4.0D) {
                int rX = reaper.getRandom().nextInt(10);
                int rY = reaper.getRandom().nextInt(6);
                int rZ = reaper.getRandom().nextInt(10);

                reaper.requestTeleport(reaper.getX() - 5 + rX, reaper.getY() + rY, reaper.getZ() - 5 + rZ);
                reaper.getNavigation().stop();
            }

            //move up to get a nice overview
            Vec3d deltaMovement = reaper.getVelocity();
            reaper.setVelocity(new Vec3d(deltaMovement.x, 0.05, deltaMovement.z));
        } else if (attackDuration > 0) {
            attackDuration--;
            reaper.setAttackState(ReaperAttackState.PRE);

            //charge
            Vec3d dir = entityToAttack.getPos().subtract(reaper.getPos()).normalize().multiply(0.15);
            reaper.addVelocity(dir.x, dir.y, dir.z);

            //attack
            if (reaper.squaredDistanceTo(entityToAttack) <= 1.0D) {
                reaper.swingHand(Hand.MAIN_HAND);
                attackDuration = 0;

                float damage = reaper.world.getDifficulty().getId() * 5.0F;
                entityToAttack.damage(DamageSource.mob(reaper), damage);

                //apply wither effect
                entityToAttack.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 200));
            }
        } else {
            retreatDuration--;
            reaper.setAttackState(ReaperAttackState.POST);

            //retreat
            Vec3d dir = entityToAttack.getPos().subtract(reaper.getPos()).normalize().multiply(-0.1);
            reaper.setVelocity(dir.x, dir.y, dir.z);
        }
    }
}
