package mca.entity.data;

import mca.enums.MarriageState;
import mca.util.WorldUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.UUID;

public class PlayerSaveData extends PersistentState {
    private UUID spouseUUID = Util.NIL_UUID;
    private String spouseName = "";
    private boolean babyPresent = false;
    private MarriageState marriageState;

    public static PlayerSaveData get(World world, UUID uuid) {
        return WorldUtils.loadData(world, PlayerSaveData::new, PlayerSaveData::new, "mca_village_" + uuid.toString());
    }

    PlayerSaveData() {}

    PlayerSaveData(NbtCompound nbt) {
        spouseUUID = nbt.getUuid("spouseUUID");
        spouseName = nbt.getString("spouseName");
        babyPresent = nbt.getBoolean("babyPresent");
    }


    public boolean isMarried() {
        return !spouseUUID.equals(Util.NIL_UUID) && marriageState != MarriageState.NOT_MARRIED;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putUuid("spouseUUID", spouseUUID);
        nbt.putString("spouseName", spouseName);
        nbt.putBoolean("babyPresent", babyPresent);
        return nbt;
    }

    public void marry(UUID uuid, String name, MarriageState marriageState) {
        this.spouseUUID = uuid;
        this.spouseName = name;
        this.marriageState = marriageState;
        markDirty();
    }

    public void endMarriage() {
        spouseUUID = Util.NIL_UUID;
        spouseName = "";
        markDirty();
    }

    public boolean isBabyPresent() {
        return this.babyPresent;
    }

    public void setBabyPresent(boolean value) {
        this.babyPresent = value;
        markDirty();
    }

    public void reset() {
        endMarriage();
        setBabyPresent(false);
        markDirty();
    }

    public MarriageState getMarriageState() {
        return this.marriageState;
    }


    public UUID getSpouseUUID() {
        return spouseUUID;
    }

    public String getSpouseName() {
        return spouseName;
    }
}
