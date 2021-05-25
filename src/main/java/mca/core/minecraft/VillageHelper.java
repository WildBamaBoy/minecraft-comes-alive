package mca.core.minecraft;

import cobalt.minecraft.world.CWorld;
import mca.core.MCA;
import mca.entity.data.Building;
import mca.entity.data.Village;
import mca.entity.data.VillageManagerData;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillageHelper {
    private static boolean isWithinVillage(Village village, Entity entity) {
        return village.getCenter().distSqr(entity.blockPosition()) < village.getSize();
    }

    public static Village getNearestVillage(Entity entity) {
        Collection<Village> villages = VillageManagerData.get(CWorld.fromMC(entity.level)).villages.values();
        for (Village village : villages) {
            if (isWithinVillage(village, entity)) {
                return village;
            }
        }
        return null;
    }

    private static final Map<UUID, Integer> playerVillagePositions = new HashMap<>();

    public static void tick(CWorld world) {
        if (world.isClientSide) {

        } else {
            //keep track on where player are currently in
            if (world.getMcWorld().getDayTime() % 100 == 0) {
                world.getMcWorld().players().forEach((player) -> {
                    //check if still in village
                    if (playerVillagePositions.containsKey(player.getUUID())) {
                        int id = playerVillagePositions.get(player.getUUID());
                        Village village = VillageManagerData.get(world).villages.get(id);
                        if (!isWithinVillage(village, player)) {
                            player.sendMessage(MCA.localizeText("gui.village.left", village.getName()), player.getUUID());
                            playerVillagePositions.remove(player.getUUID());
                        }
                    } else {
                        Village village = getNearestVillage(player);
                        if (village != null) {
                            player.sendMessage(MCA.localizeText("gui.village.welcome", village.getName()), player.getUUID());
                            playerVillagePositions.put(player.getUUID(), village.getId());
                        }
                    }
                });
            }

//        world.getVillageCollection().getVillageList().forEach(v -> {
//            spawnGuards(world, v);
//            procreate(world, v);
//            marry(world, v);
//        });
        }

    }

    private static void manageVillages(CWorld world) {
        VillageManagerData.get(world);
    }

//    public static void forceSpawnGuards(PlayerEntity player) {
//        Village nearestVillage = player.world.getVillageCollection().getNearestVillage(player.getPosition(), 100);
//        spawnGuards(player.world, nearestVillage);
//    }
//
//    public static void forceRaid(PlayerEntity player) {
//        Village nearestVillage = player.world.getVillageCollection().getNearestVillage(player.getPosition(), 100);
//        startRaid(player.world, nearestVillage);
//    }
//
//    // if the population is low, find a couple and let them have a child
//    private static void procreate(CWorld world, Village village) {
//        if (world.rand.nextFloat() < MCA.getConfig().childrenChance / 1000.0f) {
//            List<EntityVillagerMCA> allVillagers = getVillagers(world, village);
//            if (allVillagers.size() < village.getNumVillageDoors() * MCA.getConfig().childrenLimit / 100.0f) {
//                // look for married women without baby
//                List<EntityVillagerMCA> villagers = new ArrayList<>();
//                for (EntityVillagerMCA v : allVillagers) {
//                    if (v.isMarried() && !v.get(EntityVillagerMCA.hasBaby) && v.get(GENDER) == EnumGender.FEMALE.getId()) {
//                        villagers.add(v);
//                    }
//                }
//
//                if (villagers.size() > 0) {
//                    // choose a random
//                    EntityVillagerMCA villager = villagers.remove(world.rand.nextInt(villagers.size()));
//
//                    Optional<Entity> spouse = Util.getEntityByUUID(world, villager.get(SPOUSE_UUID).or(Constants.ZERO_UUID));
//                    if (spouse.isPresent()) {
//                        villager.set(HAS_BABY, true);
//                        villager.set(BABY_IS_MALE, world.rand.nextBoolean());
//                        villager.spawnParticles(EnumParticleTypes.HEART);
//
//                        // notify all players
//                        // TODO create generic send all
//                        String phrase = MCA.localize("events.baby", villager.getName(), spouse.get().getName());
//                        StringTextComponent text = new StringTextComponent(phrase);
//                        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(text);
//                    }
//                }
//            }
//        }
//    }
//
//    // if the amount of couples is low, let them marry
//    private static void marry(CWorld world, Village village) {
//        if (world.rand.nextFloat() < MCA.getConfig().marriageChance / 1000.0f) {
//            List<EntityVillagerMCA> villagers = new ArrayList<>();
//            List<EntityVillagerMCA> allVillagers = getVillagers(world, village);
//            for (EntityVillagerMCA v : allVillagers) {
//                if (!v.isMarried() && !v.isBaby()) {
//                    villagers.add(v);
//                }
//            }
//
//            if (villagers.size() > allVillagers.size() * MCA.getConfig().marriageLimit / 100.0f) {
//                // choose a random villager
//                EntityVillagerMCA villager = villagers.remove(world.rand.nextInt(villagers.size()));
//
//                // look for best partner
//                float best = Float.MAX_VALUE;
//                EntityVillagerMCA spouse = null;
//                for (EntityVillagerMCA v : villagers) {
//                    float diff = 1.0f; //TODO here we will need proper scoring for the genetics update
//                    if (diff < best) {
//                        best = diff;
//                        spouse = v;
//                    }
//                }
//
//                if (spouse != null) {
//                    // notify all players
//                    String phrase = MCA.localize("events.marry", villager.getName(), spouse.getName());
//                    StringTextComponent text = new StringTextComponent(phrase);
//                    FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(text);
//
//                    // marry
//                    spouse.marry(villager);
//                    villager.marry(spouse);
//                }
//            }
//        }
//    }
//
//    //returns all villagers of a given village
//    private static List<EntityVillagerMCA> getVillagers(CWorld world, Village village) {
//        int radius = village.getVillageRadius();
//        return Util.getEntitiesWithinDistance(world, village.getCenter(), radius, EntityVillagerMCA.class);
//    }
//
//    private static void spawnGuards(World world, Village village) {
//        int guardCapacity = village.getNumVillagers() / MCA.getConfig().guardSpawnRate;
//        int guards = 0;
//
//        // Grab all villagers in the area
//        List<EntityVillagerMCA> list = getVillagers(world, village);
//
//        // Count up the guards
//        for (EntityVillagerMCA villager : list) {
//            if (villager.getProfessionForge().getRegistryName().equals(ProfessionsMCA.guard.getRegistryName())) {
//                guards++;
//            }
//        }
//
//        // Spawn a new guard if we don't have enough, up to 10
//        // TODO magic number 10 should be in the config
//        if (guards < guardCapacity && guards < 10) {
//            Vec3d spawnPos = findRandomSpawnPos(world, village, village.getCenter(), 2, 4, 2);
//
//            if (spawnPos != null) {
//                EntityVillagerMCA guard = new EntityVillagerMCA(world, Optional.of(ProfessionsMCA.guard), Optional.absent());
//                guard.setPosition(spawnPos.x + 0.5D, spawnPos.y + 1.0D, spawnPos.z + 0.5D);
//                guard.onInitialSpawn(world.getDifficultyForLocation(guard.getPos()), null);
//                world.spawnEntity(guard);
//            }
//        }
//    }
//
//    private static void startRaid(World world, Village village) {
//        int banditsToSpawn = world.rand.nextInt(5) + 1;
//
//        while (banditsToSpawn > 0) {
//            EntityVillagerMCA bandit = new EntityVillagerMCA(world, Optional.of(ProfessionsMCA.bandit), Optional.absent());
//            BlockPos spawnLocation = village.getCenter();
//            bandit.setPosition(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());
//            world.spawnEntity(bandit);
//            banditsToSpawn--;
//        }
//    }
//
//    private static Vec3d findRandomSpawnPos(World world, Village village, BlockPos pos, int x, int y, int z) {
//        for (int i = 0; i < 10; ++i) {
//            BlockPos blockpos = pos.add(world.rand.nextInt(16) - 8, world.rand.nextInt(6) - 3, world.rand.nextInt(16) - 8);
//
//            if (village.isBlockPosWithinSqVillageRadius(blockpos) && isAreaClearAround(world, new BlockPos(x, y, z), blockpos))
//                return new Vec3d(blockpos.getX(), blockpos.getY(), blockpos.getZ());
//        }
//
//        return null;
//    }
//
//    private static boolean isAreaClearAround(World world, BlockPos blockSize, BlockPos blockLocation) {
//        if (!world.getBlockState(blockLocation.down()).isTopSolid()) return false;
//        int i = blockLocation.getX() - blockSize.getX() / 2;
//        int j = blockLocation.getZ() - blockSize.getZ() / 2;
//
//        for (int k = i; k < i + blockSize.getX(); ++k) {
//            for (int l = blockLocation.getY(); l < blockLocation.getY() + blockSize.getY(); ++l) {
//                for (int i1 = j; i1 < j + blockSize.getZ(); ++i1) {
//                    if (world.getBlockState(new BlockPos(k, l, i1)).isNormalCube()) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    public static Village findClosestVillage(World world, BlockPos p) {
//        Village village = null;
//        double best = Double.MAX_VALUE;
//        for (Village v : world.getVillageCollection().getVillageList()) {
//            double dist = v.getCenter().getDistance(p.getX(), p.getY(), p.getZ());
//            if (dist < best) {
//                best = dist;
//                village = v;
//            }
//        }
//        return village;
//    }
}
