package mca.entity.data;

import mca.core.Constants;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumDialogueType;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class PlayerHistory {
    private int hearts;
    private int interactionFatigue;
    private boolean hasGift;
    private int greetTimer;
    private EnumDialogueType dialogueType;

    private UUID playerUUID;
    private EntityVillagerMCA villager;

    private PlayerHistory() {
        hearts = 0;
        interactionFatigue = 0;
        hasGift = false;
        greetTimer = 0;
        playerUUID = Constants.ZERO_UUID;
        dialogueType = EnumDialogueType.ADULT;
    }

    public static PlayerHistory getNew(EntityVillagerMCA villager, UUID uuid) {
        PlayerHistory history = new PlayerHistory();
        history.villager = villager;
        history.playerUUID = uuid;

        if (villager.isChild()) {
            history.setDialogueType(EnumDialogueType.CHILD);
        } else {
            history.setDialogueType(EnumDialogueType.ADULT);
        }
        return history;
    }

    public static PlayerHistory fromNBT(EntityVillagerMCA villager, UUID uuid, NBTTagCompound nbt) {
        PlayerHistory history = new PlayerHistory();
        history.villager = villager;
        history.playerUUID = uuid;

        history.hearts = nbt.getInteger("hearts");
        history.interactionFatigue = nbt.getInteger("interactionFatigue");
        history.hasGift = nbt.getBoolean("hasGift");
        history.greetTimer = nbt.getInteger("greetTimer");
        history.dialogueType = EnumDialogueType.byValue(nbt.getString("dialogueType"));

        return history;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setInteger("hearts", hearts);
        nbt.setInteger("interactionFatigue", interactionFatigue);
        nbt.setBoolean("hasGift", hasGift);
        nbt.setInteger("greetTimer", greetTimer);
        nbt.setString("dialogueType", dialogueType.getId());

        return nbt;
    }

    public int getHearts() {
        return hearts;
    }

    public void setHearts(int value) {
        hearts = value;
        villager.updatePlayerHistoryMap(this);
    }

    public int getInteractionFatigue() {
        return interactionFatigue;
    }

    public boolean getHasGift() {
        return hasGift;
    }

    public int getGreetTimer() {
        return greetTimer;
    }

    public void changeHearts(int value) {
        hearts += value;
        villager.updatePlayerHistoryMap(this);
    }

    public void changeInteractionFatigue(int value) {
        interactionFatigue += value;
        villager.updatePlayerHistoryMap(this);
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void update() {
        // every 5 minutes reduce interaction fatigues
        if (villager.ticksExisted % 6000 == 0) {
            changeInteractionFatigue(-1);
        }
    }

    public EnumDialogueType getDialogueType() {
        return this.dialogueType;
    }

    public void setDialogueType(EnumDialogueType type) {
        this.dialogueType = type;
        villager.updatePlayerHistoryMap(this);
    }
}
