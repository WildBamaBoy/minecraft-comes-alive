package mca.entity.data;

import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.cobalt.minecraft.world.storage.CWorldSavedData;
import mca.core.Constants;
import mca.enums.MarriageState;
import mca.util.WorldUtils;
import net.minecraft.world.World;

import java.util.UUID;

public class PlayerSaveData extends CWorldSavedData {
    private UUID spouseUUID = Constants.ZERO_UUID;
    private String spouseName = "";
    private boolean babyPresent = false;
    private MarriageState marriageState;

    public PlayerSaveData(String id) {
        super(id);
    }

    public static PlayerSaveData get(World world, UUID uuid) {
        return WorldUtils.loadData(world, PlayerSaveData.class, "mca_village_" + uuid.toString());
    }

    public boolean isMarried() {
        return !spouseUUID.equals(Constants.ZERO_UUID) && marriageState != MarriageState.NOT_MARRIED;
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

    public void marry(UUID uuid, String name, MarriageState marriageState) {
        this.spouseUUID = uuid;
        this.spouseName = name;
        this.marriageState = marriageState;
        setDirty();
    }

    public void endMarriage() {
        spouseUUID = Constants.ZERO_UUID;
        spouseName = "";
        setDirty();
    }

    public boolean isBabyPresent() {
        return this.babyPresent;
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
