package mca.client.network;

import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientMessageQueue {
    private static ConcurrentLinkedQueue<IMessage> scheduledMessages = new ConcurrentLinkedQueue<>();

    public static void processScheduledMessages() {
        IMessage next = scheduledMessages.poll();

        if (next != null) {
            handle(next);
        }
    }

    public static void add(IMessage msg) {
        scheduledMessages.add(msg);
    }

    private static void handle(IMessage msg) {
        if (msg instanceof NetMCA.CareerResponse) {
            handleCareerId((NetMCA.CareerResponse) msg);
        } else if (msg instanceof NetMCA.InventoryResponse) {
            handleInventory((NetMCA.InventoryResponse) msg);
        } else {
            MCA.getLog().error("Unexpected message in queue:" + msg.getClass().getName());
        }
    }

    private static void handleCareerId(NetMCA.CareerResponse msg) {
        EntityPlayer player = Minecraft.getMinecraft().player;

        try {
            EntityVillagerMCA villager = null;

            synchronized (player.getEntityWorld().loadedEntityList) {
                for (Entity entity : player.getEntityWorld().loadedEntityList) {
                    if (entity.getUniqueID().equals(msg.getEntityUUID())) {
                        villager = (EntityVillagerMCA) entity;
                    }
                }
            }

            if (villager != null) {
                ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager, msg.getCareerId(), "careerId");
            }
        } catch (ClassCastException e) {
            MCA.getLog().error("Failed to cast entity to villager on career ID update.");
        }
    }

    private static void handleInventory(NetMCA.InventoryResponse msg) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        EntityVillagerMCA villager = getVillagerByUUID(player.world, msg.getEntityUUID());

        if (villager != null) {
            villager.inventory.readInventoryFromNBT(msg.getInventoryNBT().getTagList("inventory", 10));
        }
    }

    private static EntityVillagerMCA getVillagerByUUID(World world, UUID uuid) {
        try {
            synchronized (world.loadedEntityList) {
                for (Entity entity : world.loadedEntityList) {
                    if (entity.getUniqueID().equals(uuid)) {
                        return (EntityVillagerMCA) entity;
                    }
                }
            }
        } catch (ClassCastException e) {
            MCA.getLog().error("Failed to cast entity with UUID " + uuid.toString() + " to a villager!");
        }
        return null;
    }
}
