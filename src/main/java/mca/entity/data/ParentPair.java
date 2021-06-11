package mca.entity.data;

import lombok.Getter;
import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.core.Constants;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.Arrays;
import java.util.UUID;

@Getter
public class ParentPair {
    private UUID parent1UUID = Constants.ZERO_UUID;
    private UUID parent2UUID = Constants.ZERO_UUID;
    private String parent1Name = "";
    private String parent2Name = "";

    public static ParentPair fromNBT(CNBT nbt) {
        ParentPair data = new ParentPair();
        data.parent1UUID = nbt.getUUID("parent1UUID");
        data.parent2UUID = nbt.getUUID("parent2UUID");
        data.parent1Name = nbt.getString("parent1Name");
        data.parent2Name = nbt.getString("parent2Name");
        return data;
    }

    public static ParentPair create(UUID parent1UUID, UUID parent2UUID, String parent1Name, String parent2Name) {
        ParentPair data = new ParentPair();
        data.parent1UUID = parent1UUID;
        data.parent2UUID = parent2UUID;
        data.parent1Name = parent1Name;
        data.parent2Name = parent2Name;
        return data;
    }

    /*
     * Creates new parent data containing this villager and their spouse.
     */
    public static ParentPair fromVillager(VillagerEntityMCA villager) {
        ParentPair data = new ParentPair();
        data.parent1Name = villager.getName().getString();
        data.parent1UUID = villager.getUUID();
        data.parent2Name = villager.spouseName.get();
        data.parent2UUID = villager.spouseUUID.get().orElse(Constants.ZERO_UUID);
        return data;
    }

    public CNBT toNBT() {
        CNBT nbt = CNBT.createNew();
        nbt.setUUID("parent1UUID", parent1UUID);
        nbt.setUUID("parent2UUID", parent2UUID);
        nbt.setString("parent1Name", parent1Name);
        nbt.setString("parent2Name", parent2Name);
        return nbt;
    }

    public ParentPair setParents(UUID parent1UUID, String parent1Name, UUID parent2UUID, String parent2Name) {
        this.parent1UUID = parent1UUID;
        this.parent2UUID = parent2UUID;
        this.parent1Name = parent1Name;
        this.parent2Name = parent2Name;
        return this;
    }

    /**
     * Sends a message to both parent entities.
     *
     * @param world   The world containing the parents.
     * @param message The message to send.
     */
    public void sendMessage(ServerWorld world, String message) {
        Entity parent1 = world.getEntity(parent1UUID);
        Entity parent2 = world.getEntity(parent2UUID);

        if (parent1 != null) parent1.sendMessage(new StringTextComponent(message), parent1.getUUID());
        if (parent2 != null) parent2.sendMessage(new StringTextComponent(message), parent2.getUUID());
    }

    /**
     * Returns both parent entities in an array. Array will contain null values if that parent could not be found.
     *
     * @param world World which contains the parents
     * @return Entity[]
     */
    public Entity[] getBothParentEntities(ServerWorld world) {
        Entity parent1 = world.getEntity(getParent1UUID());
        Entity parent2 = world.getEntity(getParent2UUID());
        return new Entity[]{parent1, parent2};
    }

    /**
     * Returns true if the given player is a parent of this villager.
     *
     * @param player The player entity.
     * @return bool
     */
    public boolean isParent(PlayerEntity player) {
        return this.parent1UUID.equals(player.getUUID()) || this.parent2UUID.equals(player.getUUID());
    }

    /**
     * Returns true if one of the parents is a player.
     *
     * @return bool
     */
    public boolean hasPlayerParent(ServerWorld world) {
        return Arrays.stream(getBothParentEntities(world)).anyMatch(e -> e instanceof PlayerEntity);
    }
}
