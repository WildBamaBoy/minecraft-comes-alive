package mca.core.minecraft;

import mca.core.forge.NetMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;

public class WorldEventListenerMCA implements IWorldEventListener {
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
    }

    public void notifyLightSet(BlockPos pos) {
    }

    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
    }

    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
    }

    public void playRecord(SoundEvent soundIn, BlockPos pos) {
    }

    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
    }

    public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
    }

    public void onEntityAdded(Entity entityIn) {
        // Ask the server to send the villager's career ID when it is loaded into the world client-side
        if (entityIn instanceof EntityVillagerMCA) {
            // Career ID is not data managed, but we depend on it to display the proper profession for the villager alongside their name.
            // The ID is randomized client-side with populateBuyingList(), which doesn't affect anything. This throws off the career ID that we send the client.
            // To stop this, we default the career ID and level client-side to 1. This prevents populateBuyingList() from running and allows our career ID sent from the server to apply.
            ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, (EntityVillagerMCA) entityIn, 1, EntityVillagerMCA.VANILLA_CAREER_ID_FIELD_INDEX);
            ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, (EntityVillagerMCA) entityIn, 1, EntityVillagerMCA.VANILLA_CAREER_LEVEL_FIELD_INDEX);
            NetMCA.INSTANCE.sendToServer(new NetMCA.CareerRequest(entityIn.getUniqueID()));

            // The villager's inventory is also not synced to the client until it is opened in a Container.
            // When the entity joins the client world, ask the server to send over the inventory data.
            NetMCA.INSTANCE.sendToServer(new NetMCA.InventoryRequest(entityIn.getUniqueID()));
        }
    }

    public void onEntityRemoved(Entity entityIn) {
    }

    public void broadcastSound(int soundID, BlockPos pos, int data) {
    }

    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
    }

    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
    }
}
