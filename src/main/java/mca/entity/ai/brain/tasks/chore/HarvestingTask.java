package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Chore;
import mca.entity.ai.TaskUtils;
import mca.util.InventoryUtils;
import net.minecraft.block.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

//TODO Fix small stops that harvesting ones have
public class HarvestingTask extends AbstractChoreTask {
    private final List<BlockPos> harvestable = new ArrayList<>();
    private int lastCropScan = 0;
    private int lastActionTicks = 0;

    public HarvestingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        return villager.getVillagerBrain().getCurrentJob() == Chore.HARVEST;// && (blockWork - villager.tickCount) < 0;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        return shouldRun(world, villager) && villager.getHealth() == villager.getMaxHealth();
    }

    @Override
    protected void finishRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        ItemStack stack = villager.getStackInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            villager.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }

    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        super.run(world, villager, time);
        if (!villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof HoeItem);
            if (i == -1) {
                abandonJobWithMessage("chore.chopping.nohoe");
            } else {
                ItemStack stack = villager.getInventory().getStack(i);
                villager.setStackInHand(Hand.MAIN_HAND, stack);
            }
        }
    }

    private BlockPos searchCrop(int rangeX, int rangeY, boolean harvestableOnly) {
        List<BlockPos> nearbyCrops = TaskUtils.getNearbyBlocks(villager.getBlockPos(), villager.world, blockState -> blockState.isIn(BlockTags.CROPS) || blockState.getBlock() instanceof GourdBlock, rangeX, rangeY);
        harvestable.clear();

        if (harvestableOnly) {
            for (BlockPos pos : nearbyCrops) {
                BlockState state = villager.world.getBlockState(pos);
                if (state.getBlock() instanceof CropBlock) {
                    CropBlock crop = (CropBlock) state.getBlock();

                    if (crop.isMature(state)) {
                        harvestable.add(pos);
                    }
                } else if (state.getBlock() instanceof GourdBlock) {
                    harvestable.add(pos);
                }
            }
        }

        return TaskUtils.getNearestPoint(villager.getBlockPos(), harvestable.isEmpty() ? nearbyCrops : harvestable);

    }

    private BlockPos searchUnusedFarmLand(int rangeX, int rangeY) {
        List<BlockPos> nearbyFarmLand = TaskUtils.getNearbyBlocks(villager.getBlockPos(), villager.world, blockState -> blockState.isOf(Blocks.FARMLAND), rangeX, rangeY);
        List<BlockPos> fertileLand = new ArrayList<>();
        for (BlockPos pos : nearbyFarmLand) {
            BlockState state = villager.world.getBlockState(pos);
            BlockState possibleCrop = villager.world.getBlockState(pos.up());
            if (state.getBlock() instanceof FarmlandBlock) {
                FarmlandBlock farmlandBlock = (FarmlandBlock) state.getBlock();

                if (farmlandBlock.canPlaceAt(state, villager.world, pos) && possibleCrop.isAir()) {
                    fertileLand.add(pos);
                }
            }
        }

        return TaskUtils.getNearestPoint(villager.getBlockPos(), fertileLand);
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        if (this.villager == null) this.villager = villager;

        if (!InventoryUtils.contains(villager.getInventory(), HoeItem.class) && !villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            abandonJobWithMessage("chore.chopping.norod");
        } else if (!villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof HoeItem);
            ItemStack stack = villager.getInventory().getStack(i);
            villager.setStackInHand(Hand.MAIN_HAND, stack);
        }

        BlockPos fertileFarmLand = searchUnusedFarmLand(16, 3);
        if (fertileFarmLand == null && villager.age - lastCropScan > 1200) {
            lastCropScan = villager.age;
            fertileFarmLand = searchUnusedFarmLand(32, 16);
        }

        if (fertileFarmLand != null && villager.hasSeedToPlant()) {
            villager.moveTowards(fertileFarmLand);
            double distanceToSqr = villager.squaredDistanceTo(fertileFarmLand.getX(), fertileFarmLand.getY(), fertileFarmLand.getZ());
            if (distanceToSqr <= 6.0D) {
                if (!this.tryPlantSeed(world, villager, fertileFarmLand.up())) lastActionTicks++;
            }
            return;
        }


        BlockPos target = searchCrop(16, 3, true);

        if (target == null && villager.age - lastCropScan > 1200) {
            target = searchCrop(32, 16, true);
        }

        if (target != null) {
            //harvest if next to it, else try to reach it

            if (harvestable.isEmpty()) {
                target = searchCrop(16, 3, false);
            }
            villager.moveTowards(target);

            BlockState state = world.getBlockState(target);

            double distanceToSqr = villager.squaredDistanceTo(target.getX(), target.getY(), target.getZ());
            if (distanceToSqr <= 4.5D) {
                if (state.getBlock() instanceof CropBlock) {
                    CropBlock crop = (CropBlock) state.getBlock();
                    if (crop.isMature(state)) {
                        LootContext.Builder lootcontext$builder = (new LootContext.Builder(world)).parameter(LootContextParameters.ORIGIN, villager.getPos()).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).parameter(LootContextParameters.THIS_ENTITY, villager).parameter(LootContextParameters.BLOCK_STATE, state).random(this.villager.getRandom()).luck(0F);
                        List<ItemStack> drops = world.getServer().getLootManager().getTable(crop.getLootTableId()).generateLoot(lootcontext$builder.build(LootContextTypes.BLOCK));
                        for (ItemStack stack : drops) {
                            villager.getInventory().addStack(stack);
                        }

                        world.breakBlock(target, false, villager);

                        if (!this.tryPlantSeed(world, villager, target)) lastActionTicks++;
                    } else {
                        if (!this.tryBonemealCrop(world, villager, state, target)) lastActionTicks++;
                    }
                } else if (state.getBlock() instanceof GourdBlock) {
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

        GourdBlock block = (GourdBlock) state.getBlock();
        LootContext.Builder lootcontext$builder = (new LootContext.Builder(world)).parameter(LootContextParameters.ORIGIN, villager.getPos()).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).parameter(LootContextParameters.THIS_ENTITY, villager).parameter(LootContextParameters.BLOCK_STATE, state).random(this.villager.getRandom()).luck(0F);
        List<ItemStack> drops = world.getServer().getLootManager().getTable(block.getLootTableId()).generateLoot(lootcontext$builder.build(LootContextTypes.BLOCK));
        for (ItemStack stack : drops) {
            villager.getInventory().addStack(stack);
        }

        world.breakBlock(target, false, villager);
        lastActionTicks = 0;
        return true;
    }


    public boolean tryPlantSeed(ServerWorld world, VillagerEntityMCA villager, BlockPos target) {
        if (lastActionTicks < 15) {
            return false;
        }

        SimpleInventory inventory = villager.getInventory();

        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack itemstack = inventory.getStack(i);
            boolean flag = false;
            if (!itemstack.isEmpty()) {
                // TODO: Use an ItemTag for this
                if (itemstack.getItem() == Items.WHEAT_SEEDS) {
                    world.setBlockState(target, Blocks.WHEAT.getDefaultState(), Block.NOTIFY_NEIGHBORS | Block.NOTIFY_LISTENERS);
                    flag = true;
                } else if (itemstack.getItem() == Items.POTATO) {
                    world.setBlockState(target, Blocks.POTATOES.getDefaultState(), Block.NOTIFY_NEIGHBORS | Block.NOTIFY_LISTENERS);
                    flag = true;
                } else if (itemstack.getItem() == Items.CARROT) {
                    world.setBlockState(target, Blocks.CARROTS.getDefaultState(), Block.NOTIFY_NEIGHBORS | Block.NOTIFY_LISTENERS);
                    flag = true;
                } else if (itemstack.getItem() == Items.BEETROOT_SEEDS) {
                    world.setBlockState(target, Blocks.BEETROOTS.getDefaultState(), Block.NOTIFY_NEIGHBORS | Block.NOTIFY_LISTENERS);
                    flag = true;
                }// else if (itemstack.getItem() instanceof IPlantable) {
                    // TODO: Use an ItemTag for this too
                    /*if (((IPlantable) itemstack.getItem()).getPlantType(world, target) == net.minecraftforge.common.PlantType.CROP) {
                        world.setBlockState(target, ((IPlantable) itemstack.getItem()).getPlant(world, target), Block.NOTIFY_NEIGHBORS | Block.NOTIFY_LISTENERS);
                        flag = true;
                    }
                }*/
            }

            if (flag) {
                world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                itemstack.decrement(1);
                if (itemstack.isEmpty()) {
                    inventory.setStack(i, ItemStack.EMPTY);
                }
                villager.getMainHandStack().damage(1, villager, player -> player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                lastActionTicks = 0;
                villager.swingHand(Hand.MAIN_HAND);
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
            ItemStack stack = villager.getInventory().getStack(i);
            stack.decrement(1);
            ((CropBlock) state.getBlock()).grow(world, villager.getRandom(), pos, state);
            villager.getMainHandStack().damage(1, villager, player -> player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            lastActionTicks = 0;
            villager.swingHand(Hand.MAIN_HAND);
            return true;
        }

        return false;
    }
}
