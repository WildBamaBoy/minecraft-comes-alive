package mca.api.wrappers;

import com.google.common.util.concurrent.ListenableFuture;
import lombok.Getter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class WorldServerWrapper extends WorldWrapper {
	@Getter private WorldServer vanillaWorldServer;

	private WorldServerWrapper(World world) {
		super(world);
	}

	public WorldServerWrapper(WorldServer world) {
		this((World)world);
		this.vanillaWorldServer = world;
	}

	public ListenableFuture<Object> addScheduledTask(Runnable runnable) {
	    return this.vanillaWorldServer.addScheduledTask(runnable);
    }
}
