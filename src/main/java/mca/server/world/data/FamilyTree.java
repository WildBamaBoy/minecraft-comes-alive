package mca.server.world.data;

import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.Gender;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.world.PersistentState;

import java.util.*;

public class FamilyTree extends PersistentState {
    private static final String DATA_ID = "MCA-FamilyTree";

    private final Map<UUID, FamilyTreeEntry> entries;

    public static FamilyTree get(ServerWorld world) {
        return WorldUtils.loadData(world, FamilyTree::new, FamilyTree::new, DATA_ID);
    }

    FamilyTree(ServerWorld world) {
        entries = new HashMap<>();
    }

    FamilyTree(NbtCompound nbt) {
        entries = NbtHelper.toMap(nbt, UUID::fromString, element -> new FamilyTreeEntry((NbtCompound)element));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return NbtHelper.fromMap(nbt, entries, UUID::toString, FamilyTreeEntry::save);
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
            entry.children().add(child);
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
                mother,
                new ArrayList<>()
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
                Gender.MALE, //TODO player genders
                father,
                mother,
                new ArrayList<>()
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
        return entry != null && (entry.father().equals(who) || entry.mother().equals(who));
    }

    public boolean isGrandParent(UUID who, UUID of) {
        FamilyTreeEntry entry = getEntry(of);
        return entry != null && (isParent(who, entry.father()) || isParent(who, entry.mother()));
    }

    public boolean isUncle(UUID who, UUID of) {
        FamilyTreeEntry entry = getEntry(who);
        return entry != null && (getSiblings(entry.father()).contains(of) || getSiblings(entry.mother()).contains(of));
    }

    public boolean isRelative(UUID who, UUID with) {
        return getFamily(who).contains(with);
    }

    private void gatherParents(UUID current, Set<UUID> family, int depth) {
        if (depth > 0) {
            FamilyTreeEntry entry = getEntry(current);
            if (entry != null) {
                family.add(entry.father());
                family.add(entry.mother());
                gatherParents(entry.father(), family, depth - 1);
                gatherParents(entry.mother(), family, depth - 1);
            }
        }
    }

    private void gatherChildren(UUID current, Set<UUID> family, int depth) {
        if (depth > 0) {
            FamilyTreeEntry entry = getEntry(current);
            if (entry != null) {
                for (UUID child : entry.children()) {
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
            gatherChildren(entry.father(), siblings, 1);
            gatherChildren(entry.mother(), siblings, 1);
        }

        return siblings;
    }
}