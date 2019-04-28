package mca.core.forge;

import mca.core.minecraft.BlocksMCA;
import mca.client.network.ClientMessageQueue;
import mca.core.MCA;
import mca.core.minecraft.VillageHelper;
import mca.core.minecraft.WorldEventListenerMCA;
import mca.entity.EntityVillagerMCA;
import mca.core.minecraft.ItemsMCA;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHooks {
    public int serverTicks = 0;

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        ItemsMCA.register(event);
        BlocksMCA.registerItemBlocks(event);
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        BlocksMCA.register(event);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().isRemote) {
            event.getWorld().addEventListener(new WorldEventListenerMCA());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        ClientMessageQueue.processScheduledMessages();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        serverTicks++;

        if (serverTicks >= 100) {
            World overworld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
            VillageHelper.tick(overworld);

            serverTicks = 0;
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        Entity entity = event.getEntity();

        if (!world.isRemote) {
            if (MCA.getConfig().overwriteOriginalVillagers && entity.getClass().equals(EntityVillager.class)) {
                EntityVillager originalVillager = (EntityVillager) entity;
                originalVillager.setDead();

                EntityVillagerMCA newVillager = new EntityVillagerMCA(world, originalVillager.getProfessionForge(), null);
                newVillager.setPosition(originalVillager.posX, originalVillager.posY, originalVillager.posZ);
                newVillager.finalizeMobSpawn(world.getDifficultyForLocation(newVillager.getPos()), null, false);
                world.spawnEntity(newVillager);
            }
        }
    }
}
