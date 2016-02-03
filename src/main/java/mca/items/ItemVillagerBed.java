package mca.items;

import java.util.List;

import mca.blocks.BlockVillagerBed;
import mca.core.MCA;
import mca.core.minecraft.ModBlocks;
import mca.enums.EnumBedColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
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

		GameRegistry.registerItem(this, itemName);
	}

	private BlockVillagerBed getBedBlock()
	{
		switch (color)
		{
		case BLUE:
			return ModBlocks.bedBlue;
		case GREEN:
			return ModBlocks.bedGreen;
		case PINK:
			return ModBlocks.bedPink;
		case PURPLE:
			return ModBlocks.bedPurple;
		case RED:
			return ModBlocks.bedRed;
		default:
			return null;
		}
	}
	
	@Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World worldObj, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (worldObj.isRemote)
        {
            return true;
        }
		
        else if (side != EnumFacing.UP)
        {
            return false;
        }
		
        else
        {
            IBlockState state = worldObj.getBlockState(pos);
            Block block = state.getBlock();
            boolean isReplaceable = block.isReplaceable(worldObj, pos);

            if (!isReplaceable)
            {
                pos = pos.offset(EnumFacing.UP);
            }

            int metaCalc = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            EnumFacing horizontalFacing = EnumFacing.getHorizontal(metaCalc);
            BlockPos offsetPos = pos.offset(horizontalFacing);
            boolean offsetIsReplaceable = block.isReplaceable(worldObj, offsetPos);
            boolean posIsAir = worldObj.isAirBlock(pos) || isReplaceable;
            boolean offsetIsAir = worldObj.isAirBlock(offsetPos) || offsetIsReplaceable;

            if (player.canPlayerEdit(pos, side, stack) && player.canPlayerEdit(offsetPos, side, stack))
            {
                if (posIsAir && offsetIsAir && World.doesBlockHaveSolidTopSurface(worldObj, pos.offset(EnumFacing.DOWN)) && World.doesBlockHaveSolidTopSurface(worldObj, offsetPos.offset(EnumFacing.DOWN)))
                {
                    int facingIndex = horizontalFacing.getHorizontalIndex();
                    IBlockState footState = getBedBlock().getDefaultState().withProperty(BlockBed.OCCUPIED, Boolean.valueOf(false)).withProperty(BlockBed.FACING, horizontalFacing).withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT);

                    if (worldObj.setBlockState(pos, footState, 3))
                    {
                        IBlockState headState = footState.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD);
                        worldObj.setBlockState(offsetPos, headState, 3);
                    }

                    --stack.stackSize;
                    return true;
                }
                
                else
                {
                    return false;
                }
            }
            
            else
            {
                return false;
            }
        }
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(MCA.getLanguageManager().getString("information.villagerbed.line1"));
		informationList.add(MCA.getLanguageManager().getString("information.villagerbed.line2"));
		informationList.add(MCA.getLanguageManager().getString("information.villagerbed.line3"));
	}
}
