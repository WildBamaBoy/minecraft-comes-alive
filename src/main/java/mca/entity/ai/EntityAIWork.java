package mca.entity.ai;

import mca.core.minecraft.SoundsMCA;
import mca.entity.EntityVillagerMCA;
import mca.util.Util;
import net.minecraft.block.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.*;

public class EntityAIWork extends EntityAIBase {
    private final EntityVillagerMCA villager;
    private final List<BlockPos> workplaces;
    private int workingUntil = 0;
    private int workingSound = 0;
    private int lastWorkplaceScan = 0;
    private BlockPos target;

    public EntityAIWork(EntityVillagerMCA villagerIn) {
        this.villager = villagerIn;
        this.setMutexBits(1);

        workplaces = new ArrayList<>();
    }

    public boolean shouldExecute() {
        // TODO some villagers should not care about rain, e.g. the blacksmith is often under a roof anyways, the Farmer not
        // TODO the farmer should use the harvesting AI anyways
        if (villager.getWorkplace().getY() == 0 || villager.world.isRaining()) {
            return false; //no workplace or it is raining
        }

        long time = villager.world.getWorldTime() % 24000L;

        //work time and within range
        return time >= 4000 && time <= 7000 && villager.getDistanceSq(villager.getWorkplace()) < 576.0D;
    }

    public boolean shouldContinueExecuting() {
        return workingUntil > villager.ticksExisted;
    }

    //returns a random workplace and verify it, might return null
    private BlockPos getRandomWorkingBlock() {
        if (workplaces.size() > 0) {
            int index = villager.world.rand.nextInt(workplaces.size());
            BlockPos pos = workplaces.get(index);
            BlockPos workplace = villager.getWorkplace();
            if (pos.getDistance(workplace.getX(), workplace.getY(), workplace.getZ()) < 24.0) {
                return pos;
            } else {
                // this workplace seems outdated or out of range
                workplaces.remove(index);
                return null;
            }
        } else {
            // TODO complain to the player, that there are no tools
            return null;
        }
    }

    private final static int[][] neighbours = new int[][]{
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
    };

    private static void addWorkingBlocks(String resource, int id, Class... blocks) {
        workingBlocks.put(Objects.requireNonNull(ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation(resource))).getCareer(id), blocks);
    }

    private final static Map<VillagerRegistry.VillagerCareer, Class[]> workingBlocks = new HashMap<>();

    static {
        //careers marked as INVALID should not start the AIWork AI as they should have custom ones
        addWorkingBlocks("minecraft:farmer", 0, BlockCrops.class); //farmer INVALID
        addWorkingBlocks("minecraft:farmer", 1, BlockChest.class); //fisherman INVALID
        addWorkingBlocks("minecraft:farmer", 2, BlockCrops.class); //shepherd INVALID
        addWorkingBlocks("minecraft:farmer", 3, BlockWorkbench.class); //fletcher TODO fletching table

        addWorkingBlocks("minecraft:librarian", 0, BlockBookshelf.class); //librarian
        addWorkingBlocks("minecraft:librarian", 1, BlockBookshelf.class, BlockWorkbench.class); //cartographer

        addWorkingBlocks("minecraft:priest", 0, BlockEnchantmentTable.class); //cleric

        addWorkingBlocks("minecraft:smith", 0, BlockFurnace.class, BlockAnvil.class, BlockCauldron.class); //armor
        addWorkingBlocks("minecraft:smith", 1, BlockFurnace.class, BlockAnvil.class, BlockCauldron.class); //weapon
        addWorkingBlocks("minecraft:smith", 2, BlockFurnace.class, BlockAnvil.class, BlockCauldron.class); //tool

        addWorkingBlocks("minecraft:butcher", 0, BlockDoubleStoneSlab.class); //butcher
        addWorkingBlocks("minecraft:butcher", 1, BlockCauldron.class); //leather

        addWorkingBlocks("minecraft:nitwit", 0, BlockFlower.class); //nitwit

        addWorkingBlocks("mca:baker", 0, BlockFurnace.class, BlockCake.class); //baker
        addWorkingBlocks("mca:miner", 0, BlockFurnace.class, BlockChest.class, BlockOre.class); //baker
    }

    public void startExecuting() {
        //scan for a valid work space every 10 minutes
        //if (villager.ticksExisted - lastWorkplaceScan > 12000) {
        if (villager.ticksExisted - lastWorkplaceScan > 120) {
            lastWorkplaceScan = villager.ticksExisted;

            //we don't want to stand on the table, but next to it
            for (Class b : workingBlocks.getOrDefault(villager.getVanillaCareer(), new Class[]{BlockWorkbench.class})) {
                System.out.println(b);
                List<BlockPos> available = Util.getNearbyBlocks(villager.getWorkplace(), villager.world, b, 16, 4);
                workplaces.clear();
                for (BlockPos target : available) {
                    for (int[] n : neighbours) {
                        BlockPos pos = target.add(n[0], 0, n[1]);
                        if (villager.world.getBlockState(pos).canEntitySpawn(villager)) {
                            workplaces.add(pos);
                        }
                    }
                }
            }
        }

        //choose random workplace
        target = getRandomWorkingBlock();
        if (target != null) {
            double distance = Math.sqrt(villager.getDistanceSq(target));
            workingUntil = villager.ticksExisted + 200 + (int) distance * 20;

            if (!villager.getNavigator().setPath(villager.getNavigator().getPathToPos(target), 0.5D)) {
                villager.attemptTeleport(target.getX(), target.getY(), target.getZ());
            }
        }
    }

    @Override
    public void updateTask() {
        if (target != null) {
            double distance = Math.sqrt(villager.getDistanceSq(target));

            //work
            if (distance < 1.0D) {
                villager.swingArm(EnumHand.MAIN_HAND);
                villager.getLookHelper().setLookPosition(target.getX(), target.getY(), target.getZ(), villager.getHorizontalFaceSpeed(), villager.getVerticalFaceSpeed());
                //TODO how to tell the client to rotate towards the table?

                //play working sounds
                if (workingSound < villager.ticksExisted) {
                    workingSound = villager.ticksExisted + villager.getRNG().nextInt(40) + 20;
                    villager.playSound(SoundsMCA.reaper_idle, 1.0F, 1.0F);
                }
            }
        }
    }
}
