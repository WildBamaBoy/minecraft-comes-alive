package mca.core.minecraft;

import cobalt.minecraft.world.CWorld;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Village;
import mca.entity.data.VillageManagerData;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

public class VillageHelper {
    private static boolean isWithinVillage(Village village, Entity entity) {
        return village.getCenter().distSqr(entity.blockPosition()) < Math.pow(village.getSize(), 2.0);
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
        if (!world.isClientSide) {
            //keep track on where player are currently in
            if (world.getMcWorld().getDayTime() % 100 == 0) {
                world.getMcWorld().players().forEach((player) -> {
                    //check if still in village
                    if (playerVillagePositions.containsKey(player.getUUID())) {
                        int id = playerVillagePositions.get(player.getUUID());
                        Village village = VillageManagerData.get(world).villages.get(id);
                        if (village == null) {
                            //TODO world switch may trigger left village notification
                            playerVillagePositions.remove(player.getUUID());
                        } else {
                            if (!isWithinVillage(village, player)) {
                                player.sendMessage(MCA.localizeText("gui.village.left", village.getName()), player.getUUID());
                                playerVillagePositions.remove(player.getUUID());
                            }
                        }
                    } else {
                        Village village = getNearestVillage(player);
                        if (village != null) {
                            player.sendMessage(MCA.localizeText("gui.village.welcome", village.getName()), player.getUUID());
                            playerVillagePositions.put(player.getUUID(), village.getId());
                            village.deliverTaxes((ServerWorld) world.getMcWorld());
                        }
                    }
                });
            }

            //taxes time
            long time = world.getMcWorld().getGameTime();
            if (time % 24000 == 0) {
                updateTaxes(world);
            }

            //update village overall mechanics
            int moveInCooldown = 6000;
            if (time % 6000 == 0) {
                Collection<Village> villages = VillageManagerData.get(world).villages.values();
                for (Village v : villages) {
                    if (v.lastMoveIn + moveInCooldown < world.getMcWorld().getGameTime()) {
                        spawnGuards(world, v);
                        procreate(world, v);
                        marry(world, v);
                    }
                }
            }
        }
    }

    private static void updateTaxes(CWorld world) {
        if (true) {
            //WIP and nobody can stop me implementing them hehe
            return;
        }

        Collection<Village> villages = VillageManagerData.get(world).villages.values();
        for (Village village : villages) {
            int taxes = village.getPopulation() * village.getTaxes() + world.rand.nextInt(100);
            int emeraldValue = 100;
            int emeraldCount = taxes / emeraldValue;

            village.storageBuffer.add(new ItemStack(Items.EMERALD, emeraldCount));
            village.deliverTaxes((ServerWorld) world.getMcWorld());

            world.getMcWorld().players().forEach((player) -> player.sendMessage(MCA.localizeText("gui.village.taxes", village.getName()), player.getUUID()));
        }
    }

    // if the population is low, find a couple and let them have a child
    private static void procreate(CWorld world, Village village) {
        if (world.rand.nextFloat() < MCA.getConfig().childrenChance / 1.0f) {
            int population = village.getPopulation();
            int maxPopulation = village.getMaxPopulation();
            if (population < maxPopulation * MCA.getConfig().childrenLimit / 100.0f) {
                // look for married women without baby
                List<EntityVillagerMCA> villagers = village.getResidents(world);

                if (villagers.size() > 0) {
                    // choose a random
                    EntityVillagerMCA villager = villagers.remove(world.rand.nextInt(villagers.size()));

                    Entity spouse = world.getEntityByUUID(villager.spouseUUID.get().orElse(Constants.ZERO_UUID));
                    if (spouse != null) {
                        villager.hasBaby.set(true);
                        villager.isBabyMale.set(world.rand.nextBoolean());

                        // notify all players
                        StringTextComponent phrase = MCA.localizeText("events.baby", villager.getName().getContents(), spouse.getName().getContents());
                        world.getMcWorld().players().forEach((player) -> player.sendMessage(phrase, player.getUUID()));
                    }
                }
            }
        }
    }

    // if the amount of couples is low, let them marry
    private static void marry(CWorld world, Village village) {
        if (world.rand.nextFloat() < MCA.getConfig().marriageChance / 1.0f) {
            //list all and lonely villagers
            List<EntityVillagerMCA> villagers = new LinkedList<>();
            List<EntityVillagerMCA> allVillagers = village.getResidents(world);
            for (EntityVillagerMCA v : allVillagers) {
                if (!v.isMarried() && !v.isBaby()) {
                    villagers.add(v);
                }
            }

            if (villagers.size() >= 2 && villagers.size() > allVillagers.size() * MCA.getConfig().marriageLimit / 100.0f) {
                // choose a random villager
                EntityVillagerMCA villager = villagers.remove(world.rand.nextInt(villagers.size()));
                EntityVillagerMCA spouse = villagers.remove(world.rand.nextInt(villagers.size()));

                // notify all players
                StringTextComponent phrase = MCA.localizeText("events.marry", villager.getName().getContents(), spouse.getName().getContents());
                world.getMcWorld().players().forEach((player) -> player.sendMessage(phrase, player.getUUID()));

                // marry
                spouse.marry(villager);
                villager.marry(spouse);
            }
        }
    }

    private static void spawnGuards(CWorld world, Village village) {
        int guardCapacity = village.getPopulation() / MCA.getConfig().guardSpawnRate;

        // Count up the guards
        int guards = 0;
        List<EntityVillagerMCA> villagers = village.getResidents(world);
        for (EntityVillagerMCA villager : villagers) {
            if (villager.getProfession() == MCA.PROFESSION_GUARD.get()) {
                guards++;
            }
        }

        // Spawn a new guard if we don't have enough
        if (villagers.size() > 0 && guards < guardCapacity) {
            EntityVillagerMCA villager = villagers.get(world.rand.nextInt(villagers.size()));
            if (!villager.isBaby()) {
                villager.setProfession(MCA.PROFESSION_GUARD.get());
            }
        }
    }
}
