package mca.blocks;

import java.util.Random;

import mca.api.objects.Pos;
import mca.api.wrappers.WorldWrapper;
import mca.api.platforms.BlockPlatform;
import mca.core.MCA;
import mca.entity.VillagerFactory;
import mca.entity.EntityVillagerMCA;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;

public class BlockVillagerSpawner extends BlockPlatform {
    public BlockVillagerSpawner() {
        super(Material.IRON);
        setUnbreakable();
        setRandomlyTicks();
    }

    @Override
    public void onUpdate(WorldWrapper world, Pos pos, IBlockState state, Random random) {
        int nearbyVillagers = world.getEntitiesInArea(EntityVillagerMCA.class, new AxisAlignedBB(pos.getBlockPos()).expand(32D, 32D, 32D)).size();
        if (nearbyVillagers < MCA.getConfig().villagerSpawnerCap) {
            int yMod = 0;

            // Start from the current point possible and count up until air is hit. This allows the spawner to
            // be placed anywhere below ground and still spawn a villager on a top level.
            while (pos.getY() + yMod < 256) {
                Pos current = pos.add(0, yMod, 0);
                Pos above = pos.add(0, yMod + 1, 0);

                if (world.isAir(current) && world.isAir(above)) {
                	EntityVillagerMCA villager = VillagerFactory.newVillager(world).build();
                    villager.setPosition(current.getX(), current.getY(), current.getZ());
                    world.spawnEntity(villager);
                    break;
                }

                yMod++;
            }
        }

        world.scheduleUpdate(pos, this, MCA.getConfig().villagerSpawnerRateMinutes * 72000);
    }
}