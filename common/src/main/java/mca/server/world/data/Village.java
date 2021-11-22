package mca.server.world.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mca.Config;
import mca.entity.EquipmentSet;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.relationship.Gender;
import mca.resources.API;
import mca.resources.PoolUtil;
import mca.resources.Rank;
import mca.resources.Tasks;
import mca.util.NbtElementCompat;
import mca.util.NbtHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.village.VillagerProfession;

public class Village implements Iterable<Building> {

    private static final int MOVE_IN_COOLDOWN = 1200;
    private static final int MIN_SIZE = 32;
    private static final int MAX_STORAGE_SIZE = 1024;

    public final static double BORDER_MARGIN = 32.0;
    public final static double MERGE_MARGIN = 128.0;

    private String name = API.getVillagePool().pickVillageName("village");

    public final List<ItemStack> storageBuffer = new LinkedList<>();
    private final Map<Integer, Building> buildings = new HashMap<>();
    private Map<UUID, Integer> unspentHearts = new HashMap<>();
    private Map<UUID, Map<UUID, Integer>> reputation = new HashMap<>();
    private int unspentMood = 0;

    public long lastMoveIn;
    private int id;

    private int centerX, centerY, centerZ;
    private int size = MIN_SIZE;

    private int taxes = 0;
    private int populationThreshold = 50;
    private int marriageThreshold = 50;

    private boolean autoScan = true;

    public Village() {
    }

    public Village(int id) {
        this.id = id;
    }

    public static Optional<Village> findNearest(Entity entity) {
        return VillageManager.get((ServerWorld)entity.world).findNearestVillage(entity);
    }

    public boolean isWithinBorder(Entity entity) {
        return isWithinBorder(entity.getBlockPos());
    }

    public boolean isWithinBorder(BlockPos pos) {
        return isWithinBorder(pos, BORDER_MARGIN);
    }

    public boolean isWithinBorder(BlockPos pos, double margin) {
        return getCenter().getSquaredDistance(pos) < Math.pow(getSize() + margin, 2.0);
    }

    @Override
    public Iterator<Building> iterator() {
        return buildings.values().iterator();
    }

    public void addBuilding(Building building) {
        buildings.put(building.getId(), building);
        calculateDimensions();
    }

    public void removeBuilding(int id) {
        buildings.remove(id);
        calculateDimensions();
    }

    public Stream<Building> getBuildingsOfType(String type) {
        return getBuildings().values().stream().filter(b -> b.getType().equals(type));
    }

    public Optional<Building> getNearestBuildingOfType(String type, Vec3i pos) {
        return getBuildingsOfType(type).min((a, b) -> (int)(a.getCenter().getSquaredDistance(pos) - b.getCenter().getSquaredDistance(pos)));
    }

    public Optional<Building> getBuildingAt(Vec3i pos) {
        return getBuildings().values().stream().filter(b -> b.containsPos(pos)).findAny();
    }

    private void calculateDimensions() {
        if (buildings.size() == 0) {
            return;
        }

        int sx = Integer.MAX_VALUE;
        int sy = Integer.MAX_VALUE;
        int sz = Integer.MAX_VALUE;
        int ex = Integer.MIN_VALUE;
        int ey = Integer.MIN_VALUE;
        int ez = Integer.MIN_VALUE;

        for (Building building : buildings.values()) {
            ex = Math.max(building.getPos1().getX(), ex);
            sx = Math.min(building.getPos0().getX(), sx);

            ey = Math.max(building.getPos1().getY(), ey);
            sy = Math.min(building.getPos0().getY(), sy);

            ez = Math.max(building.getPos1().getZ(), ez);
            sz = Math.min(building.getPos0().getZ(), sz);
        }

        centerX = (ex + sx) / 2;
        centerY = (ey + sy) / 2;
        centerZ = (ez + sz) / 2;

        size = Math.max(ez - sz, Math.max(ex - sx, ey - sy));
    }

    public BlockPos getCenter() {
        return new BlockPos(centerX, centerY, centerZ);
    }

    public int getSize() {
        return size;
    }

    public int getTaxes() {
        return taxes;
    }

    public void setTaxes(int taxes) {
        this.taxes = taxes;
    }

    public int getPopulationThreshold() {
        return populationThreshold;
    }

    public void setPopulationThreshold(int populationThreshold) {
        this.populationThreshold = populationThreshold;
    }

    public int getMarriageThreshold() {
        return marriageThreshold;
    }

    public void setMarriageThreshold(int marriageThreshold) {
        this.marriageThreshold = marriageThreshold;
    }

    public String getName() {
        return name;
    }

    public Map<Integer, Building> getBuildings() {
        return buildings;
    }

