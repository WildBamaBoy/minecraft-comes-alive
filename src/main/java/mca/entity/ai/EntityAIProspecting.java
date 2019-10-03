package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class EntityAIProspecting extends AbstractEntityAIChore {
    private int ticks;

    public EntityAIProspecting(EntityVillagerMCA entityIn) {
        super(entityIn);
        this.setMutexBits(4);
    }

    public boolean shouldExecute() {
        if (villager.getHealth() < villager.getMaxHealth()) {
            villager.stopChore();
        }
        return EnumChore.byId(villager.get(EntityVillagerMCA.ACTIVE_CHORE)) == EnumChore.PROSPECT;
    }

    public void updateTask() {
        super.updateTask();

        ItemStack pickStack = villager.inventory.getBestItemOfType(ItemPickaxe.class);
        if (pickStack == ItemStack.EMPTY) {
            villager.say(getAssigningPlayer(), "chore.mining.nopick");
            villager.stopChore();
            return;
        }

        float efficiency = Item.ToolMaterial.valueOf(((ItemPickaxe) pickStack.getItem()).getToolMaterialName()).getEfficiency();
        float notifyRate = Math.max(600 - efficiency * 50, 100);

        if (ticks >= notifyRate) {
            BlockPos closestOre = Util.getNearestPoint(villager.getPos(), Util.getNearbyBlocks(villager.getPos(), villager.world, BlockOre.class, 4, 3));

            if (closestOre != null) {
                Block block = villager.world.getBlockState(closestOre).getBlock();
                villager.say(getAssigningPlayer(), "chore.mining.orenotify", block.getLocalizedName());
                pickStack.damageItem(2, villager);
            }
            ticks = 0;
            return;
        }
        ticks++;
    }
}