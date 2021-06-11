package mca.entity.ai.brain.tasks;

import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.AssignProfessionTask;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class ExtendedAssignProfessionTask extends AssignProfessionTask {

    //needed to change clothes as it directly touches data
    @Override
    protected void start(ServerWorld world, VillagerEntity villager, long p_212831_3_) {
        if (villager instanceof VillagerEntityMCA) {
            GlobalPos globalpos = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get();
            villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
            villager.getBrain().setMemory(MemoryModuleType.JOB_SITE, globalpos);
            world.broadcastEntityEvent(villager, (byte)14);
            if (villager.getVillagerData().getProfession() == VillagerProfession.NONE) {
                MinecraftServer minecraftserver = world.getServer();
                Optional.ofNullable(minecraftserver.getLevel(globalpos.dimension())).flatMap((p_241376_1_) -> p_241376_1_.getPoiManager().getType(globalpos.pos())).flatMap((p_220390_0_) -> Registry.VILLAGER_PROFESSION.stream().filter((p_220389_1_) -> p_220389_1_.getJobPoiType() == p_220390_0_).findFirst()).ifPresent((p_220388_2_) -> {
                    ((VillagerEntityMCA) villager).setProfession(p_220388_2_);
                    villager.refreshBrain(world);
                });
            }
        }

    }
}