    public Optional<Building> getBuilding(int id) {
        return Optional.ofNullable(buildings.get(id));
    }

    public int getId() {
        return id;
    }

    public int getPopulation() {
        int residents = 0;
        for (Building b : this) {
            residents += b.getResidents().size();
        }
        return residents;
    }

    public List<VillagerEntityMCA> getResidents(ServerWorld world) {
        return getBuildings().values()
                .stream()
                .flatMap(building -> building.getResidents().keySet().stream())
                .map(world::getEntity)
                .filter(v -> v instanceof VillagerEntityMCA)
                .map(VillagerEntityMCA.class::cast)
                .collect(Collectors.toList());
    }

    public int getMaxPopulation() {
        int residents = 0;
        for (Building b : this) {
            residents += b.getBeds();
        }
        return residents;
    }

    public boolean hasStoredResource() {
        return storageBuffer.size() > 0;
    }

    public boolean hasBuilding(String building) {
        return buildings.values().stream().anyMatch(b -> b.getType().equals(building));
    }

    public void tick(ServerWorld world, long time) {
        boolean isTaxSeason = time % 24000 == 0;
        boolean isVillageUpdateTime = time % MOVE_IN_COOLDOWN == 0;

        if (isTaxSeason && hasBuilding("storage")) {
            int emeraldValue = 100;
            int taxes = getPopulation() * getTaxes() + world.random.nextInt(emeraldValue);
            int moodImpact = 0;

            //response
            Text msg;
            float r = MathHelper.lerp(0.5f, getTaxes() / 100.0f, world.random.nextFloat());
            if (getTaxes() == 0.0f) {
                r = 0.0f;
            }
            if (r < 0.1) {
                msg = new TranslatableText("gui.village.taxes.more", getName()).formatted(Formatting.GREEN);
                taxes += getPopulation() * 0.25;
            } else if (r < 0.3) {
                msg = new TranslatableText("gui.village.taxes.happy", getName()).formatted(Formatting.DARK_GREEN);
                moodImpact = 5;
            } else if (r < 0.7) {
                msg = new TranslatableText("gui.village.taxes", getName());
            } else if (r < 0.8) {
                msg = new TranslatableText("gui.village.taxes.sad", getName()).formatted(Formatting.GOLD);
                moodImpact = -5;
            } else if (r < 0.9) {
                msg = new TranslatableText("gui.village.taxes.angry", getName()).formatted(Formatting.RED);
                moodImpact = -10;
            } else {
                msg = new TranslatableText("gui.village.taxes.riot", getName()).formatted(Formatting.DARK_RED);
                taxes = 0;
            }

            //send all player with rank merchant a notification
            world.getPlayers().stream()
                    .filter(v -> Tasks.getRank(this, v).isAtLeast(Rank.MERCHANT))
                    .forEach(player -> player.sendMessage(msg, true));

            if (hasBuilding("library")) {
                taxes *= 1.5;
            }

            int emeraldCount = taxes / emeraldValue;
            while (emeraldCount > 0 && storageBuffer.size() < MAX_STORAGE_SIZE) {
                storageBuffer.add(new ItemStack(Items.EMERALD, Math.min(emeraldCount, Items.EMERALD.getMaxCount())));
                emeraldCount -= Items.EMERALD.getMaxCount();
            }

            if (moodImpact != 0) {
                pushMood(world, moodImpact * getPopulation());
            }

            deliverTaxes(world);
        }

        if (isVillageUpdateTime && lastMoveIn + MOVE_IN_COOLDOWN < time) {
            spawnGuards(world);
            procreate(world);
            marry(world);
        }
    }

    public void deliverTaxes(ServerWorld world) {
        if (hasStoredResource()) {
            getBuildingsOfType("storage").forEach(building -> {
                BlockPos pos0 = building.getPos0();
                BlockPos pos1 = building.getPos1();
                for (int x = pos0.getX(); x <= pos1.getX(); x++) {
                    for (int y = pos0.getY(); y <= pos1.getY(); y++) {
                        for (int z = pos0.getZ(); z <= pos1.getZ(); z++) {
                            BlockPos p = new BlockPos(x, y, z);
                            if (hasStoredResource()) {
                                tryToPutIntoInventory(world, p);
                            } else {
                                return;
                            }
                        }
                    }
                }
            });
        }
    }

