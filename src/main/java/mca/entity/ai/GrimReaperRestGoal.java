package mca.entity.ai;

import mca.entity.GrimReaperEntity;
import mca.enums.ReaperAttackState;
import mca.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrimReaperRestGoal extends Goal {
    private final static int COOLDOWN = 1000;
    private final GrimReaperEntity reaper;
    private int lastHeal = -COOLDOWN;
    private int healingCount = 0;
    private int healingTime;

    public GrimReaperRestGoal(GrimReaperEntity reaper) {
        this.reaper = reaper;
    }

    @Override
    public boolean canStart() {
        return reaper.age > lastHeal + COOLDOWN && reaper.getHealth() <= (reaper.getMaxHealth() / (healingCount + 1.5));
    }

    @Override
    public boolean shouldContinue() {
        return healingTime > 0;
    }

    @Override
    public boolean canStop() {
        return false;
    }

    @Override
    public void start() {
        reaper.requestTeleport(reaper.offsetX(), reaper.getBodyY() + 8, reaper.offsetZ());

        healingTime = 500;
        lastHeal = reaper.age;
        healingCount++;
    }

    @Override
    public void stop() {
        reaper.setAttackState(ReaperAttackState.IDLE);
    }

    @Override
    public void tick() {
        healingTime--;
        reaper.setAttackState(ReaperAttackState.REST);

        reaper.setVelocity(Vec3d.ZERO);

        if (!reaper.world.isClient && healingTime % 10 == 1) {
            reaper.setHealth(reaper.getHealth() + MathHelper.clamp(1.0F / healingCount, 0.5F, 1.0F));
        }

        if (!reaper.world.isClient && healingTime % 100 == 1) {
            // Let's have a light show.
            int dX = reaper.getRandom().nextInt(8) + 4 * reaper.getRandom().nextFloat() >= 0.50F ? 1 : -1;
            int dZ = reaper.getRandom().nextInt(8) + 4 * reaper.getRandom().nextFloat() >= 0.50F ? 1 : -1;
            int y = Util.getSpawnSafeTopLevel(reaper.world, (int) reaper.offsetX() + dX, 256, (int) reaper.offsetZ() + dZ);

            EntityType.LIGHTNING_BOLT.spawn((ServerWorld) reaper.world, null, null, null, new BlockPos(dX, y, dZ), SpawnReason.TRIGGERED, false, false);

            // Also spawn a random skeleton or zombie.
            EntityType<?> m = reaper.getRandom().nextFloat() >= 0.5F ? EntityType.ZOMBIE : EntityType.SKELETON;
            Entity e = m.spawn((ServerWorld) reaper.world, null, null, null, new BlockPos(reaper.offsetX() + dX + 4, y, reaper.offsetZ() + dZ + 4), SpawnReason.TRIGGERED, false, false);

            if (e != null && m == EntityType.SKELETON) {
                e.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            }
        }
    }
}
