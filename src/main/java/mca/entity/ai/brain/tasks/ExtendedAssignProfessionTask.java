package mca.entity.ai.brain.tasks;

import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.GoToWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import java.util.Optional;

public class ExtendedAssignProfessionTask extends GoToWorkTask {

    //needed to change clothes as it directly touches data
    @Override
    protected void start(ServerWorld world, VillagerEntity villager, long p_212831_3_) {
        if (villager instanceof VillagerEntityMCA) {
            GlobalPos globalpos = villager.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get();
            villager.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
            villager.getBrain().remember(MemoryModuleType.JOB_SITE, globalpos);
            world.sendEntityStatus(villager, (byte) 14);
            if (villager.getVillagerData().getProfession() == VillagerProfession.NONE) {
                MinecraftServer minecraftserver = world.getServer();
                Optional.ofNullable(minecraftserver.getWorld(globalpos.getDimension())).flatMap((p_241376_1_) -> p_241376_1_.getPointOfInterestStorage().getType(globalpos.getPos())).flatMap((p_220390_0_) -> Registry.VILLAGER_PROFESSION.stream().filter((p_220389_1_) -> p_220389_1_.getWorkStation() == p_220390_0_).findFirst()).ifPresent((p_220388_2_) -> {
                    ((VillagerEntityMCA) villager).setProfession(p_220388_2_);
                    villager.reinitializeBrain(world);
                });
            }
        }

    }
}
