package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import mca.enums.Chore;
import mca.util.InventoryUtils;
import mca.util.Util;
import net.minecraft.block.*;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;

import java.util.ArrayList;
import java.util.List;

//TODO Fix small stops that harvesting ones have
public class HarvestingTask extends AbstractChoreTask {
    private final List<BlockPos> harvestable = new ArrayList<>();
    private int lastCropScan = 0;
    private int lastActionTicks = 0;


    public HarvestingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        return villager.activeChore.get() == Chore.HARVEST.getId();// && (blockWork - villager.tickCount) < 0;
    }

    @Override
    protected boolean canStillUse(ServerWorld world, VillagerEntityMCA villager, long p_212834_3_) {
        return checkExtraStartConditions(world, villager) && villager.getHealth() == villager.getMaxHealth();
    }

    @Override
    protected void stop(ServerWorld world, VillagerEntityMCA villager, long p_212835_3_) {
        ItemStack stack = villager.getItemInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            villager.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }

    }

    @Override
    protected void start(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        super.start(world, villager, p_212831_3_);
        if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof HoeItem);
            if (i == -1) {
                villager.say(getAssigningPlayer().get(), "chore.harvesting.nohoe");
                villager.stopChore();
            } else {
                ItemStack stack = villager.inventory.getItem(i);
                villager.setItemInHand(Hand.MAIN_HAND, stack);
            }


        }


    }

    private BlockPos searchCrop(int rangeX, int rangeY, boolean harvestableOnly) {
        List<BlockPos> nearbyCrops = Util.getNearbyBlocks(villager.blockPosition(), villager.level, blockState -> blockState.is(BlockTags.CROPS) || blockState.getBlock() instanceof StemGrownBlock, rangeX, rangeY);
        harvestable.clear();

        if (harvestableOnly) {
            for (BlockPos pos : nearbyCrops) {
                BlockState state = villager.level.getBlockState(pos);
                if (state.getBlock() instanceof CropsBlock) {
                    CropsBlock crop = (CropsBlock) state.getBlock();

                    if (crop.isMaxAge(state)) {
                        harvestable.add(pos);
                    }
                } else if (state.getBlock() instanceof StemGrownBlock) {
                    harvestable.add(pos);
                }
            }
        }

        return Util.getNearestPoint(villager.blockPosition(), harvestable.isEmpty() ? nearbyCrops : harvestable);

    }

    private BlockPos searchUnusedFarmLand(int rangeX, int rangeY) {
        List<BlockPos> nearbyFarmLand = Util.getNearbyBlocks(villager.blockPosition(), villager.level, blockState -> blockState.is(Blocks.FARMLAND), rangeX, rangeY);
        List<BlockPos> fertileLand = new ArrayList<>();
        for (BlockPos pos : nearbyFarmLand) {
            BlockState state = villager.level.getBlockState(pos);
            BlockState possibleCrop = villager.level.getBlockState(pos.above());
            if (state.getBlock() instanceof FarmlandBlock) {
                FarmlandBlock farmlandBlock = (FarmlandBlock) state.getBlock();

                if (farmlandBlock.isFertile(state, villager.level, pos) && possibleCrop.isAir()) {
                    fertileLand.add(pos);
                }
            }
        }

        return Util.getNearestPoint(villager.blockPosition(), fertileLand);
    }

    @Override
    protected void tick(ServerWorld world, VillagerEntityMCA villager, long p_212833_3_) {
        if (this.villager == null) this.villager = villager;

        if (!InventoryUtils.contains(villager.getInventory(), HoeItem.class) && !villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            villager.say(this.getAssigningPlayer().get(), "chore.harvesting.nohoe");
            villager.stopChore();
        } else if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof HoeItem);
            ItemStack stack = villager.inventory.getItem(i);
            villager.setItemInHand(Hand.MAIN_HAND, stack);
        }

        BlockPos fertileFarmLand = searchUnusedFarmLand(16, 3);
        if (fertileFarmLand == null && villager.tickCount - lastCropScan > 1200) {
            lastCropScan = villager.tickCount;
            fertileFarmLand = searchUnusedFarmLand(32, 16);
        }

        if (fertileFarmLand != null && villager.hasFarmSeeds()) {
            villager.moveTowards(fertileFarmLand);
            double distanceToSqr = villager.distanceToSqr(fertileFarmLand.getX(), fertileFarmLand.getY(), fertileFarmLand.getZ());
            if (distanceToSqr <= 6.0D) {
                if (!this.tryPlantSeed(world, villager, fertileFarmLand.above())) lastActionTicks++;
            }
            return;
        }


        BlockPos target = searchCrop(16, 3, true);

        if (target == null && villager.tickCount - lastCropScan > 1200) {
            target = searchCrop(32, 16, true);
        }

        if (target != null) {
            //harvest if next to it, else try to reach it

            if (harvestable.isEmpty()) {
                target = searchCrop(16, 3, false);
            }
            villager.moveTowards(target);

            BlockState state = world.getBlockState(target);

            double distanceToSqr = villager.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (distanceToSqr <= 4.5D) {
                if (state.getBlock() instanceof CropsBlock) {
                    CropsBlock crop = (CropsBlock) state.getBlock();
                    if (crop.isMaxAge(state)) {
                        LootContext.Builder lootcontext$builder = (new LootContext.Builder(world)).withParameter(LootParameters.ORIGIN, villager.position()).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withParameter(LootParameters.THIS_ENTITY, villager).withParameter(LootParameters.BLOCK_STATE, state).withRandom(this.villager.getRandom()).withLuck(0F);
                        List<ItemStack> drops = world.getServer().getLootTables().get(crop.getLootTable()).getRandomItems(lootcontext$builder.create(LootParameterSets.BLOCK));
                        for (ItemStack stack : drops) {
                            villager.inventory.addItem(stack);
                        }

                        world.destroyBlock(target, false, villager);

                        if (!this.tryPlantSeed(world, villager, target)) lastActionTicks++;
                    } else {
                        if (!this.tryBonemealCrop(world, villager, state, target)) lastActionTicks++;
                    }
                } else if (state.getBlock() instanceof StemGrownBlock) {
                    if (!this.tryBreakStemGrownBlock(world, villager, target)) lastActionTicks++;
                    //No planting seed or bonemeal, since none can be made at this point.
                }
            }
        }
    }

    public boolean tryBreakStemGrownBlock(ServerWorld world, VillagerEntityMCA villager, BlockPos target) {
        if (lastActionTicks < 15) {
            return false;
        }

        BlockState state = world.getBlockState(target);

        StemGrownBlock block = (StemGrownBlock) state.getBlock();
        LootContext.Builder lootcontext$builder = (new LootContext.Builder(world)).withParameter(LootParameters.ORIGIN, villager.position()).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withParameter(LootParameters.THIS_ENTITY, villager).withParameter(LootParameters.BLOCK_STATE, state).withRandom(this.villager.getRandom()).withLuck(0F);
        List<ItemStack> drops = world.getServer().getLootTables().get(block.getLootTable()).getRandomItems(lootcontext$builder.create(LootParameterSets.BLOCK));
        for (ItemStack stack : drops) {
            villager.inventory.addItem(stack);
        }

        world.destroyBlock(target, false, villager);
        lastActionTicks = 0;
        return true;
    }


    public boolean tryPlantSeed(ServerWorld world, VillagerEntityMCA villager, BlockPos target) {
        if (lastActionTicks < 15) {
            return false;
        }


        Inventory inventory = villager.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemstack = inventory.getItem(i);
            boolean flag = false;
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() == Items.WHEAT_SEEDS) {
                    world.setBlock(target, Blocks.WHEAT.defaultBlockState(), 3);
                    flag = true;
                } else if (itemstack.getItem() == Items.POTATO) {
                    world.setBlock(target, Blocks.POTATOES.defaultBlockState(), 3);
                    flag = true;
                } else if (itemstack.getItem() == Items.CARROT) {
                    world.setBlock(target, Blocks.CARROTS.defaultBlockState(), 3);
                    flag = true;
                } else if (itemstack.getItem() == Items.BEETROOT_SEEDS) {
                    world.setBlock(target, Blocks.BEETROOTS.defaultBlockState(), 3);
                    flag = true;
                } else if (itemstack.getItem() instanceof IPlantable) {
                    if (((IPlantable) itemstack.getItem()).getPlantType(world, target) == net.minecraftforge.common.PlantType.CROP) {
                        world.setBlock(target, ((IPlantable) itemstack.getItem()).getPlant(world, target), 3);
                        flag = true;
                    }
                }
            }

            if (flag) {
                world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.CROP_PLANTED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                itemstack.shrink(1);
                if (itemstack.isEmpty()) {
                    inventory.setItem(i, ItemStack.EMPTY);
                }
                villager.getMainHandItem().hurtAndBreak(1, villager, (p_220038_0_) -> p_220038_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
                lastActionTicks = 0;
                villager.swing(Hand.MAIN_HAND);
                return true;
            } else {
                //TODO make the villager say that it needs seeds. ALSO NEEDS COOLDOWN OR IT WILL SPAM IT
                //villager.say(getAssigningPlayer().get(), "chore.harvesting.noseed");
            }


        }

        return false;
    }

    public boolean tryBonemealCrop(ServerWorld world, VillagerEntityMCA villager, BlockState state, BlockPos pos) {
        if (lastActionTicks < 15) {
            return false;
        }

        int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof BoneMealItem);
        if (i > -1) {
            ItemStack stack = villager.inventory.getItem(i);
            stack.shrink(1);
            ((CropsBlock) state.getBlock()).performBonemeal(world, villager.getRandom(), pos, state);
            villager.getMainHandItem().hurtAndBreak(1, villager, (p_220038_0_) -> p_220038_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
            lastActionTicks = 0;
            villager.swing(Hand.MAIN_HAND);
            return true;
        }

        return false;
    }
}
