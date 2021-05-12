package mca.entity.ai;

import mca.entity.EntityGrimReaper;
import mca.enums.EnumReaperAttackState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

public class GrimReaperCurse extends Goal {
    private final EntityGrimReaper reaper;
    private int blockTime;
    private int attackTime;

    private final static int COOLDOWN = 100;
    private int lastCurse;

    public GrimReaperCurse(EntityGrimReaper reaper) {
        this.reaper = reaper;
    }

    @Override
    public boolean canUse() {
        LivingEntity entityToAttack = reaper.getTarget();
        return entityToAttack != null && reaper.distanceTo(entityToAttack) <= 5.0D && reaper.tickCount > lastCurse + COOLDOWN;
    }

    @Override
    public boolean canContinueToUse() {
        return attackTime > 0;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        blockTime = reaper.getRandom().nextInt(300) + 100;
        attackTime = 100;
        lastCurse = reaper.tickCount;

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

        //start blocking phase
        reaper.setAttackState(EnumReaperAttackState.BLOCK);
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void tick() {
        // Don't block if we've already committed to an attack.
        blockTime--;
        if (blockTime > 0) {
            LivingEntity entityToAttack = reaper.getTarget();
            if (entityToAttack != null && reaper.distanceTo(entityToAttack) <= 1.5D) {
                // We are blocking, let's retreat
                int rX = reaper.getRandom().nextInt(10);
                int rY = reaper.getRandom().nextInt(4);
                int rZ = reaper.getRandom().nextInt(10);

                reaper.teleportTo(reaper.getX() - 5 + rX, reaper.getY() + rY, reaper.getZ() - 5 + rZ);
            }
        } else if (blockTime == 0) {
            //prepare attack
            reaper.setAttackState(EnumReaperAttackState.PRE);
        } else {
            attackTime--;
        }
    }
}
