package mca.entity.data;

import cobalt.core.CConstants;
import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import lombok.Getter;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Getter
public class ParentPair {
    private UUID parent1UUID = CConstants.ZERO_UUID;
    private UUID parent2UUID = CConstants.ZERO_UUID;
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
    public static ParentPair fromVillager(EntityVillagerMCA villager) {
        ParentPair data = new ParentPair();
        data.parent1Name = villager.getName().getString();
        data.parent1UUID = villager.getUUID();
        data.parent2Name = villager.spouseName.get();
        data.parent2UUID = villager.spouseUUID.get().orElse(CConstants.ZERO_UUID);
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
     * Returns an optional reference to the parent entity with the given UUID.
     *
     * @param world The world containing the parent.
     * @param uuid  The UUID of the parent
     * @return Optional Entity
     */
    public Optional<Entity> getParentEntity(CWorld world, UUID uuid) {
        return world.getEntityByUUID(uuid);
    }

    /**
     * Sends a message to both parent entities.
     *
     * @param world   The world containing the parents.
     * @param message The message to send.
     */
    public void sendMessage(CWorld world, String message) {
        Optional<Entity> parent1 = world.getEntityByUUID(parent1UUID);
        Optional<Entity> parent2 = world.getEntityByUUID(parent2UUID);

        parent1.ifPresent(p -> p.sendMessage(new StringTextComponent(message), p.getUUID()));
        parent2.ifPresent(p -> p.sendMessage(new StringTextComponent(message), p.getUUID()));
    }

    /**
     * Returns both parent entities in an array. Array will contain null values if that parent could not be found.
     *
     * @param world World which contains the parents
     * @return Entity[]
     */
    public Entity[] getBothParentEntities(CWorld world) {
        Optional<Entity> parent1 = getParentEntity(world, getParent1UUID());
        Optional<Entity> parent2 = getParentEntity(world, getParent2UUID());
        return new Entity[]{
                parent1.orElse(null), parent2.orElse(null)
        };
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
    public boolean hasPlayerParent(CWorld world) {
        return Arrays.stream(getBothParentEntities(world)).anyMatch(e -> e instanceof PlayerEntity);
    }
}
