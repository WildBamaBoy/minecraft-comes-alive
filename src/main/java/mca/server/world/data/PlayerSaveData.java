package mca.server.world.data;

import mca.entity.ai.relationship.EntityRelationship;
import mca.entity.ai.relationship.MarriageState;
import mca.entity.ai.relationship.RelationshipType;
import mca.util.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.world.PersistentState;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

public class PlayerSaveData extends PersistentState implements EntityRelationship {
    @Nullable
    private final UUID playerId;

    private Optional<UUID> spouseUUID = Optional.empty();

    private String spouseName = "";

    private boolean babyPresent = false;

    private MarriageState marriageState;

    private final ServerWorld world;

    public static PlayerSaveData get(ServerWorld world, UUID uuid) {
        return WorldUtils.loadData(world, nbt -> new PlayerSaveData(world, nbt), w -> new PlayerSaveData(w, uuid), "mca_village_" + uuid.toString());
    }

    PlayerSaveData(ServerWorld world, UUID playerId) {
        this.world = world;
        this.playerId = playerId;
    }

    PlayerSaveData(ServerWorld world, NbtCompound nbt) {
        this.world = world;
        playerId = nbt.getUuid("playerId");
        spouseUUID = Optional.ofNullable(nbt.getUuid("spouseUUID"));
        spouseName = nbt.getString("spouseName");
        babyPresent = nbt.getBoolean("babyPresent");
    }

    @Override
    public void onTragedy(DamageSource cause, RelationshipType type) {

        if (playerId == null) {
            return; // legacy: old saves will not have this
        }

        EntityRelationship.super.onTragedy(cause, type);
    }

    @Override
    public boolean isMarried() {
        return !spouseUUID.orElse(Util.NIL_UUID).equals(Util.NIL_UUID);
    }

    public void marry(UUID uuid, String name, MarriageState marriageState) {
        this.spouseUUID = Optional.ofNullable(uuid);
        this.spouseName = name;
        this.marriageState = marriageState;
        markDirty();
    }

    @Override
    public void endMarriage(MarriageState newState) {
        spouseUUID = Optional.empty();
        spouseName = "";
        marriageState = newState;
        markDirty();
    }

    public boolean isBabyPresent() {
        return this.babyPresent;
    }

    public void setBabyPresent(boolean value) {
        this.babyPresent = value;
        markDirty();
    }

    @Override
    public MarriageState getMarriageState() {
        return marriageState;
    }

    public UUID getSpouseUUID() {
        return spouseUUID.orElse(Util.NIL_UUID);
    }

    public String getSpouseName() {
        return spouseName;
    }

    @Override
    public FamilyTree getFamilyTree() {
        return FamilyTree.get(world);
    }

    @Override
    public FamilyTreeEntry getFamily() {
        return getFamilyTree().getOrCreate(world.getEntity(playerId));
    }

    @Override
    public Stream<Entity> getParents() {
        return getFamily().parents().map(world::getEntity).filter(Objects::nonNull);
    }

    @Override
    public Stream<Entity> getSiblings() {
        return getFamilyTree()
                .getSiblings(playerId)
                .stream()
                .map(world::getEntity)
                .filter(Objects::nonNull)
                .filter(e -> !e.getUuid().equals(playerId)); // we exclude ourselves from the list of siblings
    }

    @Override
    public Optional<Entity> getSpouse() {
        return spouseUUID.map(world::getEntity);
    }

    public void reset() {
        endMarriage(MarriageState.SINGLE);
        setBabyPresent(false);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        spouseUUID.ifPresent(id -> nbt.putUuid("spouseUUID", id));
        nbt.putString("spouseName", spouseName);
        nbt.putBoolean("babyPresent", babyPresent);
        return nbt;
    }
}
