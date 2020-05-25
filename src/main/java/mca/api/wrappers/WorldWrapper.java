package mca.api.wrappers;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import lombok.Getter;
import mca.api.objects.NPC;
import mca.api.objects.Player;
import mca.api.objects.Pos;
import mca.entity.EntityVillagerMCA;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Implementation of the World class from Minecraft. Encapsulates the original class with methods relevant to MCA passed to the appropriate methods in the Minecraft World class.<br>
 * <br>
 * This allows for invariance across multiple versions of Minecraft / less effort when updating to other versions.<br>
 * <br>
 * Proper usage of WorldWrapper is to instantiate it when working within a particular context where a World is available and you
 * want to guarantee any method calls made against World will remain functional across other Minecraft versions.
 */
public class WorldWrapper {
	@Getter
	private World vanillaWorld;
	public final boolean isRemote;
	public final Random rand;
	
	public WorldWrapper(World world) {
		this.vanillaWorld = world;
		this.isRemote = world.isRemote;
		this.rand = world.rand;
	}

	public static WorldWrapper getOverworld() {
		return new WorldWrapper(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
	}

	public VillageCollection getVillageCollection() {
		return vanillaWorld.getVillageCollection();
	}

	public <T extends Entity> List<T> getEntitiesInArea(Class <? extends T > classEntity, AxisAlignedBB bb) {
		return vanillaWorld.getEntitiesWithinAABB(classEntity, bb);
	}

	public List<Entity> getLoadedEntityList() {
		return vanillaWorld.loadedEntityList;
	}

	public boolean isAir(Pos pos) {
		return vanillaWorld.isAirBlock(pos.getBlockPos());
	}
	
	public boolean spawnEntity(Entity entity) {
		return vanillaWorld.spawnEntity(entity);
	}
	
	public void scheduleUpdate(Pos pos, Block blockIn, int delay) {
		vanillaWorld.scheduleUpdate(pos.getBlockPos(), blockIn, delay);
	}
	
	public Optional<Player> getPlayerEntityByUUID(UUID uuid) {
		EntityPlayer player = vanillaWorld.getPlayerEntityByUUID(uuid);
		if (player != null) {
			return Optional.of(new Player(player));
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<NPC> getNPCByUUID(UUID uuid) {
        Optional<Entity> entity = vanillaWorld.loadedEntityList.stream().filter(e -> e.getUniqueID().equals(uuid)).findFirst();

        if (entity.isPresent() && entity.get() instanceof EntityPlayer) {
        	return Optional.of(new Player((EntityPlayer)entity.get()));
        }

        return entity.isPresent() ? Optional.of(new NPC(entity.get())) : Optional.empty();
	}

	public Optional<EntityVillagerMCA> getVillagerByUUID(UUID uuid) {
		Optional<Entity> entity = vanillaWorld.loadedEntityList.stream().filter(e -> e.getUniqueID().equals(uuid)).findFirst();
        if (entity.isPresent() && entity.get() instanceof EntityVillagerMCA) {
        	return Optional.of((EntityVillagerMCA)entity.get());
        }
        return Optional.empty();
	}

	public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed) {
		vanillaWorld.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed);
	}

	public Entity getClosestSaddledHorseTo(Entity entity) throws NoSuchElementException {
        List<EntityHorse> horses = vanillaWorld.getEntities(EntityHorse.class, h -> (h.isHorseSaddled() && !h.isBeingRidden() && h.getDistance(entity) < 3.0D));
        return horses.stream().min(Comparator.comparingDouble(entity::getDistance)).get();
	}

	public IBlockState getBlockState(Pos pos) {
		return vanillaWorld.getBlockState(pos.getBlockPos());
	}

	public void spawnLightningBolt(int x, int y, int z) {
		EntityLightningBolt lightningBolt = new EntityLightningBolt(vanillaWorld, x, y, z, false);
		vanillaWorld.addWeatherEffect(lightningBolt);
	}

	public Optional<Player> getPlayerEntityByName(String name) {
		EntityPlayer player = vanillaWorld.getPlayerEntityByName(name);
		return player != null ? Optional.of(new Player(player)) : Optional.empty();
	}

	public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
		return vanillaWorld.getDifficultyForLocation(pos);
	}

	public <T extends Entity> List<T> getEntitiesWithinAABB(Class <? extends T > clazz, AxisAlignedBB bb) {
		return vanillaWorld.getEntitiesWithinAABB(clazz, bb);
	}

	public void setBlockToAir(Pos pos) {
		vanillaWorld.setBlockToAir(pos.getBlockPos());
	}

	public long getWorldTime() {
		return vanillaWorld.getWorldTime();
	}

	public void setBlockState(Pos target, IBlockState state) {
		vanillaWorld.setBlockState(target.getBlockPos(), state);
	}

	public void playSound(Player player, Pos pos, SoundEvent event, SoundCategory category, float volume, float pitch) {
		vanillaWorld.playSound(player.getPlayer(), pos.getBlockPos(), event, category, volume, pitch);
	}

	public boolean isRaining() {
		return vanillaWorld.isRaining();
	}
}
