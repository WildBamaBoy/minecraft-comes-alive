package mca.entity.data;

import lombok.Getter;
import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.core.Constants;
import mca.entity.VillagerEntityMCA;
import mca.enums.DialogueType;

import java.util.UUID;

public class Memories {
    @Getter
    private int hearts;
    @Getter
    private UUID playerUUID;
    @Getter
    private int interactionFatigue;

    private int dialogueType;

    private VillagerEntityMCA villager;

    private Memories() {
        hearts = 0;
        playerUUID = Constants.ZERO_UUID;
        interactionFatigue = 0;
        dialogueType = DialogueType.UNASSIGNED.getId();
    }

    public static Memories getNew(VillagerEntityMCA villager, UUID uuid) {
        Memories memory = new Memories();
        memory.villager = villager;
        memory.playerUUID = uuid;
        memory.interactionFatigue = 0;
        memory.dialogueType = DialogueType.ADULT.getId();
        return memory;
    }

    public static Memories fromCNBT(VillagerEntityMCA villager, CNBT cnbt) {
        if (cnbt == null || cnbt.getMcCompound().isEmpty()) {
            return null;
        }

        Memories memories = getNew(villager, cnbt.getUUID("playerUUID"));

        memories.hearts = cnbt.getInteger("hearts");
        memories.interactionFatigue = cnbt.getInteger("interactionFatigue");
        memories.dialogueType = cnbt.getInteger("dialogueType");

        return memories;
    }

    public CNBT toCNBT() {
        CNBT nbt = CNBT.createNew();
        nbt.setUUID("playerUUID", playerUUID);
        nbt.setInteger("hearts", hearts);
        nbt.setInteger("interactionFatigue", interactionFatigue);
        nbt.setInteger("dialogueType", dialogueType);
        return nbt;
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

    public DialogueType getDialogueType() {
        return DialogueType.byId(this.dialogueType);
    }

    public void setDialogueType(DialogueType value) {
        this.dialogueType = value.getId();
        villager.updateMemories(this);
    }
}
