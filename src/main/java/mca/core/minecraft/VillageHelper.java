package mca.core.minecraft;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.Village;
import mca.entity.data.VillageManagerData;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

public class VillageHelper {
    private static final Map<UUID, Integer> playerVillagePositions = new HashMap<>();

    private static boolean isWithinVillage(Village village, Entity entity) {
        return village.getCenter().distSqr(entity.blockPosition()) < Math.pow(village.getSize(), 2.0);
    }

    public static Village getNearestVillage(Entity entity) {
        Collection<Village> villages = VillageManagerData.get(entity.level).villages.values();
        for (Village village : villages) {
            if (isWithinVillage(village, entity)) {
                return village;
            }
        }
        return null;
    }

    public static void tick(World world) {
        if (!world.isClientSide) {
            //keep track on where player are currently in
            if (world.getDayTime() % 100 == 0) {
                world.players().forEach((player) -> {
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
                            village.deliverTaxes((ServerWorld) world);
                        }
                    }
                });
            }

            //taxes time
            long time = world.getGameTime();
            if (time % 24000 == 0) {
                updateTaxes(world);
            }

            //update village overall mechanics
            int moveInCooldown = 6000;
            if (time % 6000 == 0) {
                Collection<Village> villages = VillageManagerData.get(world).villages.values();
                for (Village v : villages) {
                    if (v.lastMoveIn + moveInCooldown < time) {
                        spawnGuards(world, v);
                        procreate(world, v);
                        marry(world, v);
                    }
                }
            }
        }
    }

    private static void updateTaxes(World world) {
        if (true) {
            //WIP and nobody can stop me implementing them hehe
            return;
        }

        Collection<Village> villages = VillageManagerData.get(world).villages.values();
        for (Village village : villages) {
            int taxes = village.getPopulation() * village.getTaxes() + world.random.nextInt(100);
            int emeraldValue = 100;
            int emeraldCount = taxes / emeraldValue;

            village.storageBuffer.add(new ItemStack(Items.EMERALD, emeraldCount));
            village.deliverTaxes((ServerWorld) world);

            world.players().forEach((player) -> player.sendMessage(MCA.localizeText("gui.village.taxes", village.getName()), player.getUUID()));
        }
    }

    // if the population is low, find a couple and let them have a child
    private static void procreate(World world, Village village) {
        if (world.random.nextFloat() < MCA.getConfig().childrenChance / 100.0f) {
            int population = village.getPopulation();
            int maxPopulation = village.getMaxPopulation();
            if (population < maxPopulation * MCA.getConfig().childrenLimit / 100.0f) {
                // look for married women without baby
                List<VillagerEntityMCA> villagers = village.getResidents(world);

                if (villagers.size() > 0) {
                    // choose a random
                    VillagerEntityMCA villager = villagers.remove(world.random.nextInt(villagers.size()));

                    Entity spouse = ((ServerWorld) world).getEntity(villager.spouseUUID.get().orElse(Constants.ZERO_UUID));
                    if (spouse != null) {
                        villager.hasBaby.set(true);
                        villager.isBabyMale.set(world.random.nextBoolean());

                        // notify all players
                        StringTextComponent phrase = MCA.localizeText("events.baby", villager.getName().getContents(), spouse.getName().getContents());
                        world.players().forEach((player) -> player.sendMessage(phrase, player.getUUID()));
                    }
                }
            }
        }
    }

    // if the amount of couples is low, let them marry
    private static void marry(World world, Village village) {
        if (world.random.nextFloat() < MCA.getConfig().marriageChance / 100.0f) {
            //list all and lonely villagers
            List<VillagerEntityMCA> villagers = new LinkedList<>();
            List<VillagerEntityMCA> allVillagers = village.getResidents(world);
            for (VillagerEntityMCA v : allVillagers) {
                if (!v.isMarried() && !v.isBaby()) {
                    villagers.add(v);
                }
            }

            if (villagers.size() >= 2 && villagers.size() > allVillagers.size() * MCA.getConfig().marriageLimit / 100.0f) {
                // choose a random villager
                VillagerEntityMCA villager = villagers.remove(world.random.nextInt(villagers.size()));
                VillagerEntityMCA spouse = villagers.remove(world.random.nextInt(villagers.size()));

                // notify all players
                StringTextComponent phrase = MCA.localizeText("events.marry", villager.getName().getContents(), spouse.getName().getContents());
                world.players().forEach((player) -> player.sendMessage(phrase, player.getUUID()));

                // marry
                spouse.marry(villager);
                villager.marry(spouse);
            }
        }
    }

    private static void spawnGuards(World world, Village village) {
        int guardCapacity = village.getPopulation() / MCA.getConfig().guardSpawnRate;

        // Count up the guards
        int guards = 0;
        List<VillagerEntityMCA> villagers = village.getResidents(world);
        for (VillagerEntityMCA villager : villagers) {
            if (villager.getProfession() == ProfessionsMCA.GUARD) {
                guards++;
            }
        }

        // Spawn a new guard if we don't have enough
        if (villagers.size() > 0 && guards < guardCapacity) {
            VillagerEntityMCA villager = villagers.get(world.random.nextInt(villagers.size()));
            if (!villager.isBaby()) {
                villager.setProfession(ProfessionsMCA.GUARD);
            }
        }
    }
}
