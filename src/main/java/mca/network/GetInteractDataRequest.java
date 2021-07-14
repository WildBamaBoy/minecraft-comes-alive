package mca.network;

import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.Relationship;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.FamilyTreeEntry;
import mca.enums.Constraint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GetInteractDataRequest implements Message {
    private static final long serialVersionUID = -4363277735373237564L;

    UUID uuid;

    public GetInteractDataRequest(UUID villager) {
        this.uuid = villager;
    }

    @Override
    public void receive(PlayerEntity player) {
        Entity entity = ((ServerWorld) player.world).getEntity(uuid);

        if (entity instanceof VillagerEntityMCA && player instanceof ServerPlayerEntity) {
            VillagerEntityMCA villager = (VillagerEntityMCA) entity;

            //get constraints
            Map<String, Boolean> constraints = new HashMap<>();
            for (Constraint c : Constraint.values()) {
                constraints.put(c.getId(), c.test(villager, player));
            }

            Relationship relationship = villager.getRelationships();
            Optional<FamilyTreeEntry> family = relationship.getFamily();

            String fatherName = family.map(f -> relationship.getFamilyTree().getEntry(f.getFather())).map(FamilyTreeEntry::getName).orElse(null);
            String motherName = family.map(f -> relationship.getFamilyTree().getEntry(f.getMother())).map(FamilyTreeEntry::getName).orElse(null);

            NetworkHandler.sendToPlayer(new GetInteractDataResponse(constraints, fatherName, motherName), (ServerPlayerEntity)player);
        }
    }
}
