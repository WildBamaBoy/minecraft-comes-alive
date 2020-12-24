package com.minecraftcomesalive.mca.entity.data;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import cobalt.core.CConstants;
import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import lombok.Getter;

import javax.swing.text.html.Option;

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
        data.parent1Name = villager.getVillagerName();
        data.parent1UUID = villager.getUniqueID();
        data.parent2Name = villager.getSpouseName();
        data.parent2UUID = villager.getSpouseUUID();
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
     * @return Optional CEntity
     */
    public Optional<CEntity> getParentEntity(CWorld world, UUID uuid) {
        return world.getEntityByUUID(uuid);
    }

    /**
     * Sends a message to both parent entities.
     *
     * @param world     The world containing the parents.
     * @param message   The message to send.
     */
    public void sendMessage(CWorld world, String message) {
        Optional<CEntity> parent1 = world.getEntityByUUID(parent1UUID);
        Optional<CEntity> parent2 = world.getEntityByUUID(parent2UUID);

        parent1.ifPresent(p -> p.sendMessage(message));
        parent2.ifPresent(p -> p.sendMessage(message));
    }

    /**
     * Returns both parent entities in an array. Array will contain null values if that parent could not be found.
     *
     * @param world World which contains the parents
     * @return CEntity[]
     */
    public CEntity[] getBothParentEntities(CWorld world) {
        Optional<CEntity> parent1 = getParentEntity(world, getParent1UUID());
        Optional<CEntity> parent2 = getParentEntity(world, getParent2UUID());
        return new CEntity[] {
                parent1.orElse(null), parent2.orElse(null)
        };
    }

    /**
     * Returns true if the given player is a parent of this villager.
     *
     * @param player    The player entity.
     * @return bool
     */
    public boolean isParent(CPlayer player) {
        return this.parent1UUID.equals(player.getUniqueID()) || this.parent2UUID.equals(player.getUniqueID());
    }

    /**
     * Returns true if one of the parents is a player.
     * @return  bool
     */
    public boolean hasPlayerParent(CWorld world) {
        return Arrays.stream(getBothParentEntities(world)).anyMatch(CEntity::isPlayer);
    }
}
