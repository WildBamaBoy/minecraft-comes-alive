package mca.core.minecraft;

import com.google.common.base.Optional;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.util.Util;
import net.minecraft.entity.Entity;
import cobalt.minecraft.entity.player.CPlayer;
import net.minecraft.util.EnumParticleTypes;
import cobalt.minecraft.util.math.CPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;

public class VillageHelper {

    public static void tick(World world) {
        world.getVillageCollection().getVillageList().forEach(v -> {
            spawnGuards(world, v);
            procreate(world, v);
            marry(world, v);
        });
    }

    public static void forceSpawnGuards(CPlayer player) {
        Village nearestVillage = player.world.getVillageCollection().getNearestVillage(player.getPosition(), 100);
        spawnGuards(player.world, nearestVillage);
    }

    public static void forceRaid(CPlayer player) {
        Village nearestVillage = player.world.getVillageCollection().getNearestVillage(player.getPosition(), 100);
        startRaid(player.world, nearestVillage);
    }

    // if the population is low, find a couple and let them have a child
    private static void procreate(World world, Village village) {
        if (world.rand.nextFloat() < MCA.getConfig().childrenChance / 1000.0f) {
            List<EntityVillagerMCA> allVillagers = getVillagers(world, village);
            if (allVillagers.size() < village.getNumVillageDoors() * MCA.getConfig().childrenLimit / 100.0f) {
                // look for married women without baby
                List<EntityVillagerMCA> villagers = new ArrayList<>();
                for (EntityVillagerMCA v : allVillagers) {
                    if (v.isMarried() && !v.get(EntityVillagerMCA.hasBaby) && v.get(GENDER) == EnumGender.FEMALE.getId()) {
                        villagers.add(v);
                    }
                }

                if (villagers.size() > 0) {
                    // choose a random
                    EntityVillagerMCA villager = villagers.remove(world.rand.nextInt(villagers.size()));

                    Optional<Entity> spouse = Util.getEntityByUUID(world, villager.get(SPOUSE_UUID).or(Constants.ZERO_UUID));
                    if (spouse.isPresent()) {
                        villager.set(HAS_BABY, true);
                        villager.set(BABY_IS_MALE, world.rand.nextBoolean());
                        villager.spawnParticles(EnumParticleTypes.HEART);

                        // notify all players
                        // TODO create generic send all
                        String phrase = MCA.localize("events.baby", villager.getName(), spouse.get().getName());
                        StringTextComponent text = new StringTextComponent(phrase);
                        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(text);
                    }
                }
            }
        }
    }

    // if the amount of couples is low, let them marry
    private static void marry(World world, Village village) {
        if (world.rand.nextFloat() < MCA.getConfig().marriageChance / 1000.0f) {
            List<EntityVillagerMCA> villagers = new ArrayList<>();
            List<EntityVillagerMCA> allVillagers = getVillagers(world, village);
            for (EntityVillagerMCA v : allVillagers) {
                if (!v.isMarried() && !v.isChild()) {
                    villagers.add(v);
                }
            }

            if (villagers.size() > allVillagers.size() * MCA.getConfig().marriageLimit / 100.0f) {
                // choose a random villager
                EntityVillagerMCA villager = villagers.remove(world.rand.nextInt(villagers.size()));

                // look for best partner
                float best = Float.MAX_VALUE;
                EntityVillagerMCA spouse = null;
                for (EntityVillagerMCA v : villagers) {
                    float diff = 1.0f; //TODO here we will need proper scoring for the genetics update
                    if (diff < best) {
                        best = diff;
                        spouse = v;
                    }
                }

                if (spouse != null) {
                    // notify all players
                    String phrase = MCA.localize("events.marry", villager.getName(), spouse.getName());
                    StringTextComponent text = new StringTextComponent(phrase);
                    FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(text);

                    // marry
                    spouse.marry(villager);
                    villager.marry(spouse);
                }
            }
        }
    }

    //returns all villagers of a given village
    private static List<EntityVillagerMCA> getVillagers(World world, Village village) {
        int radius = village.getVillageRadius();
        return Util.getEntitiesWithinDistance(world, village.getCenter(), radius, EntityVillagerMCA.class);
    }

    private static void spawnGuards(World world, Village village) {
        int guardCapacity = village.getNumVillagers() / MCA.getConfig().guardSpawnRate;
        int guards = 0;

        // Grab all villagers in the area
        List<EntityVillagerMCA> list = getVillagers(world, village);

        // Count up the guards
        for (EntityVillagerMCA villager : list) {
            if (villager.getProfessionForge().getRegistryName().equals(ProfessionsMCA.guard.getRegistryName())) {
                guards++;
            }
        }

        // Spawn a new guard if we don't have enough, up to 10
        // TODO magic number 10 should be in the config
        if (guards < guardCapacity && guards < 10) {
            Vec3d spawnPos = findRandomSpawnPos(world, village, village.getCenter(), 2, 4, 2);

            if (spawnPos != null) {
                EntityVillagerMCA guard = new EntityVillagerMCA(world, Optional.of(ProfessionsMCA.guard), Optional.absent());
                guard.setPosition(spawnPos.x + 0.5D, spawnPos.y + 1.0D, spawnPos.z + 0.5D);
                guard.onInitialSpawn(world.getDifficultyForLocation(guard.getPos()), null);
                world.spawnEntity(guard);
            }
        }
    }

    private static void startRaid(World world, Village village) {
        int banditsToSpawn = world.rand.nextInt(5) + 1;

        while (banditsToSpawn > 0) {
            EntityVillagerMCA bandit = new EntityVillagerMCA(world, Optional.of(ProfessionsMCA.bandit), Optional.absent());
            CPos spawnLocation = village.getCenter();
            bandit.setPosition(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());
            world.spawnEntity(bandit);
            banditsToSpawn--;
        }
    }

    private static Vec3d findRandomSpawnPos(World world, Village village, CPos pos, int x, int y, int z) {
        for (int i = 0; i < 10; ++i) {
            CPos blockpos = pos.add(world.rand.nextInt(16) - 8, world.rand.nextInt(6) - 3, world.rand.nextInt(16) - 8);

            if (village.isCPosWithinSqVillageRadius(blockpos) && isAreaClearAround(world, new CPos(x, y, z), blockpos))
                return new Vec3d(blockpos.getX(), blockpos.getY(), blockpos.getZ());
        }

        return null;
    }

    private static boolean isAreaClearAround(World world, CPos blockSize, CPos blockLocation) {
        if (!world.getBlockState(blockLocation.down()).isTopSolid()) return false;
        int i = blockLocation.getX() - blockSize.getX() / 2;
        int j = blockLocation.getZ() - blockSize.getZ() / 2;

        for (int k = i; k < i + blockSize.getX(); ++k) {
            for (int l = blockLocation.getY(); l < blockLocation.getY() + blockSize.getY(); ++l) {
                for (int i1 = j; i1 < j + blockSize.getZ(); ++i1) {
                    if (world.getBlockState(new CPos(k, l, i1)).isNormalCube()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static Village findClosestVillage(World world, CPos p) {
        Village village = null;
        double best = Double.MAX_VALUE;
        for (Village v : world.getVillageCollection().getVillageList()) {
            double dist = v.getCenter().getDistance(p.getX(), p.getY(), p.getZ());
            if (dist < best) {
                best = dist;
                village = v;
            }
        }
        return village;
    }
}
