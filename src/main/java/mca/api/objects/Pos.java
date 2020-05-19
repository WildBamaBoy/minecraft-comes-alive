package mca.api.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.math.BlockPos;

@AllArgsConstructor
public class Pos {
	public static final Pos ORIGIN = new Pos(BlockPos.ORIGIN);
	
	@Getter
	private BlockPos blockPos;
	
	public Pos(int x, int y, int z) {
		blockPos = new BlockPos(x, y, z);
	}

	public Pos(double x, double y, double z) {
		blockPos = new BlockPos(x, y, z);
	}
	
	public int getX() {
		return blockPos.getX();
	}
	
	public int getY() {
		return blockPos.getY();
	}
	
	public int getZ() {
		return blockPos.getZ();
	}
	
	public Pos add(int x, int y, int z) {
		blockPos.add(x, y, z);
		return this;
	}

    public double getDistance(int x, int y, int z) {
		return blockPos.getDistance(x, y, z);
	}

	public Pos down() {
		blockPos = blockPos.down();
		return this;
	}
}
