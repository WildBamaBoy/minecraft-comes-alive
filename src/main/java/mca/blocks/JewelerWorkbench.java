package mca.blocks;

import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class JewelerWorkbench extends Block {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(1.0D, 0.1D, 1.0D, 15.0D, 24.0D, 15.0D);

    public JewelerWorkbench(Settings properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockView world) {
        return null;//return new JewelerWorkbenchTileEntity();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult rayTrace) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        this.interactWith(world, pos, player);
        return ActionResult.CONSUME;
    }

    private void interactWith(World world, BlockPos pos, PlayerEntity player) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public VoxelShape getRayTraceShape(BlockState state, BlockView reader, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public void addInformation(ItemStack item, @Nullable BlockView iBlock, List<Text> tooltip, TooltipContext iTooltipFlag) {
        tooltip.add(new LiteralText("Workbench allows you to buy rings from Jeweler").formatted(Formatting.GRAY));
        tooltip.add(new TranslatableText(String.format("tooltip.%s.block.statue.line1", MCA.MOD_ID)).formatted(Formatting.GRAY));
        tooltip.add(new TranslatableText(String.format("tooltip.%s.block.statue.line2", MCA.MOD_ID)).formatted(Formatting.GRAY));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getPlayerFacing().getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof Inventory) {
                ItemScatterer.spawn(world, pos, (Inventory) tileEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, isMoving);
        }
    }
}
