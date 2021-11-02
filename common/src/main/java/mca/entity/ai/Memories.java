package mca.entity.ai;

import mca.entity.VillagerLike;
import mca.entity.ai.brain.VillagerBrain;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Memories {

    private int hearts;

    private final UUID playerUUID;

    private int interactionFatigue;

    private DialogueType dialogueType;

    private final VillagerBrain<?> brain;

    private long lastSeen;

    public Memories(VillagerBrain<?> brain, long time, UUID uuid) {
        this.brain = brain;
        playerUUID = uuid;
        dialogueType = DialogueType.UNASSIGNED;
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

    public NbtCompound toCNBT() {
        NbtCompound nbt = new NbtCompound();

        nbt.putUuid("playerUUID", playerUUID);
        nbt.putInt("hearts", hearts);
        nbt.putInt("interactionFatigue", interactionFatigue);
        nbt.putInt("dialogueType", dialogueType.ordinal());
        nbt.putLong("lastSeen", lastSeen);

        return nbt;
    }

    public static <E extends MobEntity & VillagerLike<E>> Memories fromCNBT(E villager, @Nullable NbtCompound tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }

        Memories memories = new Memories(villager.getVillagerBrain(), villager.world.getTimeOfDay(), tag.getUuid("playerUUID"));

        memories.hearts = tag.getInt("hearts");
        memories.interactionFatigue = tag.getInt("interactionFatigue");
        memories.dialogueType = DialogueType.byId(tag.getInt("dialogueType"));
        memories.lastSeen = tag.getLong("lastSeen");

        return memories;
    }

}