    private void tryToPutIntoInventory(ServerWorld world, BlockPos p) {
        BlockState state = world.getBlockState(p);
        Block block = state.getBlock();
        if (block.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(p);
            if (blockEntity instanceof Inventory) {
                Inventory inventory = (Inventory)blockEntity;
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    inventory = ChestBlock.getInventory((ChestBlock)block, state, world, p, true);
                    if (inventory != null) {
                        putIntoInventory(inventory);
                    }
                }
            }
        }
    }

    private void putIntoInventory(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            boolean changes = true;
            while (changes) {
                changes = false;
                ItemStack stack = inventory.getStack(i);
                ItemStack tax = storageBuffer.get(0);
                if (stack.getItem() == tax.getItem()) {
                    int diff = Math.min(tax.getCount(), stack.getMaxCount() - stack.getCount());
                    if (diff > 0) {
                        stack.increment(diff);
                        tax.decrement(diff);
                        if (tax.isEmpty()) {
                            storageBuffer.remove(0);
                            changes = true;
                        }
                        inventory.markDirty();
                    }
                } else if (stack.isEmpty()) {
                    inventory.setStack(i, tax);
                    inventory.markDirty();
                    storageBuffer.remove(0);
                    changes = true;
                }
                if (!hasStoredResource()) {
                    return;
                }
            }
        }
    }

    private void spawnGuards(ServerWorld world) {
        int guardCapacity = getPopulation() / Config.getInstance().guardSpawnRate;

        // Count up the guards
        int guards = 0;
        List<VillagerEntityMCA> villagers = getResidents(world);
        List<VillagerEntityMCA> nonGuards = new LinkedList<>();
        for (VillagerEntityMCA villager : villagers) {
            if (villager.getProfession() == ProfessionsMCA.GUARD || villager.getProfession() == ProfessionsMCA.ARCHER) {
                guards++;
            } else if (!villager.isBaby() && !villager.isProfessionImportant()) {
                nonGuards.add(villager);
            }
        }

        // todo if not all villagers are loaded undefined behavior can happen
        // todo what happen about people who are missing in action?

        // Spawn a new guard if we don't have enough
        if (nonGuards.size() > 0 && guards < guardCapacity) {
            VillagerEntityMCA villager = nonGuards.get(world.random.nextInt(nonGuards.size()));
            villager.setProfession(guards % 2 == 0 ? ProfessionsMCA.GUARD : ProfessionsMCA.ARCHER);
        }
    }

    // if the population is low, find a couple and let them have a child
    public void procreate(ServerWorld world) {
        if (world.random.nextFloat() >= Config.getInstance().childrenChance / 100F) {
            return;
        }

        int population = getPopulation();
        int maxPopulation = getMaxPopulation();
        if (population >= maxPopulation * Config.getInstance().childrenLimit / 100F) {
            return;
        }

        // look for married women without baby
        PoolUtil.pick(getResidents(world), world.random)
                .filter(villager -> villager.getGenetics().getGender() == Gender.FEMALE)
                .filter(villager -> villager.getRelationships().getPregnancy().tryStartGestation())
                .ifPresent(villager -> {
                    villager.getRelationships().getSpouse().ifPresent(spouse -> villager.sendEventMessage(new TranslatableText("events.baby", villager.getName(), spouse.getName())));
                });
    }

    // if the amount of couples is low, let them marry
    public void marry(ServerWorld world) {
        if (world.random.nextFloat() >= Config.getInstance().marriageChance / 100f) {
            return;
        }

        //list all and lonely villagers
        List<VillagerEntityMCA> allVillagers = getResidents(world);
        List<VillagerEntityMCA> availableVillagers = allVillagers.stream()
                .filter(v -> !v.getRelationships().isMarried() && !v.isBaby())
                .collect(Collectors.toList());

        if (availableVillagers.size() < allVillagers.size() * Config.getInstance().marriageLimit / 100f) {
            return; // The village is too small.
        }

        // pick a random villager
        PoolUtil.pop(availableVillagers, world.random).ifPresent(suitor -> {
            // Find a potential mate
            PoolUtil.pop(availableVillagers.stream()
                    .filter(i -> suitor.getGenetics().getGender().isMutuallyAttracted(i.getGenetics().getGender()))
                    .collect(Collectors.toList()), world.random).ifPresent(mate -> {
                // smash their bodies together like nobody's business!
                suitor.getRelationships().marry(mate);
                mate.getRelationships().marry(suitor);

                // tell everyone about it
                suitor.sendEventMessage(new TranslatableText("events.marry", suitor.getName(), mate.getName()));
            });
        });
    }

    private void markDirty(ServerWorld world) {
        VillageManager.get(world).markDirty();
    }

    public void addResident(VillagerEntityMCA villager, int buildingId) {
        removeResident(villager);
        lastMoveIn = villager.world.getTime();
        buildings.get(buildingId).addResident(villager);
        markDirty((ServerWorld)villager.world);
    }

    public void removeResident(VillagerEntityMCA villager) {
        buildings.values().forEach(b -> b.getResidents().remove(villager.getUuid()));
    }

    public EquipmentSet getGuardEquipment(VillagerProfession profession) {
        if (profession == ProfessionsMCA.ARCHER) {
            if (hasBuilding("armory")) {
                return EquipmentSet.ARCHER_1;
            } else {
                return EquipmentSet.ARCHER_0;
            }
        } else {
            if (hasBuilding("armory")) {
                return EquipmentSet.GUARD_1;
            } else {
                return EquipmentSet.GUARD_0;
            }
        }
    }

    public void setReputation(PlayerEntity player, VillagerEntityMCA villager, int rep) {
        reputation.computeIfAbsent(player.getUuid(), i -> new HashMap<>()).put(villager.getUuid(), rep);
        markDirty((ServerWorld)player.world);
    }

    public int getReputation(PlayerEntity player) {
        int hearts = reputation.getOrDefault(player.getUuid(), Collections.emptyMap()).values().stream().mapToInt(i -> i).sum()
                + unspentHearts.getOrDefault(player.getUuid(), 0);
        int residents = getPopulation() + 5; //we slightly favor bigger villages
        return hearts / residents;
    }

    public void pushHearts(PlayerEntity player, int rep) {
        unspentHearts.put(player.getUuid(), unspentHearts.getOrDefault(player.getUuid(), 0) + rep);
        markDirty((ServerWorld)player.world);
    }

    public int popHearts(PlayerEntity player) {
        int v = unspentHearts.getOrDefault(player.getUuid(), 0);
        int step = (int)Math.ceil(Math.abs(((double)v) / getPopulation()));
        if (v > 0) {
            v -= step;
            if (v == 0) {
                unspentHearts.remove(player.getUuid());
            } else {
                unspentHearts.put(player.getUuid(), v);
            }
            markDirty((ServerWorld)player.world);
            return step;
        } else if (v < 0) {
            v += step;
            if (v == 0) {
                unspentHearts.remove(player.getUuid());
            } else {
                unspentHearts.put(player.getUuid(), v);
            }
            markDirty((ServerWorld)player.world);
            return -step;
        } else {
            return 0;
        }
    }

    public void pushMood(ServerWorld world, int m) {
        unspentMood += m;
        markDirty(world);
    }

    public int popMood(ServerWorld world) {
        int step = (int)Math.ceil(Math.abs(((double)unspentMood) / getPopulation()));
        if (unspentMood > 0) {
            unspentMood -= step;
            markDirty(world);
            return step;
        } else if (unspentMood < 0) {
            unspentMood += step;
            markDirty(world);
            return -step;
        } else {
            return 0;
        }
    }

    public boolean isAutoScan() {
        return autoScan;
    }

    public void setAutoScan(boolean autoScan) {
        this.autoScan = autoScan;
    }

    public void toggleAutoScan() {
        setAutoScan(!isAutoScan());
    }

    public NbtCompound save() {
        NbtCompound v = new NbtCompound();
        v.putInt("id", id);
        v.putString("name", name);
        v.putInt("taxes", taxes);
        v.put("unspentHearts", NbtHelper.fromMap(new NbtCompound(), unspentHearts, UUID::toString, NbtInt::of));
        v.put("reputation", NbtHelper.fromMap(new NbtCompound(), reputation, UUID::toString, i -> {
            return NbtHelper.fromMap(new NbtCompound(), i, UUID::toString, NbtInt::of);
        }));
        v.putInt("unspentMood", unspentMood);
        v.putInt("populationThreshold", populationThreshold);
        v.putInt("marriageThreshold", marriageThreshold);
        v.put("buildings", NbtHelper.fromList(buildings.values(), Building::save));
        v.putBoolean("autoScan", autoScan);
        return v;
    }

    public void load(NbtCompound v) {
        id = v.getInt("id");
        name = v.getString("name");
        taxes = v.getInt("taxes");
        unspentHearts = NbtHelper.toMap(v.getCompound("unspentHearts"), UUID::fromString, i -> ((NbtInt)i).intValue());
        reputation = NbtHelper.toMap(v.getCompound("reputation"), UUID::fromString, i -> {
            return NbtHelper.toMap((NbtCompound)i, UUID::fromString, i2 -> ((NbtInt)i2).intValue());
        });
        unspentMood = v.getInt("unspentMood");
        populationThreshold = v.getInt("populationThreshold");
        marriageThreshold = v.getInt("marriageThreshold");

        if (v.contains("autoScan")) {
            autoScan = v.getBoolean("autoScan");
        } else {
            autoScan = true;
        }

        NbtList b = v.getList("buildings", NbtElementCompat.COMPOUND_TYPE);
        for (int i = 0; i < b.size(); i++) {
            Building building = new Building(b.getCompound(i));
            buildings.put(building.getId(), building);
        }

        calculateDimensions();
    }
}
