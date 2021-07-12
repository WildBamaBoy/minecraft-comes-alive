package mca.entity.data;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.minecraft.world.storage.CWorldSavedData;
import mca.core.Constants;
import mca.entity.VillagerEntityMCA;
import mca.enums.Gender;
import mca.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.world.World;

import java.util.*;

public class FamilyTree extends CWorldSavedData {
    private static final String DATA_ID = "MCA-FamilyTree";

    private final Map<UUID, FamilyTreeEntry> entries = new HashMap<>();

    public static FamilyTree get(World world) {
        return WorldUtils.loadData(world, FamilyTree::new, DATA_ID);
    }

    //in case the villager does not exist, add
    public void addEntry(VillagerEntityMCA villager) {
        if (!entries.containsKey(villager.getUuid())) {
            addEntry(villager, Util.NIL_UUID, Util.NIL_UUID);
        }
    }

    private void addChildToParent(UUID child, UUID parent) {
        FamilyTreeEntry entry = getEntry(parent);
        if (entry != null) {
            entry.getChildren().add(child);
        }
    }

    public void addEntry(VillagerEntityMCA villager, UUID father, UUID mother) {
        addChildToParent(villager.getUuid(), father);
        addChildToParent(villager.getUuid(), mother);

        entries.put(villager.getUuid(), new FamilyTreeEntry(
                villager.villagerName.get(),
                false,
                villager.getGenetics().getGender(),
                father,
                mother
        ));
    }

    public void addEntry(PlayerEntity player) {
        if (!entries.containsKey(player.getUuid())) {
            addEntry(player, Util.NIL_UUID, Util.NIL_UUID);
        }
    }

    public void addEntry(PlayerEntity player, UUID father, UUID mother) {
        addChildToParent(player.getUuid(), father);
        addChildToParent(player.getUuid(), mother);

        entries.put(player.getUuid(), new FamilyTreeEntry(
                player.getName().asString(),
                true,
                Gender.MALE, //TODO
                father,
                mother
        ));
    }

    public FamilyTreeEntry getEntry(VillagerEntityMCA villager) {
        if (!entries.containsKey(villager.getUuid())) {
            //a new villager appeared, parents are unknown
            addEntry(villager, Util.NIL_UUID, Util.NIL_UUID);
        }
        return entries.get(villager.getUuid());
    }

    public FamilyTreeEntry getEntry(UUID uuid) {
        return entries.get(uuid);
    }

    public boolean isParent(UUID who, UUID of) {
        FamilyTreeEntry entry = getEntry(of);
        return entry != null && (entry.getFather().equals(who) || entry.getMother().equals(who));
    }

    public boolean isGrandParent(UUID who, UUID of) {
        FamilyTreeEntry entry = getEntry(of);
        return entry != null && (isParent(who, entry.getFather()) || isParent(who, entry.getMother()));
    }

    public boolean isUncle(UUID who, UUID of) {
        FamilyTreeEntry entry = getEntry(who);
        return entry != null && (getSiblings(entry.getFather()).contains(of) || getSiblings(entry.getMother()).contains(of));
    }

    public boolean isRelative(UUID who, UUID with) {
        return getFamily(who).contains(with);
    }

    @Override
    public CNBT save(CNBT nbt) {
        for (Map.Entry<UUID, FamilyTreeEntry> entry : entries.entrySet()) {
            nbt.setTag(entry.getKey().toString(), entry.getValue().save());
        }
        return nbt;
    }

    @Override
    public void load(CNBT nbt) {
        for (String uuid : nbt.getKeySet()) {
            FamilyTreeEntry entry = FamilyTreeEntry.fromCBNT(nbt.getCompoundTag(uuid));
            entries.put(UUID.fromString(uuid), entry);
        }
    }

    private void gatherParents(UUID current, Set<UUID> family, int depth) {
        if (depth > 0) {
            FamilyTreeEntry entry = getEntry(current);
            if (entry != null) {
                family.add(entry.getFather());
                family.add(entry.getMother());
                gatherParents(entry.getFather(), family, depth - 1);
                gatherParents(entry.getMother(), family, depth - 1);
            }
        }
    }

    private void gatherChildren(UUID current, Set<UUID> family, int depth) {
        if (depth > 0) {
            FamilyTreeEntry entry = getEntry(current);
            if (entry != null) {
                for (UUID child : entry.getChildren()) {
                    family.add(child);
                    gatherChildren(child, family, depth - 1);
                }
            }
        }
    }

    public Set<UUID> getFamily(UUID uuid) {
        return getFamily(uuid, 3);
    }

    public Set<UUID> getFamily(UUID uuid, int depth) {
        return getFamily(uuid, depth, depth);
    }

    public Set<UUID> getFamily(UUID uuid, int parentDepth, int childrenDepth) {
        Set<UUID> family = new HashSet<>();

        //fetch parents and children
        gatherParents(uuid, family, parentDepth);
        gatherChildren(uuid, family, childrenDepth);

        //zero UUIDs are no real members
        family.remove(Util.NIL_UUID);

        //and the caller is not meant either
        family.remove(uuid);

        return family;
    }

    //all persons who share at least one common parent
    public Set<UUID> getSiblings(UUID uuid) {
        Set<UUID> siblings = new HashSet<>();

        FamilyTreeEntry entry = getEntry(uuid);
        if (entry != null) {
            gatherChildren(entry.getFather(), siblings, 1);
            gatherChildren(entry.getMother(), siblings, 1);
        }

        return siblings;
    }
}