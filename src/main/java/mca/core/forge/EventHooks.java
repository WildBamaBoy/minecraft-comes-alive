package mca.core.forge;

import mca.client.network.ClientMessageQueue;
import mca.core.MCA;
import mca.core.MCAServer;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.WorldEventListenerMCA;
import mca.entity.EntityVillagerMCA;
import mca.items.ItemBaby;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EventHooks {
    // Maps a player UUID to the itemstack of their held ItemBaby. Filled when a player dies so the baby is never lost.
    public Map<UUID, ItemStack> limbo = new HashMap<>();

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        ItemsMCA.register(event);
        BlocksMCA.registerItemBlocks(event);

        GameRegistry.addSmelting(BlocksMCA.ROSE_GOLD_ORE, new ItemStack(ItemsMCA.ROSE_GOLD_INGOT), 5.0F);
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
        MCAServer.get().tick();
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

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntityItem().getItem();
        if (stack.getItem() instanceof ItemBaby) {
            event.getPlayer().addItemStackToInventory(stack);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // When players respawn check to see if their baby was saved in limbo. Add it back to their inventory.
        if (limbo.containsKey(event.player.getUniqueID())) {
            event.player.inventory.addItemStackToInventory(limbo.get(event.player.getUniqueID()));
            limbo.remove(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        // If a player dies while holding a baby, remember it until they respawn.
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntityLiving();
            Optional<ItemStack> babyStack = player.inventory.mainInventory.stream().filter(s -> s.getItem() instanceof ItemBaby).findFirst();
            babyStack.ifPresent(s -> limbo.put(player.getUniqueID(), babyStack.get()));
        }
    }

    @SubscribeEvent
    public void onLivingSetTarget(LivingSetAttackTargetEvent event)
    {
        //Mobs shouldn't attack infected villagers. Account for this when they attempt to set their target.
        if (event.getEntityLiving() instanceof EntityMob && event.getTarget() instanceof EntityVillagerMCA) {
            EntityMob mob = (EntityMob) event.getEntityLiving();
            EntityVillagerMCA target = (EntityVillagerMCA) event.getTarget();

            if (target.get(EntityVillagerMCA.IS_INFECTED)) {
                mob.setAttackTarget(null);
            }
        }
    }
}
