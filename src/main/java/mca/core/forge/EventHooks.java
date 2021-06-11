package mca.core.forge;

import com.mojang.brigadier.CommandDispatcher;
import mca.command.AdminCommand;
import mca.command.MCACommand;
import mca.core.MCA;
import mca.core.minecraft.EntitiesMCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.core.minecraft.VillageHelper;
import mca.entity.GrimReaperEntity;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.VillageManagerData;
import mca.items.BabyItem;
import mca.server.ReaperSpawner;
import mca.server.ServerInteractionManager;
import mca.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.*;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = MCA.MOD_ID)
public class EventHooks {
    // Maps a player UUID to the itemstack of their held ItemBaby. Filled when a player dies so the baby is never lost.
    public final Map<UUID, ItemStack> limbo = new HashMap<>();
    private final List<VillagerEntity> spawnQueue = new LinkedList<>();

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();

        AdminCommand.register(dispatcher);
        MCACommand.register(dispatcher);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        //our villager handler
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            VillageHelper.tick(event.world);
        }

        //update buildings
        if (event.world.getGameTime() % 21 == 0) {
            VillageManagerData manager = VillageManagerData.get(event.world);
            manager.processNextBuildings(event.world);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        ReaperSpawner.tick();
        ServerInteractionManager.getInstance().tick();


        // lazy spawning of our villagers as they can't be spawned while loading
        if (!spawnQueue.isEmpty()) {
            VillagerEntity e = spawnQueue.remove(0);
            if (e.level.isLoaded(e.blockPosition())) {
                e.remove();

                VillagerEntityMCA newVillager = new VillagerEntityMCA(e.level);
                newVillager.setPos(e.getX(), e.getY(), e.getZ());

                e.level.isLoaded(newVillager.blockPosition());
                WorldUtils.spawnEntity(e.level, newVillager);
            } else {
                spawnQueue.add(e);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        Entity entity;
        entity = event.getEntity();

        if (world.isClientSide()) return;
        if (!MCA.getConfig().overwriteOriginalVillagers) return;

        if (entity.getClass().equals(VillagerEntity.class)) {
            VillagerEntity v = (VillagerEntity) entity;
            spawnQueue.add(v);
        }
    }

    @SubscribeEvent
    public void onEntityDamaged(LivingDamageEvent event) {
        if (!event.getEntity().level.isClientSide && event.getEntity() instanceof VillagerEntityMCA) {
            VillagerEntityMCA villager = (VillagerEntityMCA) event.getEntity();
            Entity source = event.getSource() != null ? event.getSource().getDirectEntity() : null;

            if (source instanceof LivingEntity) {
                double r = 10.0D;
                AxisAlignedBB axisAlignedBB = new AxisAlignedBB(villager.getX() - r, villager.getY() - r, villager.getZ() - r, villager.getX() + r, villager.getY() + r, villager.getZ() + r);
                villager.level.getLoadedEntitiesOfClass(VillagerEntityMCA.class, axisAlignedBB).forEach(v -> {
                    if (v.distanceToSqr(v) <= 100.0D && v.getProfession() == ProfessionsMCA.GUARD) {
                        v.setTarget((LivingEntity) source);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntityItem().getItem();
        if (stack.getItem() instanceof BabyItem) {
            event.getPlayer().inventory.add(stack);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        Block placedBlock = event.getPlacedBlock().getBlock();

        long time = event.getWorld().dayTime();
        if (time > 13000 && time < 23000) {
            int x = event.getPos().getX();
            int y = event.getPos().getY();
            int z = event.getPos().getZ();

            // summon the grim reaper
            if (placedBlock == Blocks.FIRE && event.getWorld().getBlockState(new BlockPos(x, y - 1, z)).getBlock() == Blocks.EMERALD_BLOCK) {
                int totemsFound = 0;

                // Check on +/- X and Z for at least 3 totems on fire.
                for (int i = 0; i < 4; i++) {
                    int dX = 0;
                    int dZ = 0;

                    if (i == 0) dX = -3;
                    else if (i == 1) dX = 3;
                    else if (i == 2) dZ = -3;
                    else dZ = 3;

                    // Scan upwards to ensure it's obsidian, and on fire.
                    for (int j = -1; j < 2; j++) {
                        BlockPos pos = new BlockPos(x + dX, y + j, z + dZ);
                        Block block = event.getWorld().getBlockState(pos).getBlock();
                        if (block != Blocks.OBSIDIAN && block != Blocks.FIRE) break;

                        // If we made it up to 1 without breaking, make sure the block is fire so that it's a lit totem.
                        if (j == 1 && block == Blocks.FIRE) {
                            totemsFound++;
                        }
                    }
                }

                if (totemsFound >= 3) {
                    //spawn
                    ReaperSpawner.start((ServerWorld) event.getWorld(), new BlockPos(x + 1, y + 10, z + 1));

                    BlockPos pos = new BlockPos(x, y, z);
                    EntityType.LIGHTNING_BOLT.spawn((ServerWorld) event.getWorld(), null, null, null, pos, SpawnReason.STRUCTURE, false, false);

                    event.getWorld().setBlock(pos, Blocks.SOUL_SOIL.defaultBlockState(), 3);
                    event.getWorld().setBlock(pos.above(), Blocks.SOUL_FIRE.defaultBlockState(), 3);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // When players respawn check to see if their baby was saved in limbo. Add it back to their inventory.
        if (limbo.containsKey(event.getPlayer().getUUID())) {
            event.getPlayer().inventory.add(limbo.get(event.getPlayer().getUUID()));
            limbo.remove(event.getPlayer().getUUID());
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        // If a player dies while holding a baby, remember it until they respawn.
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            Optional<ItemStack> babyStack = player.inventory.items.stream().filter(s -> s.getItem() instanceof BabyItem).findFirst();
            babyStack.ifPresent(s -> limbo.put(player.getUUID(), babyStack.get()));
        }
    }

    @SubscribeEvent
    public void onLivingSetTarget(LivingSetAttackTargetEvent event) {
        // Mobs shouldn't attack infected villagers. Account for this when they attempt to set their target.
        if (event.getEntityLiving() instanceof MobEntity && event.getTarget() instanceof VillagerEntityMCA) {
            MobEntity mob = (MobEntity) event.getEntityLiving();
            VillagerEntityMCA target = (VillagerEntityMCA) event.getTarget();

            if (target.isInfected.get()) {
                mob.setTarget(null);
            }
        }
    }

    //this event doesn't seem to fire so check EntitiesMCA for real one
    @SubscribeEvent
    public void attributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntitiesMCA.VILLAGER, VillagerEntityMCA.createAttributes().build());
        event.put(EntitiesMCA.GRIM_REAPER, GrimReaperEntity.createAttributes().build());
    }
}
