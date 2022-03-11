package mca.entity.ai.relationship.family;

import java.util.stream.Stream;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FamilyTree extends PersistentStateCompat {
    private static final String DATA_ID = "MCA-FamilyTree";

    private final Map<UUID, FamilyTreeNode> entries;

    public static FamilyTree get(ServerWorld world) {
        return WorldUtils.loadData(world.getServer().getOverworld(), FamilyTree::new, FamilyTree::new, DATA_ID);
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

    public Optional<FamilyTreeNode> getOrEmpty(@Nullable UUID id) {
        return id == null ? Optional.empty() : Optional.ofNullable(entries.get(id));
    }

    public Stream<FamilyTreeNode> getAllWithName(String name) {
        return entries.values().stream().filter(n -> n.getName().equals(name));
    }

    @NotNull
    public FamilyTreeNode getOrCreate(Entity entity) {
        return entries.computeIfAbsent(entity.getUuid(), uuid -> createEntry(entity, Util.NIL_UUID, Util.NIL_UUID));
    }

    @NotNull
    public FamilyTreeNode getOrCreate(UUID id, String name, Gender gender) {
        return entries.computeIfAbsent(id, uuid -> createEntry(uuid, name, gender));
    }

    private FamilyTreeNode createEntry(UUID uuid, String name, Gender gender) {
        markDirty();
        return new FamilyTreeNode(this,
                uuid,
                name,
                false,
                gender,
                Util.NIL_UUID,
                Util.NIL_UUID
        );
    }

    private FamilyTreeNode createEntry(Entity entity, UUID father, UUID mother) {
        markDirty();
        return new FamilyTreeNode(this,
                entity.getUuid(),
                entity.getName().getString(),
                entity instanceof PlayerEntity,
                entity instanceof VillagerEntityMCA ? ((VillagerEntityMCA)entity).getGenetics().getGender() : Gender.MALE, //TODO player genders
                father,
                mother
        );
    }
}
