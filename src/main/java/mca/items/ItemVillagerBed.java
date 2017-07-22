package mca.items;

import java.util.List;

import mca.blocks.BlockVillagerBed;
import mca.core.MCA;
import mca.core.minecraft.BlocksMCA;
import mca.enums.EnumBedColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemVillagerBed extends Item
{
	private EnumBedColor color;
	
	public ItemVillagerBed(EnumBedColor color)
	{
		final String itemName = "ItemVillagerBed" + color.toString();
		
		this.color = color;
		this.setCreativeTab(MCA.getCreativeTabMain());
		this.setMaxStackSize(1);
		this.setUnlocalizedName(itemName);
	}

	private BlockVillagerBed getBedBlock()
	{
		switch (color)
		{
		case BLUE:
			return BlocksMCA.bedBlue;
		case GREEN:
			return BlocksMCA.bedGreen;
		case PINK:
			return BlocksMCA.bedPink;
		case PURPLE:
			return BlocksMCA.bedPurple;
		case RED:
			return BlocksMCA.bedRed;
		default:
			return null;
		}
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		
		if (world.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }
		
        else if (side != EnumFacing.UP)
        {
            return EnumActionResult.FAIL;
        }
		
        else
        {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            boolean isReplaceable = block.isReplaceable(world, pos);

            if (!isReplaceable)
            {
                pos = pos.offset(EnumFacing.UP);
            }

            int metaCalc = MathHelper.floor((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            EnumFacing horizontalFacing = EnumFacing.getHorizontal(metaCalc);
            BlockPos offsetPos = pos.offset(horizontalFacing);
            boolean offsetIsReplaceable = block.isReplaceable(world, offsetPos);
            boolean posIsAir = world.isAirBlock(pos) || isReplaceable;
            boolean offsetIsAir = world.isAirBlock(offsetPos) || offsetIsReplaceable;

            if (player.canPlayerEdit(pos, side, stack) && player.canPlayerEdit(offsetPos, side, stack))
            {
            	BlockPos offsetDown = pos.offset(EnumFacing.DOWN);
            	
                if (posIsAir && offsetIsAir && world.isSideSolid(offsetDown, EnumFacing.UP))
                {
                    IBlockState footState = getBedBlock().getDefaultState().withProperty(BlockBed.OCCUPIED, Boolean.valueOf(false)).withProperty(BlockBed.FACING, horizontalFacing).withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT);

                    if (world.setBlockState(pos, footState, 3))
                    {
                        IBlockState headState = footState.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD);
                        world.setBlockState(offsetPos, headState, 3);
                    }

                    stack.shrink(1);
                    return EnumActionResult.SUCCESS;
                }
                
                else
                {
                    return EnumActionResult.FAIL;
                }
            }
            
            else
            {
                return EnumActionResult.FAIL;
            }
        }
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) 
	{
		tooltip.add(MCA.getLocalizer().getString("information.villagerbed.line1"));
		tooltip.add(MCA.getLocalizer().getString("information.villagerbed.line2"));
		tooltip.add(MCA.getLocalizer().getString("information.villagerbed.line3"));
	}
}
