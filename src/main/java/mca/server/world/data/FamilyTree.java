package mca.server.world.data;

import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.Gender;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.world.PersistentState;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public FamilyTreeEntry getEntry(UUID uuid) {
        return entries.get(uuid);
    }

    public Optional<FamilyTreeEntry> getOrEmpty(UUID id) {
        return Optional.ofNullable(entries.get(id));
    }

    @NotNull
    public FamilyTreeEntry getOrCreate(Entity entity) {
        return entries.computeIfAbsent(entity.getUuid(), uuid -> {
            return createEntry(entity, Util.NIL_UUID, Util.NIL_UUID);
        });
    }

    public void addChild(Entity father, Entity mother, Entity child) {
        getOrCreate(father);
        getOrCreate(mother);
        addChild(father.getUuid(), mother.getUuid(), child);
    }

    public void addChild(UUID father, UUID mother, Entity child) {
        addChildToParent(child.getUuid(), father);
        addChildToParent(child.getUuid(), mother);
        entries.put(child.getUuid(), createEntry(child, father, mother));
    }

    private FamilyTreeEntry createEntry(Entity entity, UUID father, UUID mother) {
        return new FamilyTreeEntry(
                entity instanceof VillagerEntityMCA ? ((VillagerEntityMCA)entity).villagerName.get() : entity.getName().asString(),
                entity instanceof PlayerEntity,
                entity instanceof VillagerEntityMCA ? ((VillagerEntityMCA)entity).getGenetics().getGender() : Gender.MALE, //TODO player genders
                father,
                mother,
                new HashSet<>()
        );
    }

    private void addChildToParent(UUID child, UUID parent) {
        FamilyTreeEntry entry = getEntry(parent);
        if (entry != null) {
            entry.children().add(child);
        }
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

        //and the caller is not meant either
        family.remove(uuid);

        return family;
    }

    /**
     * All persons who share at least one common parent
     */
    public Set<UUID> getSiblings(UUID uuid) {

        FamilyTreeEntry entry = getEntry(uuid);

        if (entry == null) {
            return new HashSet<>();
        }

        Set<UUID> siblings = new HashSet<>();

        entry.parents().forEach(parent -> gatherChildren(parent, siblings, 1));

        return siblings;
    }

    private void gatherParents(UUID current, Set<UUID> family, int depth) {
        gather(getEntry(current), family, depth, FamilyTreeEntry::parents);
    }

    private void gatherChildren(UUID current, Set<UUID> family, int depth) {
        gather(getEntry(current), family, depth, FamilyTreeEntry::streamChildren);
    }

    private void gather(@Nullable FamilyTreeEntry entry, Set<UUID> output, int depth, Function<FamilyTreeEntry, Stream<UUID>> walker) {
        if (entry == null || depth <= 0) {
            return;
        }
        walker.apply(entry).forEach(id -> {
            if (!Util.NIL_UUID.equals(id)) {
                output.add(id); //zero UUIDs are no real members
            }
            gather(getEntry(id), output, depth - 1, walker);
        });
    }
}