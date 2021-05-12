package mca.core.forge;

import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.world.CWorld;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.ReaperSpawner;
import mca.entity.EntityVillagerMCA;
import mca.items.ItemBaby;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class EventHooks {
    // Maps a player UUID to the itemstack of their held ItemBaby. Filled when a player dies so the baby is never lost.
    public Map<UUID, ItemStack> limbo = new HashMap<>();
    private final List<VillagerEntity> spawnQueue = new LinkedList<>();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        ReaperSpawner.update();

        // lazy spawning of our villagers as they can't be spawned while loading
        if (!spawnQueue.isEmpty()) {
            VillagerEntity e = spawnQueue.remove(0);
            if (e.level.isLoaded(e.blockPosition())) {
                e.remove();

                EntityVillagerMCA newVillager = new EntityVillagerMCA(MCA.ENTITYTYPE_VILLAGER.get(), e.level);
                newVillager.setPos(e.getX(), e.getY(), e.getZ());

                e.level.isLoaded(newVillager.getPos().getMcPos());
                CWorld.fromMC(e.level).spawnEntity(CEntity.fromMC(newVillager));
            } else {
                spawnQueue.add(e);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (true) return;

        StringTextComponent updateMessage = new StringTextComponent(Constants.Color.DARKGREEN + "An update for Minecraft Comes Alive is available: vTODO");
        String updateURLText = Constants.Color.YELLOW + "Click " + Constants.Color.BLUE + Constants.Format.ITALIC + Constants.Format.UNDERLINE + "here" + Constants.Format.RESET + Constants.Color.YELLOW + " to download the update.";

        StringTextComponent chatComponentUpdate = new StringTextComponent(updateURLText);
        chatComponentUpdate.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minecraftcomesalive.com/download"));
        chatComponentUpdate.getStyle().setUnderlined(true);

        event.getPlayer().sendMessage(updateMessage, Constants.ZERO_UUID);
        event.getPlayer().sendMessage(chatComponentUpdate, Constants.ZERO_UUID);
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
        if (event.getEntity() instanceof EntityVillagerMCA) {
            EntityVillagerMCA villager = (EntityVillagerMCA) event.getEntity();
            Entity source = event.getSource() != null ? event.getSource().getDirectEntity() : null;

            if (source instanceof LivingEntity) {
                villager.world.loadedEntityList.forEach(e -> {
                    if (e instanceof EntityVillagerMCA) {
                        EntityVillagerMCA v = (EntityVillagerMCA) e;
                        if (v.distanceTo(v) <= 10.0D && v.getProfession() == MCA.PROFESSION_GUARD.get()) {
                            v.setTarget((LivingEntity) source);
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntityItem().getItem();
        if (stack.getItem() instanceof ItemBaby) {
            event.getPlayer().addItem(stack);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        long time = event.getWorld().dayTime();
        if (time > 13000 && time < 23000) {
            int x = event.getPos().getX();
            int y = event.getPos().getY();
            int z = event.getPos().getZ();
            Block placedBlock = event.getPlacedBlock().getBlock();

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
            Optional<ItemStack> babyStack = player.inventory.items.stream().filter(s -> s.getItem() instanceof ItemBaby).findFirst();
            babyStack.ifPresent(s -> limbo.put(player.getUUID(), babyStack.get()));
        }
    }

    @SubscribeEvent
    public void onLivingSetTarget(LivingSetAttackTargetEvent event) {
        // Mobs shouldn't attack infected villagers. Account for this when they attempt to set their target.
        if (event.getEntityLiving() instanceof MobEntity && event.getTarget() instanceof EntityVillagerMCA) {
            MobEntity mob = (MobEntity) event.getEntityLiving();
            EntityVillagerMCA target = (EntityVillagerMCA) event.getTarget();

            if (target.isInfected.get()) {
                mob.setTarget(null);
            }
        }
    }
}
