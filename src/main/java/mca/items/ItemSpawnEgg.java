package mca.items;

import com.google.common.base.Optional;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSpawnEgg extends Item {
    private boolean isMale;

    public ItemSpawnEgg(boolean isMale) {
        this.isMale = isMale;
        this.setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        int posX = pos.getX();
        int posY = pos.getY() + 1;
        int posZ = pos.getZ();

        if (!world.isRemote) {
            EntityVillagerMCA villager = new EntityVillagerMCA(world, Optional.absent(), Optional.of(isMale ? EnumGender.MALE : EnumGender.FEMALE));
            villager.setPosition(posX + 0.5D, posY, posZ + 0.5D);
            villager.finalizeMobSpawn(world.getDifficultyForLocation(villager.getPos()), null, false);
            world.spawnEntity(villager);

            if (!player.capabilities.isCreativeMode) player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
        }

        return EnumActionResult.PASS;
    }
}