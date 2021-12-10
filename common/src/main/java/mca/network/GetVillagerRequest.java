package mca.network;

import java.util.Optional;
import java.util.UUID;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.ai.relationship.family.FamilyTree;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.network.client.GetVillagerResponse;
import mca.server.world.data.PlayerSaveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;

public class GetVillagerRequest implements Message {
    private static final long serialVersionUID = -4415670234855916259L;

    private final UUID uuid;

    public GetVillagerRequest(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(PlayerEntity player) {
        Entity e = ((ServerWorld)player.world).getEntity(uuid);
        NbtCompound villagerData = getVillagerData(e);

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new GetVillagerResponse(villagerData), (ServerPlayerEntity)player);
        }
    }

    private static void storeNode(NbtCompound data, Optional<FamilyTreeNode> entry, String prefix) {
        if (entry.isPresent()) {
            data.putString("tree_" + prefix + "_name", entry.get().getName());
            data.putUuid("tree_" + prefix + "_uuid", entry.get().id());
        } else {
            data.putString("tree_" + prefix + "_name", "");
            data.putUuid("tree_" + prefix + "_uuid", Util.NIL_UUID);
        }
    }

    public static NbtCompound getVillagerData(Entity e) {
        NbtCompound data;

        if (e instanceof PlayerEntity) {
            data = PlayerSaveData.get((ServerWorld)e.world, e.getUuid()).getEntityData();
        } else if (e instanceof LivingEntity) {
            data = new NbtCompound();
            ((MobEntity)e).writeCustomDataToNbt(data);
        } else {
            return null;
        }

        FamilyTree tree = FamilyTree.get((ServerWorld)e.world);
        FamilyTreeNode entry = tree.getOrCreate(e);

        storeNode(data, tree.getOrEmpty(entry.spouse()), "spouse");
        storeNode(data, tree.getOrEmpty(entry.father()), "father");
        storeNode(data, tree.getOrEmpty(entry.mother()), "mother");

        return data;
    }
}
