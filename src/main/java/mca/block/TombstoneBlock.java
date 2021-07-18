package mca.block;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import mca.entity.ai.relationship.EntityRelationship;
import mca.entity.ai.relationship.Gender;
import mca.server.world.data.GraveyardManager;
import mca.server.world.data.GraveyardManager.TombstoneState;
import mca.util.VoxelShapeUtil;
import mca.util.localization.FlowingText;

public class TombstoneBlock extends BlockWithEntity implements Waterloggable {

    public static final VoxelShape GRAVELLING_SHAPE = Block.createCuboidShape(1, 0, 1, 15, 1, 15);
    public static final VoxelShape UPRIGHT_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(1, 0, 2, 15, 18, 4),
            Block.createCuboidShape(2, 18, 2, 14, 19, 4),
            Block.createCuboidShape(3, 19, 2, 13, 20, 4)
    );
    public static final VoxelShape CROSS_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(6, 0, 2, 10, 28, 4),
            Block.createCuboidShape(-1, 18, 2, 17, 21, 4)
    );
    public static final VoxelShape SLANTED_SHAPE = Block.createCuboidShape(0, 0, 1, 16, 10, 10);

    private final Map<Direction, VoxelShape> shapes;

    private final int lineWidth;
    private final int maxNameHeight;
    private final Vec3d nameplateOffset;

    public TombstoneBlock(Settings properties, int lineWidth, int maxNameHeight, Vec3d nameplateOffset, VoxelShape baseShape) {
        super(properties);
        setDefaultState(getDefaultState().with(Properties.WATERLOGGED, false));

        this.lineWidth = lineWidth;
        this.maxNameHeight = maxNameHeight;
        this.nameplateOffset = nameplateOffset;
        shapes = Arrays.stream(Direction.values())
                .filter(d -> d.getAxis() != Axis.Y)
                .collect(Collectors.toMap(
                        Function.identity(),
                        VoxelShapeUtil.rotator(baseShape))
                );
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public int getMaxNameHeight() {
        return maxNameHeight;
    }

    public Vec3d getNameplateOffset() {
        return nameplateOffset;
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public boolean canMobSpawnInside() {
        return true;
    }

    @Override
    @Deprecated
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ePos) {
        if (this == BlocksMCA.SLANTED_HEADSTONE) {
            for (Direction i : shapes.keySet()) {
                shapes.put(i, VoxelShapeUtil.rotator(SLANTED_SHAPE).apply(i));
            }
        }

        return shapes.getOrDefault(state.get(Properties.HORIZONTAL_FACING), VoxelShapes.fullCube());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient) {
            GraveyardManager.get((ServerWorld)world).setTombstoneState(pos, TombstoneState.EMPTY);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!world.isClient && !state.isOf(newState.getBlock())) {
            updateNeighbors(state, world, pos);
            GraveyardManager.get((ServerWorld)world).removeTombstoneState(pos);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new Data(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.WATERLOGGED).add(Properties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        if (direction == Direction.DOWN && !canPlaceAt(state, world, pos)) {
            return Blocks.AIR.getDefaultState();
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        pos = pos.down();
        return world.getBlockState(pos).isSideSolid(world, pos, Direction.UP, SideShapeType.FULL);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
       return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    private void updateNeighbors(BlockState state, World world, BlockPos pos) {
        world.updateNeighborsAlways(pos, this);
        world.updateNeighborsAlways(pos.offset(state.get(Properties.HORIZONTAL_FACING)), this);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(Properties.HORIZONTAL_FACING, context.getPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(Properties.HORIZONTAL_FACING, rot.rotate(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.getStrongRedstonePower(world, pos, direction);
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return direction == state.get(Properties.HORIZONTAL_FACING) && hasEntity(world, pos) ? 15 : 0;
    }

    protected boolean hasEntity(BlockView world, BlockPos pos) {
        return Optional.ofNullable(world.getBlockEntity(pos)).filter(p -> p instanceof Data).map(Data.class::cast).map(Data::hasEntity).orElse(false);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
       return true;
    }

    public static class Data extends BlockEntity implements BlockEntityClientSerializable {

        private Optional<EntityData> entityData = Optional.empty();

        @Nullable
        private FlowingText computedName;

        public Data(BlockPos pos, BlockState state) {
            super(BlockEntityTypesMCA.TOMBSTONE, pos, state);
        }

        public void setEntity(@Nullable Entity entity) {
            entityData = Optional.ofNullable(entity).map(e -> new EntityData(
                    writeEntityToNbt(e),
                    e.getDisplayName(),
                    EntityRelationship.of(e).map(EntityRelationship::getGender).orElse(Gender.MALE)
            ));
            computedName = null;
            markDirty();

            if (hasWorld()) {
                world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(getCachedState()));
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos);
                ((TombstoneBlock)getCachedState().getBlock()).updateNeighbors(this.getCachedState(), world, pos);

                if (!world.isClient) {
                    GraveyardManager.get((ServerWorld)world).setTombstoneState(pos,
                            hasEntity() ? GraveyardManager.TombstoneState.EMPTY : GraveyardManager.TombstoneState.FILLED
                    );
                    sync();
                }
            }
        }

        public boolean hasEntity() {
            return entityData.isPresent();
        }

        public Gender getGender() {
            return entityData.map(e -> e.gender).orElse(Gender.MALE);
        }

        public Text getEntityName() {
            return entityData.map(e -> e.name).orElse(LiteralText.EMPTY);
        }

        public FlowingText getOrCreateEntityName(Function<Text, FlowingText> factory) {
            if (computedName == null) {
                computedName = factory.apply(getEntityName());
            }
            return computedName;
        }

        public Optional<Entity> createEntity(World world, boolean remove) {
            try {
                return entityData.flatMap(data -> EntityType.getEntityFromNbt(data.nbt, world));
            } finally {
                if (remove) {
                    setEntity(null);
                }
            }
        }

        private NbtCompound writeEntityToNbt(Entity entity) {
            NbtCompound nbt = new NbtCompound();
            entity.writeNbt(nbt);
            nbt.putString("id", EntityType.getId(entity.getType()).toString());
            return nbt;
        }

        @Override
        public void fromClientTag(NbtCompound tag) {
            readNbt(tag);
        }

        @Override
        public NbtCompound toClientTag(NbtCompound tag) {
            return writeNbt(tag);
        }

        @Override
        public void readNbt(NbtCompound nbt) {
            entityData = nbt.contains("entityData", NbtElement.COMPOUND_TYPE) ? Optional.of(new EntityData(
                    nbt.getCompound("entityData"),
                    Text.Serializer.fromJson(nbt.getString("entityName")),
                    Gender.byId(nbt.getInt("entityGender"))
            )) : Optional.empty();
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
           entityData.ifPresent(data -> data.writeNbt(nbt));
           return super.writeNbt(nbt);
        }

        static record EntityData(
                NbtCompound nbt,
                Text name,
                Gender gender) {

            void writeNbt(NbtCompound nbt) {
                nbt.put("entityData", this.nbt);
                nbt.putString("entityName", Text.Serializer.toJson(name));
                nbt.putInt("entityGender", gender.ordinal());
            }
        }
    }
}
