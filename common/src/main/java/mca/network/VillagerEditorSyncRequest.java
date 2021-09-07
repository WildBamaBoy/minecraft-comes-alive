package mca.network;

import java.util.UUID;
import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerLike;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;

public class VillagerEditorSyncRequest extends S2CNbtDataMessage {
    private final String command;
    private final UUID uuid;

    public VillagerEditorSyncRequest(String command, UUID uuid) {
        this(command, uuid, new NbtCompound());
    }

    public VillagerEditorSyncRequest(String command, UUID uuid, NbtCompound data) {
        super(data);
        this.command = command;
        this.uuid = uuid;
    }

    @Override
    public void receive(PlayerEntity e) {
        Entity entity = ((ServerWorld)e.world).getEntity(uuid);
        if (entity instanceof VillagerLike) {
            VillagerLike<?> villager = (VillagerLike<?>)entity;
            switch (command) {
                case "sync":
                    ((LivingEntity)entity).readCustomDataFromNbt(getData());
                    break;
                case "profession":
                    if (entity instanceof VillagerEntityMCA) {
                        VillagerProfession profession = Registry.VILLAGER_PROFESSION.get(new Identifier(getData().getString("profession")));
                        ((VillagerEntityMCA)villager).setProfession(profession);
                    }
                    break;
            }
        }
        getData();
    }
}
