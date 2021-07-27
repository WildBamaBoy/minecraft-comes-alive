package mca.server.world.data;

import mca.entity.ai.relationship.EntityRelationship;
import mca.entity.ai.relationship.MarriageState;
import mca.entity.ai.relationship.RelationshipType;
import mca.util.NbtElementCompat;
import mca.util.WorldUtils;
import mca.util.compat.OptionalCompat;
import mca.util.compat.PersistentStateCompat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class PlayerSaveData extends PersistentStateCompat implements EntityRelationship {
    @Nullable
    private final UUID playerId;

    private Optional<UUID> spouseUUID = Optional.empty();

    private Optional<Text> spouseName = Optional.empty();

    private boolean babyPresent = false;

    private MarriageState marriageState;

    private final ServerWorld world;

    private Optional<Integer> lastSeenVillage = Optional.empty();

    public static PlayerSaveData get(ServerWorld world, UUID uuid) {
        return WorldUtils.loadData(world, nbt -> new PlayerSaveData(world, nbt), w -> new PlayerSaveData(w, uuid), "mca_village_" + uuid.toString());
    }

    PlayerSaveData(ServerWorld world, UUID playerId) {
        this.world = world;
        this.playerId = playerId;
    }

    PlayerSaveData(ServerWorld world, NbtCompound nbt) {
        this.world = world;
        playerId = nbt.contains("playerId", NbtElementCompat.INT_TYPE) ? nbt.getUuid("playerId") : null;
        lastSeenVillage = nbt.contains("lastSeenVillage", NbtElementCompat.INT_TYPE) ? Optional.of(nbt.getInt("lastSeenVillage")) : Optional.empty();
        spouseUUID = nbt.contains("spouseUUID", NbtElementCompat.INT_TYPE) ? Optional.of(nbt.getUuid("spouseUUID")) : Optional.empty();
        spouseName = nbt.contains("spouseName") ? Optional.of(new LiteralText(nbt.getString("spouseName"))) : Optional.empty();
        babyPresent = nbt.getBoolean("babyPresent");
    }

    @Override
    public void onTragedy(DamageSource cause, @Nullable BlockPos burialSite, RelationshipType type) {

        if (playerId == null) {
            return; // legacy: old saves will not have this
        }

        EntityRelationship.super.onTragedy(cause, burialSite, type);
    }

    public void updateLastSeenVillage(VillageManager manager, PlayerEntity self) {
        Optional<Village> prevVillage = lastSeenVillage.flatMap(manager::getOrEmpty);
        Optional<Village> nextVillage = OptionalCompat.or(prevVillage
                        .filter(v -> v.isWithinBorder(self))
                , () -> manager.findNearestVillage(self));

        setLastSeenVillage(self, prevVillage.orElse(null), nextVillage.orElse(null));
    }

    public void setLastSeenVillage(PlayerEntity self, Village oldVillage, @Nullable Village newVillage) {
        lastSeenVillage = Optional.ofNullable(newVillage).map(Village::getId);
        markDirty();

        if (oldVillage != newVillage) {
            if (oldVillage != null) {
                onLeave(self, oldVillage);
            }
            if (newVillage != null) {
                onEnter(self, newVillage);
            }
        }
    }

    protected void onLeave(PlayerEntity self, Village village) {
        self.sendMessage(new TranslatableText("gui.village.left", village.getName()).formatted(Formatting.GOLD), true);
    }

    protected void onEnter(PlayerEntity self, Village village) {
        self.sendMessage(new TranslatableText("gui.village.welcome", village.getName()).formatted(Formatting.GOLD), true);
        village.deliverTaxes(world);
    }

    @Override
    public Optional<UUID> getSpouseUuid() {
        return spouseUUID;
    }

    public void marry(UUID uuid, String name, MarriageState marriageState) {
        this.spouseUUID = Optional.ofNullable(uuid);
        this.spouseName = Optional.of(name).filter(Strings::isNotEmpty).map(LiteralText::new);
        this.marriageState = marriageState;
        markDirty();
    }

    @Override
    public void endMarriage(MarriageState newState) {
        spouseUUID = Optional.empty();
        spouseName = Optional.empty();
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

    @Override
    public Optional<Text> getSpouseName() {
        return isMarried() ? spouseName : Optional.empty();
    }

    @Override
    public FamilyTree getFamilyTree() {
        return FamilyTree.get(world);
    }

    @Override
    public Stream<Entity> getFamily(int parents, int children) {
        return getFamilyTree()
                .getFamily(playerId, parents, children)
                .stream()
                .map(world::getEntity)
                .filter(Objects::nonNull)
                .filter(e -> !e.getUuid().equals(playerId));
    }

    @Override
    public FamilyTreeEntry getFamilyEntry() {
        return getFamilyTree().getOrCreate(world.getEntity(playerId));
    }

    @Override
    public Stream<Entity> getParents() {
        return getFamilyEntry().parents().map(world::getEntity).filter(Objects::nonNull);
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
        lastSeenVillage.ifPresent(id -> nbt.putInt("lastSeenVillage", id));
        spouseName.ifPresent(n -> nbt.putString("spouseName", n.getString()));
        nbt.putBoolean("babyPresent", babyPresent);
        return nbt;
    }
}
