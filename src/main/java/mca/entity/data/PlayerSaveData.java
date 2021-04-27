package mca.entity.data;

import cobalt.core.CConstants;
import cobalt.minecraft.world.storage.CWorldSavedData;
import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;

import java.util.UUID;

public class PlayerSaveData extends CWorldSavedData {
    public static final String PREFIX = "MCA-Player-V2-";

    private UUID spouseUUID = CConstants.ZERO_UUID;
    private String spouseName = "";
    private boolean babyPresent = false;

    protected PlayerSaveData(String id) {
        super(id);
    }

    public static PlayerSaveData get(CWorld world, UUID uuid) {
        return (PlayerSaveData) CWorldSavedData.get(PREFIX, PlayerSaveData.class, world, uuid);
    }

    public boolean isMarried() {
        return !spouseUUID.equals(CConstants.ZERO_UUID);
    }

    @Override
    public CNBT save(CNBT nbt) {
        nbt.setUUID("spouseUUID", spouseUUID);
        nbt.setString("spouseName", spouseName);
        nbt.setBoolean("babyPresent", babyPresent);
        return nbt;
    }

    @Override
    public void load(CNBT nbt) {
        spouseUUID = nbt.getUUID("spouseUUID");
        spouseName = nbt.getString("spouseName");
        babyPresent = nbt.getBoolean("babyPresent");
    }

    public void marry(UUID uuid, String name) {
        spouseUUID = uuid;
        spouseName = name;
        setDirty();
    }

    public void endMarriage() {
        spouseUUID = CConstants.ZERO_UUID;
        spouseName = "";
        setDirty();
    }

    public void setBabyPresent(boolean value) {
        this.babyPresent = value;
        setDirty();
    }

    public void reset() {
        endMarriage();
        setBabyPresent(false);
        setDirty();
    }

    public UUID getSpouseUUID() {
        return spouseUUID;
    }

    public String getSpouseName() {
        return spouseName;
    }
}
