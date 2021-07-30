package mca.entity.ai.relationship.family;

import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.Gender;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import mca.util.compat.PersistentStateCompat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;

import java.util.*;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FamilyTree extends PersistentStateCompat {
    private static final String DATA_ID = "MCA-FamilyTree";

    private final Map<UUID, FamilyTreeNode> entries;

    public static FamilyTree get(ServerWorld world) {
        return WorldUtils.loadData(world, FamilyTree::new, FamilyTree::new, DATA_ID);
    }

    FamilyTree(ServerWorld world) {
        entries = new HashMap<>();
    }

    FamilyTree(NbtCompound nbt) {
        entries = NbtHelper.toMap(nbt, UUID::fromString, (id, element) -> new FamilyTreeNode(this, id, (NbtCompound)element));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return NbtHelper.fromMap(nbt, entries, UUID::toString, FamilyTreeNode::save);
    }

    @Deprecated
    @Nullable
    public FamilyTreeNode getNode(UUID uuid) {
        return entries.get(uuid);
    }

    public Optional<FamilyTreeNode> getOrEmpty(UUID id) {
        return Optional.ofNullable(entries.get(id));
    }

    @NotNull
    public FamilyTreeNode getOrCreate(Entity entity) {
        return entries.computeIfAbsent(entity.getUuid(), uuid -> {
            return createEntry(entity, Util.NIL_UUID, Util.NIL_UUID);
        });
    }

    @Deprecated
    public void addChild(Entity father, Entity mother, Entity child) {
        getOrCreate(father);
        getOrCreate(mother);
        addChild(father.getUuid(), mother.getUuid(), child);
    }

    @Deprecated
    public void addChild(UUID father, UUID mother, Entity child) {
        addChildToParent(child.getUuid(), father);
        addChildToParent(child.getUuid(), mother);
        entries.put(child.getUuid(), createEntry(child, father, mother));
    }

    private FamilyTreeNode createEntry(Entity entity, UUID father, UUID mother) {
        return new FamilyTreeNode(this,
                entity.getUuid(),
                entity.getName().getString(),
                entity instanceof PlayerEntity,
                entity instanceof VillagerEntityMCA ? ((VillagerEntityMCA)entity).getGenetics().getGender() : Gender.MALE, //TODO player genders
                father,
                mother,
                new HashSet<>()
        );
    }

    private void addChildToParent(UUID child, UUID parent) {
        FamilyTreeNode entry = getNode(parent);
        if (entry != null) {
            entry.addChild(child);
        }
    }

    @Deprecated
    public boolean isParent(UUID who, UUID of) {
        return getOrEmpty(of).filter(entry -> entry.isParent(who)).isPresent();
    }

    @Deprecated
    public boolean isGrandParent(UUID who, UUID of) {
        return getOrEmpty(of).filter(entry -> entry.isGrandParent(who)).isPresent();
    }

    @Deprecated
    public boolean isUncle(UUID who, UUID of) {
        return getOrEmpty(of).filter(entry -> entry.isUncle(who)).isPresent();
    }

    @Deprecated
    public boolean isRelative(UUID who, UUID with) {
        return getFamily(who).contains(with);
    }

    @Deprecated
    public Set<UUID> getFamily(UUID uuid) {
        return getOrEmpty(uuid).map(entry -> entry.getFamily().collect(Collectors.toSet())).orElseGet(Collections::emptySet);
    }

    @Deprecated
    public Set<UUID> getFamily(UUID uuid, int depth) {
        return getOrEmpty(uuid).map(entry -> entry.getFamily(depth, depth).collect(Collectors.toSet())).orElseGet(Collections::emptySet);
    }

    @Deprecated
    public Set<UUID> getFamily(UUID uuid, int parentDepth, int childrenDepth) {
        return getOrEmpty(uuid).map(entry -> entry.getFamily(parentDepth, childrenDepth).collect(Collectors.toSet())).orElseGet(Collections::emptySet);
    }

    @Deprecated
    public Set<UUID> getSiblings(UUID uuid) {
        return getOrEmpty(uuid).map(FamilyTreeNode::siblings).orElseGet(Collections::emptySet);
    }
}