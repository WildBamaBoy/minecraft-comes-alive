package mca.entity.data;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.brain.VillagerBrain;
import mca.enums.DialogueType;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public class Memories {

    private int hearts;

    private UUID playerUUID;

    private int interactionFatigue;

    private DialogueType dialogueType;

    private VillagerBrain brain;

    private long lastSeen;

    public Memories(VillagerBrain brain, long time, UUID uuid) {
        this.brain = brain;
        playerUUID = uuid;
        dialogueType = DialogueType.ADULT;
        lastSeen = time / 24000L;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getHearts() {
        return hearts;
    }

    public void setHearts(int value) {
        this.hearts = value;
        brain.updateMemories(this);
    }

    public void modHearts(int value) {
        setHearts(this.hearts += value);
    }

    public int getInteractionFatigue() {
        return interactionFatigue;
    }

    public void setInteractionFatigue(int value) {
        this.interactionFatigue = value;
        brain.updateMemories(this);
    }

    public void modInteractionFatigue(int value) {
        this.interactionFatigue += value;
        brain.updateMemories(this);
    }

    public DialogueType getDialogueType() {
        return dialogueType;
    }

    public void setDialogueType(DialogueType dialogueType) {
        this.dialogueType = dialogueType;
        brain.updateMemories(this);
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(int lastSeen) {
        this.lastSeen = lastSeen;
        brain.updateMemories(this);
    }

    public CNBT toCNBT() {
        NbtCompound nbt = new NbtCompound();

        nbt.putUuid("playerUUID", playerUUID);
        nbt.putInt("hearts", hearts);
        nbt.putInt("interactionFatigue", interactionFatigue);
        nbt.putInt("dialogueType", dialogueType.ordinal());
        nbt.putLong("lastSeen", lastSeen);

        return CNBT.fromMC(nbt);
    }

    public static Memories fromCNBT(VillagerEntityMCA villager, @Nullable CNBT cnbt) {
        if (cnbt == null || cnbt.getMcCompound().isEmpty()) {
            return null;
        }

        NbtCompound tag = cnbt.getMcCompound();
        Memories memories = new Memories(villager.getVillagerBrain(), villager.world.getTimeOfDay(), tag.getUuid("playerUUID"));

        memories.hearts = tag.getInt("hearts");
        memories.interactionFatigue = tag.getInt("interactionFatigue");
        memories.dialogueType = DialogueType.byId(tag.getInt("dialogueType"));
        memories.lastSeen = tag.getLong("lastSeen");

        return memories;
    }

}
