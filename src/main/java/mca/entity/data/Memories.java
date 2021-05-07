package mca.entity.data;

import cobalt.core.CConstants;
import cobalt.minecraft.nbt.CNBT;
import lombok.Getter;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumDialogueType;

import java.util.UUID;

public class Memories {
    @Getter
    private int hearts;
    @Getter
    private UUID playerUUID;
    @Getter
    private int interactionFatigue;

    private int dialogueType;

    private EntityVillagerMCA villager;

    private Memories() {
        hearts = 0;
        playerUUID = CConstants.ZERO_UUID;
        interactionFatigue = 0;
        dialogueType = EnumDialogueType.UNASSIGNED.getId();
    }

    public static Memories getNew(EntityVillagerMCA villager, UUID uuid) {
        Memories memory = new Memories();
        memory.villager = villager;
        memory.playerUUID = uuid;
        memory.interactionFatigue = 0;
        memory.dialogueType = EnumDialogueType.ADULT.getId();
        return memory;
    }

    public CNBT toCNBT() {
        CNBT nbt = CNBT.createNew();
        nbt.setUUID("playerUUID", playerUUID);
        nbt.setInteger("hearts", hearts);
        nbt.setInteger("interactionFatigue", interactionFatigue);
        nbt.setInteger("dialogueType", dialogueType);
        return nbt;
    }

    public static Memories fromCNBT(EntityVillagerMCA villager, CNBT cnbt) {
        if (cnbt == null || cnbt.getMcCompound().isEmpty()) {
            return null;
        }

        Memories memories = getNew(villager, cnbt.getUUID("playerUUID"));

        memories.hearts = cnbt.getInteger("hearts");
        memories.interactionFatigue = cnbt.getInteger("interactionFatigue");
        memories.dialogueType = cnbt.getInteger("dialogueType");

        return memories;
    }

    public void setHearts(int value) {
        this.hearts = value;
        villager.updateMemories(this);
    }

    public void modHearts(int value) {
        this.hearts += value;
        villager.updateMemories(this);
    }

    public void setInteractionFatigue(int value) {
        this.interactionFatigue = value;
        villager.updateMemories(this);
    }

    public void modInteractionFatigue(int value) {
        this.interactionFatigue += value;
        villager.updateMemories(this);
    }

    public EnumDialogueType getDialogueType() {
        return EnumDialogueType.byId(this.dialogueType);
    }

    public void setDialogueType(EnumDialogueType value) {
        this.dialogueType = value.getId();
        villager.updateMemories(this);
    }
}
