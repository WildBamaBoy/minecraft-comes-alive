package mca.entity.ai;

import mca.core.minecraft.SoundsMCA;
import mca.entity.EntityVillagerMCA;
import mca.util.Util;
import net.minecraft.block.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.*;

public class EntityAIWork extends EntityAIBase {
    //a found workplace
    private static class WorkPlace {
        public final BlockPos tool;
        public final BlockPos pos;
        public final SoundEvent sound;

        private WorkPlace(BlockPos tool, BlockPos pos, SoundEvent sound) {
            this.tool = tool;
            this.pos = pos;
            this.sound = sound;
        }
    }

    private final EntityVillagerMCA villager;
    private final List<WorkPlace> workplaces;
    private int workingUntil = 0;
    private int workingSound = 0;
    private static final int TICKS_PER_SCAN = 12000;
    private int lastWorkplaceScan = -TICKS_PER_SCAN;
    private WorkPlace place;

    public EntityAIWork(EntityVillagerMCA villagerIn) {
        this.villager = villagerIn;
        this.setMutexBits(1);

        workplaces = new ArrayList<>();
    }

    // sounds associated with a block
    private final static Map<Class, SoundEvent> soundBlocks = new HashMap<>();

    static {
        addSoundBlock(BlockWorkbench.class, SoundsMCA.working_saw);
        addSoundBlock(BlockBookshelf.class, SoundsMCA.working_page);
        addSoundBlock(BlockEnchantmentTable.class, SoundsMCA.working_page);
        addSoundBlock(BlockDoubleStoneSlab.class, SoundsMCA.working_sharpen);
        addSoundBlock(BlockCauldron.class, SoundsMCA.working_anvil);
        addSoundBlock(BlockAnvil.class, SoundsMCA.working_anvil);
    }

    private static void addSoundBlock(Class block, SoundEvent sound) {
        soundBlocks.put(block, sound);
    }

    // blocks available to work on per faction
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

    private static void addWorkingBlocks(String resource, int id, Class... blocks) {
        workingBlocks.put(Objects.requireNonNull(ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation(resource))).getCareer(id), blocks);
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
    private WorkPlace getRandomWorkingBlock() {
        if (workplaces.size() > 0) {
            int index = villager.world.rand.nextInt(workplaces.size());
            WorkPlace place = workplaces.get(index);
            BlockPos workplace = villager.getWorkplace();
            if (place.pos.getDistance(workplace.getX(), workplace.getY(), workplace.getZ()) < 24.0) {
                return place;
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

    public void startExecuting() {
        //scan for a valid work space every 5 minutes
        if (villager.ticksExisted - lastWorkplaceScan > TICKS_PER_SCAN) {
            lastWorkplaceScan = villager.ticksExisted;

            for (Class b : workingBlocks.getOrDefault(villager.getVanillaCareer(), new Class[]{BlockWorkbench.class})) {
                List<BlockPos> available = Util.getNearbyBlocks(villager.getWorkplace(), villager.world, b, 16, 4);
                workplaces.clear();
                for (BlockPos tool : available) {
                    for (int[] n : neighbours) {
                        BlockPos pos = tool.add(n[0], 0, n[1]);
                        if (villager.world.getBlockState(pos).canEntitySpawn(villager)) {
                            //add new position to work on a block
                            Block block = villager.world.getBlockState(tool).getBlock();
                            SoundEvent sound = soundBlocks.getOrDefault(block.getClass(), SoundsMCA.working_page);
                            workplaces.add(new WorkPlace(tool, pos, sound));
                        }
                    }
                }
            }
        }

        //choose random workplace
        place = getRandomWorkingBlock();
        if (place != null) {
            double distance = Math.sqrt(villager.getDistanceSq(place.pos));
            workingUntil = villager.ticksExisted + 200 + (int) distance * 20;

            if (!villager.getNavigator().setPath(villager.getNavigator().getPathToPos(place.pos), 0.5D)) {
                villager.attemptTeleport(place.pos.getX(), place.pos.getY(), place.pos.getZ());
            }
        }
    }

    @Override
    public void updateTask() {
        if (place != null) {
            double distance = Math.sqrt(villager.getDistanceSq(place.pos));

            //work
            if (distance < 1.0D) {
                villager.swingArm(EnumHand.MAIN_HAND);
                villager.swingArm(EnumHand.OFF_HAND);
                villager.getLookHelper().setLookPosition(place.pos.getX(), place.pos.getY(), place.pos.getZ(), villager.getHorizontalFaceSpeed(), villager.getVerticalFaceSpeed());
                //TODO how to tell the client to rotate towards the table?

                //play working sounds
                if (workingSound < villager.ticksExisted) {
                    workingSound = villager.ticksExisted + villager.getRNG().nextInt(40) + 20;
                    villager.playSound(place.sound, 1.0F, 1.0F);
                }
            }
        }
    }
}
