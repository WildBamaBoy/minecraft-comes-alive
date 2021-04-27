package mca.client.network;

import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import cobalt.minecraft.entity.player.CPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientMessageQueue {
    private static final ConcurrentLinkedQueue<IMessage> scheduledMessages = new ConcurrentLinkedQueue<>();

    public static void processScheduledMessages() {
        IMessage next = scheduledMessages.poll();

        if (next != null) handle(next);
    }

    public static void add(IMessage msg) {
        scheduledMessages.add(msg);
    }

    private static void handle(IMessage msg) {
        if (msg instanceof NetMCA.CareerResponse) handleCareerId((NetMCA.CareerResponse) msg);
        else if (msg instanceof NetMCA.InventoryResponse) handleInventory((NetMCA.InventoryResponse) msg);
        else MCA.getLog().error("Unexpected message in queue:" + msg.getClass().getName());
    }

    private static void handleCareerId(NetMCA.CareerResponse msg) {
        CPlayer player = Minecraft.getMinecraft().player;

        try {
            Optional<EntityVillagerMCA> villager = getVillagerByUUID(player.getEntityWorld(), msg.getEntityUUID());

            if (villager.isPresent()) {
                ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager.get(), msg.getCareerId(), EntityVillagerMCA.VANILLA_CAREER_ID_FIELD_INDEX);
            }
        } catch (ClassCastException e) {
            MCA.getLog().error("Failed to cast entity to villager on career ID update.");
        } catch (Exception e) {
            MCA.getLog().error("Failed to set career ID on villager!", e);
        }
    }

    private static void handleInventory(NetMCA.InventoryResponse msg) {
        CPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            Optional<EntityVillagerMCA> villager = getVillagerByUUID(player.world, msg.getEntityUUID());
            villager.ifPresent(entityVillagerMCA -> entityVillagerMCA.inventory.readInventoryFromNBT(msg.getInventoryNBT().getTagList("inventory", 10)));
        }
    }

    private static Optional<EntityVillagerMCA> getVillagerByUUID(World world, UUID uuid) {
        try {
            synchronized (world.loadedEntityList) {
                return world.loadedEntityList.stream().filter(e -> e.getUUID().equals(uuid)).map(EntityVillagerMCA.class::cast).findFirst();
            }
        } catch (ClassCastException ignored) {
            MCA.getLog().error("Failed to cast entity with UUID " + uuid.toString() + " to a villager!");
        }
        return Optional.empty();
    }
}
