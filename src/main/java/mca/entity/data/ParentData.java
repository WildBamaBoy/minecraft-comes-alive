package mca.entity.data;

import static mca.entity.EntityVillagerMCA.SPOUSE_NAME;
import static mca.entity.EntityVillagerMCA.SPOUSE_UUID;
import static mca.entity.EntityVillagerMCA.VILLAGER_NAME;

import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import mca.api.objects.NPC;
import mca.api.wrappers.NBTWrapper;
import mca.api.wrappers.WorldWrapper;
import mca.core.Constants;
import mca.entity.EntityVillagerMCA;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;

@Getter
public class ParentData {
    private UUID parent1UUID = Constants.ZERO_UUID;
    private UUID parent2UUID = Constants.ZERO_UUID;
    private String parent1Name = "";
    private String parent2Name = "";


    public static ParentData fromNBT(NBTTagCompound nbt) {
    	return ParentData.fromNBT(new NBTWrapper(nbt));
    }
    
    public static ParentData fromNBT(NBTWrapper nbt) {
        ParentData data = new ParentData();
        data.parent1UUID = nbt.getUUID("parent1UUID");
        data.parent2UUID = nbt.getUUID("parent2UUID");
        data.parent1Name = nbt.getString("parent1Name");
        data.parent2Name = nbt.getString("parent2Name");
        return data;
    }

    public static ParentData create(UUID parent1UUID, UUID parent2UUID, String parent1Name, String parent2Name) {
        ParentData data = new ParentData();
        data.parent1UUID = parent1UUID;
        data.parent2UUID = parent2UUID;
        data.parent1Name = parent1Name;
        data.parent2Name = parent2Name;
        return data;
    }

    /*
     * Creates new parent data containing this villager and their spouse.
     */
    public static ParentData fromVillager(EntityVillagerMCA villager) {
        ParentData data = new ParentData();
        data.parent1Name = villager.get(VILLAGER_NAME);
        data.parent1UUID = villager.getUniqueID();
        data.parent2Name = villager.get(SPOUSE_NAME);
        data.parent2UUID = villager.get(SPOUSE_UUID).or(Constants.ZERO_UUID);
        return data;
    }

    public NBTTagCompound toVanillaNBT() {
    	NBTTagCompound nbt = new NBTTagCompound();
        nbt.setUniqueId("parent1UUID", parent1UUID);
        nbt.setUniqueId("parent2UUID", parent2UUID);
        nbt.setString("parent1Name", parent1Name);
        nbt.setString("parent2Name", parent2Name);
        return nbt;
    }

    public ParentData setParents(UUID parent1UUID, String parent1Name, UUID parent2UUID, String parent2Name) {
        this.parent1UUID = parent1UUID;
        this.parent2UUID = parent2UUID;
        this.parent1Name = parent1Name;
        this.parent2Name = parent2Name;
        return this;
    }

    public Optional<NPC> getParentEntity(WorldWrapper world, UUID uuid) {
    	return world.getNPCByUUID(uuid);
    }
    
    public void sendMessage(WorldWrapper world, String message) {
    	Optional<NPC> parent1 = world.getNPCByUUID(parent1UUID);
    	Optional<NPC> parent2 = world.getNPCByUUID(parent2UUID);

    	parent1.ifPresent(p -> p.sendMessage(message));
    	parent2.ifPresent(p -> p.sendMessage(message));
    }

	public NPC[] getBothParentNPCs(WorldWrapper world) {
    	Optional<NPC> parent1 = getParentEntity(world, getParent1UUID());
    	Optional<NPC> parent2 = getParentEntity(world, getParent2UUID());
		return new NPC[] {
				parent1.orElse(null), parent2.orElse(null)
		};
	}
}
