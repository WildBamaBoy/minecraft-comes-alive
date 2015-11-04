package mca.ai;

import com.google.common.collect.ImmutableMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import mca.api.CropEntry;
import mca.api.enums.EnumCropCategory;
import mca.blocks.BlockVillagerSpawner;
import mca.core.MCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumMovementState;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection; 
import net.minecraft.block.state.IBlockState; 
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe; 
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemPickaxe; 
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemSpade; 
import net.minecraft.item.ItemStack; 
import net.minecraft.item.ItemSword; 
import net.minecraft.nbt.CompressedStreamTools; 
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList; 
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import radixcore.data.BlockObj;
import radixcore.data.WatchedBoolean;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
import radixcore.util.RadixExcept; 
import radixcore.util.RadixLogic;
import radixcore.util.SchematicHandler;

public class AIBuild extends AbstractToggleAI
{
	private Map<Point3D, BlockObj> schematicMap;
	private CropEntry cropEntry;

	private List<Point3D> blockPoints;
	private List<Point3D> torchPoints;
	private Point3D origin;
	private WatchedBoolean isAIActive;

	private String schematicName = "none";
	private int interval = MCA.getConfig().ticksPerBuildStep;
	private int index = -1;
	private Block groundBlock;
        
        
        // build menu
        private boolean external = false;
        private boolean useItems = false;
        private byte rotation = 0; //1=90, 2=180, 3=270, ?=0
/*
 * remember to rotate in the metadata, too
 * 0:   x=x     z=z
 * 90:  x=-z    z=x
 * 180: x=-x    z=-z
 * 270: x=z     z=-x
 */
        
        
        
	public AIBuild(EntityHuman owner) 
	{
		super(owner);
		origin = Point3D.ZERO;
		isAIActive = new WatchedBoolean(false, WatcherIDsHuman.IS_BUILDING_ACTIVE, owner.getDataWatcherEx());
	}

	@Override
	public void onUpdateCommon() 
	{	
	}

	@Override
	public void onUpdateClient() 
	{	
	}

	@Override
	public void onUpdateServer() 
	{
		if (index != -1)
		{
			interval--;

			if (interval <= 0)
			{
				interval = MCA.getConfig().ticksPerBuildStep;
				owner.swingItem();
                                

				for (int i = 0; i < MCA.getConfig().cyclesPerBuildStep; i++)
				{
					//Building the main schematic is complete once index is too high. Now place the torches and finish.
					if (index >= blockPoints.size())
					{
						for (Point3D point : torchPoints)
						{
							BlockObj blockObj = schematicMap.get(point);
                                                        
                                                        placeRotatedBlock(point,blockObj);
						}

						owner.setMovementState(EnumMovementState.MOVE);
						reset();
						break;
					}

					else
					{
                                            Point3D point = blockPoints.get(index);
                                            BlockObj firstBlock = schematicMap.get(point);
                                            BlockObj blockObj;
                                            Point3D worldPoint;
                                            BlockPos worldPos;
                                            Block worldBlock;
                                            int worldMeta;
                                            
                                            int skips = 0;
                                            do {
                                                point = blockPoints.get(index);
                                                blockObj = schematicMap.get(point);
                                                worldPoint = rotatePoint(point);
                                                worldPoint = new Point3D(origin.iPosX + worldPoint.iPosX, origin.iPosY + worldPoint.iPosY, origin.iPosZ + worldPoint.iPosZ);
                                                worldPos = new BlockPos(worldPoint.iPosX, worldPoint.iPosY, worldPoint.iPosZ);
                                                worldBlock = BlockHelper.getBlock(owner.worldObj, worldPoint);
                                                worldMeta = worldBlock.getMetaFromState(owner.worldObj.getBlockState(worldPos));
                                                
                                                if (   MCA.getConfig().skipsPerBuildCycle > 0
                                                        && ( groundBlock == null || blockObj.getBlock() != Blocks.grass ) 
                                                        && blockObj.getBlock() == firstBlock.getBlock() && blockObj.getMeta() == firstBlock.getMeta() 
                                                        && blockObj.getBlock() == worldBlock && blockObj.getMeta() == worldMeta  )
                                                {
                                                    index++;
                                                    skips++;
                                                }
                                                else 
                                                {
//                                                    MCA.getLog().debug("Skipped " + Integer.toString(skips) + " blocks when building.");
                                                    skips = -1;
                                                }
                                                
                                            } while (skips > -1 && skips < MCA.getConfig().skipsPerBuildCycle && index < blockPoints.size());
                                                
                                            if (index < blockPoints.size())
                                            {
						index++;
                                                
                                                //collect the block if you have the correct tool
                                                String toolNeeded = worldBlock.getHarvestTool(worldBlock.getStateFromMeta(worldMeta));
                                                ItemStack tool = null;
                                                boolean collect = false;
                                                
//                                                MCA.getLog().debug( "Block " + worldBlock.getLocalizedName() + " needs tool " + ((toolNeeded != null)?toolNeeded:"null") );
                                                
                                                //use the proper tool if neccessary
                                                if (toolNeeded == null || owner.getVillagerInventory().contains(BlockVillagerSpawner.class)) 
                                                {
                                                    collect = true;
                                                }
                                                else
                                                {
                                                    if (toolNeeded.equals("shovel"))
                                                    {
                                                        tool = owner.getVillagerInventory().getBestItemOfType(ItemSpade.class);
                                                    }
                                                    else if (toolNeeded.equals("pickaxe"))
                                                    {
                                                        tool = owner.getVillagerInventory().getBestItemOfType(ItemPickaxe.class);
                                                    }
                                                    else if (toolNeeded.equals("axe"))
                                                    {
                                                        tool = owner.getVillagerInventory().getBestItemOfType(ItemAxe.class);
                                                    }
                                                    else if (toolNeeded.equals("shears"))
                                                    { //nothing "needs" shears to be broken but you get different output. It won't call this line, though
                                                        tool = owner.getVillagerInventory().getBestItemOfType(ItemShears.class);
                                                    }
                                                    else if (toolNeeded.equals("sword"))
                                                    {
                                                        tool = owner.getVillagerInventory().getBestItemOfType(ItemSword.class);
                                                    }
                                                    //how do I add different classes of tools from unknown mods?
                                                }
                                                
                                                if (!collect && tool != null)
                                                {
                                                    tool.attemptDamageItem(1, new Random());
                                                    collect = true;
//                                                    MCA.getLog().debug("Using tool: " + tool.getDisplayName());
                                                }
                                                
                                                
                                                //collect the block if you have the right tool
                                                if (collect)
                                                {
                                                    int fortune = 0;
                                                    if (tool != null)
                                                    {
                                                        NBTTagList nbt = tool.getEnchantmentTagList();
                                                        for (int nbti=0; nbt!=null && nbti<nbt.tagCount(); ++nbti)
                                                        {
                                                            fortune = nbt.getCompoundTagAt(nbti).getInteger("fortune");
                                                            if (fortune > 0) break;
                                                        }
                                                    }
                                                    List<ItemStack> collection = worldBlock.getDrops(owner.worldObj, worldPos, worldBlock.getStateFromMeta(worldMeta), fortune);
                                                    for (ItemStack itemStack : collection)
                                                    {
                                                        owner.getVillagerInventory().addItemStackToInventory(itemStack);
                                                    }
                                                }
                                                
                                                boolean goodToGo = false;
                                                
//                                                MCA.getLog().debug( "Block " + blockObj.getBlock().getStateFromMeta(blockObj.getMeta()).toString() + " to be placed. " + (useItems?"Using items.":"Not using items.") );
                                                //check if item to place is in inventory                                                
                                                if (!useItems || owner.getVillagerInventory().contains(BlockVillagerSpawner.class)) 
                                                {
//                                                    MCA.getLog().debug("Villager not using an item.");
                                                    goodToGo = true;
                                                }
                                                else
                                                {
                                                    //figure out if you have the correct item to place
                                                    if (blockObj.getBlock() != Blocks.air)
                                                    {
                                                        Item item = Item.getByNameOrId(blockObj.getBlock().getStateFromMeta(blockObj.getMeta()).toString());
//                                                        MCA.getLog().debug( "Block " + blockObj.getBlock().getStateFromMeta(blockObj.getMeta()).toString() + " needs item " + ((item != null)?item.getUnlocalizedName():"null") );
                                                        int slot = -1;
                                                        String variant = null;
                                                        
                                                        if (item == null)
                                                        {
                                                            //check for item variation
                                                            variant = getVariant(blockObj.getBlock().getStateFromMeta(blockObj.getMeta()));

                                                            MCA.getLog().debug("Variant: " + ((variant != null)?variant:"null") );
                                                            
                                                            item = Item.getItemFromBlock(blockObj.getBlock());
//                                                            MCA.getLog().debug("Block form.");
                                                        }
//                                                        else MCA.getLog().debug("Item form.");
                                                        
                                                        if (item != null)
                                                        {
                                                            if (variant == null)
                                                            {
                                                                slot = owner.getVillagerInventory().getFirstSlotContainingItem(item);
                                                            }
                                                            else
                                                            {
                                                                int sloti = owner.getVillagerInventory().getFirstSlotContainingItem(item);
                                                                if (sloti >= 0) for (; sloti < owner.getVillagerInventory().getSizeInventory(); ++sloti)
                                                                {
                                                                    ItemStack stack = owner.getVillagerInventory().getStackInSlot(sloti);
                                                                    Item sitem = null;
                                                                    if (stack != null) sitem = stack.getItem();
                                                                    if (sitem != null && sitem.equals(item) && sitem instanceof ItemMultiTexture)
                                                                    {
                                                                        ItemMultiTexture imt = (ItemMultiTexture)sitem;
                                                                        String var = getVariant(imt.block.getStateFromMeta( imt.getMetadata(stack.getMetadata())) );
//                                                                        MCA.getLog().debug("Inventory item: " + sitem.getUnlocalizedName() + " " + var);
                                                                        if (var.equals(variant)) 
                                                                        {
                                                                            slot = sloti;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        else
                                                        {
                                                            //special handling here
                                                            MCA.getLog().debug("Unknown building block.");
                                                        }
                                                        
                                                        if (slot >= 0)
                                                        {
                                                            MCA.getLog().debug("Found " + item.getUnlocalizedName() + " in slot " + Integer.toString(slot));
                                                            owner.getVillagerInventory().decrStackSize(slot, 1);
                                                            goodToGo = true;
                                                        }
                                                    }
                                                    else goodToGo = true;
                                                    
                                                }

                                                //the actual block placing
                                                if (goodToGo)
                                                {
                                                    if (blockObj.getBlock() == Blocks.grass && groundBlock != null)
                                                    {
                                                        placeRotatedBlock(point,new BlockObj(groundBlock,groundBlock.getMetaFromState(groundBlock.getDefaultState())));			
                                                    }

                                                    else
                                                    {
                                                        placeRotatedBlock(point,blockObj);
                                                    }
                                                }
                                                else if (worldBlock != Blocks.air) placeRotatedBlock(point,new BlockObj(Blocks.air,Blocks.air.getMetaFromState(Blocks.air.getDefaultState())));
                                            }
					}
				}
			}
		}
	}
        
        public String getVariant(IBlockState bs)
        {
            String variant = null;
            if (bs != null)
            {
                ImmutableMap props = bs.getProperties();
                Collection propnames = bs.getPropertyNames();
                if (props != null && propnames != null) for (Object obj : propnames)
                {
                    String str = obj.toString();
                    if (str.startsWith("PropertyEnum{name=variant,"))
                    {
                        variant = props.get(obj).toString();
                        break;
                    }
                }
            }
            return variant;
        }
        
        private void placeRotatedBlock(Point3D point, BlockObj blockObj) 
        {
                                                EnumFacing facing;
                                                IBlockState blockstate = blockObj.getBlock().getStateFromMeta(blockObj.getMeta());
                                                PropertyDirection propertyDirection = null;
                                                for (Object property : blockstate.getProperties().keySet()) 
                                                { // example: https://gist.github.com/TheGreyGhost/d3e89af8f4121bd63acc
                                                    if (property instanceof PropertyDirection) 
                                                    {
                                                        propertyDirection = (PropertyDirection) property;
                                                        break;
                                                    }
                                                }

                                                int x;
                                                int z;
                                                int currentHorizontalIndex = -1;
                                                int newHorizontalIndex  = -1;
                                                EnumFacing newFacing = null;

                                                if (propertyDirection != null)
                                                {
                                                    facing = (EnumFacing)blockstate.getValue(propertyDirection);
                                                    currentHorizontalIndex = facing.getHorizontalIndex();
                                                }

                                                
                                                switch (rotation)
                                                {
                                                    case 1: //90
                                                        x = point.iPosZ*-1;
                                                        z = point.iPosX;
                                                        if (currentHorizontalIndex >= 0) {
                                                            newHorizontalIndex  = (currentHorizontalIndex + 1) & 3;
                                                            newFacing = EnumFacing.getHorizontal(newHorizontalIndex);
                                                        }
                                                        break;
                                                    case 2: //180
                                                        x = point.iPosX*-1;
                                                        z = point.iPosZ*-1;
                                                        if (currentHorizontalIndex >= 0) {
                                                            newHorizontalIndex  = (currentHorizontalIndex + 2) & 3;
                                                            newFacing = EnumFacing.getHorizontal(newHorizontalIndex);
                                                        }
                                                        break;
                                                    case 3: //270
                                                        x = point.iPosZ;
                                                        z = point.iPosX*-1;
                                                        if (currentHorizontalIndex >= 0) {
                                                            newHorizontalIndex  = (currentHorizontalIndex + 3) & 3;
                                                            newFacing = EnumFacing.getHorizontal(newHorizontalIndex);
                                                        }
                                                        break;
                                                    default: //0
                                                        x = point.iPosX;
                                                        z = point.iPosZ;
                                                        newFacing = null;
                                                }

                                                if (newFacing != null)
                                                {
                                                    blockstate = blockstate.withProperty(propertyDirection, newFacing);
                                                }
                                                BlockHelper.setBlock(owner.worldObj, origin.iPosX + x, origin.iPosY + point.iPosY, origin.iPosZ + z, blockstate);
        }
        
        private Point3D rotatePoint(Point3D point)
        {
            int x;
            int z;
            switch (rotation)
            {
                case 1: //90
                    x = point.iPosZ*-1;
                    z = point.iPosX;
                    break;
                case 2: //180
                    x = point.iPosX*-1;
                    z = point.iPosZ*-1;
                    break;
                case 3: //270
                    x = point.iPosZ;
                    z = point.iPosX*-1;
                    break;
                default: //0
                    x = point.iPosX;
                    z = point.iPosZ;
            }
            return new Point3D(x,point.iPosY,z);
        }

	@Override
	public void reset() 
	{
		setIsActive(false);
		schematicMap = null;
		origin = Point3D.ZERO;
		blockPoints.clear();
		torchPoints.clear();
		groundBlock = null;
		cropEntry = null;
                
                
                external = false;
                useItems = false;
                rotation = 0;

		schematicName = "none";
		index = -1; //-1 index indicates building is not taking place.
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setString("schematicName", schematicName);
		nbt.setInteger("interval", interval);
		nbt.setInteger("index", index);
		nbt.setInteger("originX", origin.iPosX);
		nbt.setInteger("originY", origin.iPosY);
		nbt.setInteger("originZ", origin.iPosZ);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		schematicName = nbt.getString("schematicName");
		interval = nbt.getInteger("interval");
		index = nbt.getInteger("index");

		int originX = nbt.getInteger("originX");
		int originY = nbt.getInteger("originY");
		int originZ = nbt.getInteger("originZ");

		if (index != -1)
		{
			primeSchematic(schematicName);
			origin = new Point3D(originX, originY, originZ);
		}
	}

	/**
	 * Begins building the specified schematic if possible for this area.
	 * 
	 * @param 	schematicLocation	The location of the schematic to place in the world.
	 * @param	doTopDown			Whether or not the schematic should be built from the top down.
	 * 
	 * @return	True if the building can begin. False if otherwise.
	 */
	public boolean startBuilding(String schematicLocation, boolean doTopDown)
	{
		if (RadixLogic.getNearbyBlocks(owner, Blocks.planks, 10).size() != 0)
		{			
			return false;
		}

		else
		{
			this.origin = new Point3D(owner.posX, owner.posY + 1, owner.posZ);
			this.owner.setMovementState(EnumMovementState.STAY);

			primeSchematic(schematicLocation);

			setIsActive(true);
			return true;
		}
	}

	public boolean startBuilding(String schematicLocation, boolean doTopDown, Block groundBlock)
	{
		this.groundBlock = groundBlock;
		return startBuilding(schematicLocation, doTopDown);
	}

	public boolean startBuilding(String schematicLocation, boolean doTopDown, Block groundBlock, CropEntry cropEntry)
	{
		this.cropEntry = cropEntry;
		return startBuilding(schematicLocation, doTopDown, groundBlock);
	}
        
        
        public boolean startBuilding(String schematicLocation, boolean doTopDown, Block groundBlock, CropEntry cropEntry, int rotation)
        {
		this.rotation = (byte)rotation;
		return startBuilding(schematicLocation, doTopDown, groundBlock, cropEntry);
        }
        
        
        public boolean startBuilding(String schematicLocation, boolean doTopDown, Block groundBlock, CropEntry cropEntry, int rotation, boolean external)
        {
		this.external = external;
		return startBuilding(schematicLocation, doTopDown, groundBlock, cropEntry, rotation);
        }
        
        
        public boolean startBuilding(String schematicLocation, boolean doTopDown, Block groundBlock, CropEntry cropEntry, int rotation, boolean external, boolean useThoseItems)
        {
		this.useItems = useThoseItems;
		return startBuilding(schematicLocation, doTopDown, groundBlock, cropEntry, rotation, external);
        }

	private void primeSchematic(String schematicLocation)
	{
		schematicName = schematicLocation;
		blockPoints = new ArrayList<Point3D>();
		torchPoints = new ArrayList<Point3D>();
		if (!external) schematicMap = SchematicHandler.readSchematic(schematicLocation);
                else schematicMap = readExternalSchematic(schematicLocation);
		index = 0;

		int compareY = -25;

		for (final Map.Entry<Point3D, BlockObj> entry: schematicMap.entrySet())
		{
			final Point3D point = entry.getKey();
//                        Point3D pt = rotatePoint(point);
//			Block blockAtPoint = BlockHelper.getBlock(owner.worldObj, origin.iPosX + pt.iPosX, origin.iPosY + pt.iPosY, origin.iPosZ + pt.iPosZ);
			
//			if (blockAtPoint == Blocks.tallgrass || blockAtPoint == Blocks.red_flower || blockAtPoint == Blocks.double_plant || blockAtPoint == Blocks.yellow_flower)
//			{
//                                
//				BlockHelper.setBlock(owner.worldObj, origin.iPosX + pt.iPosX, origin.iPosY + pt.iPosY, origin.iPosZ + pt.iPosZ, Blocks.air);
//			}
			
			compareY = -25;

			while (compareY < 25)
			{
				if (schematicMap.get(point).getBlock() == Blocks.torch)
				{
					torchPoints.add(point);
				}

				else if (point.iPosY == compareY)
				{
					blockPoints.add(point);
				}

				compareY++;
			}
		}

		//Modify the schematic as needed if a crop entry is provided.
		if (cropEntry != null)
		{
			final BlockObj searchRefBlock = new BlockObj(Blocks.wool, cropEntry.getCategory().getReferenceMeta());
			final BlockObj waterRefBlock = new BlockObj(Blocks.wool, 11);
			final BlockObj farmland = new BlockObj(Blocks.farmland, 0);
			final BlockObj cropBlock = new BlockObj(cropEntry.getCropBlock(), 0);
			final BlockObj waterBlock = new BlockObj(Blocks.water, 0);

			final Map<Point3D, BlockObj> changes = new HashMap<Point3D, BlockObj>();

			for (final Map.Entry<Point3D, BlockObj> entry : schematicMap.entrySet())
			{
				if (entry.getValue().equals(searchRefBlock))
				{
					final Point3D key = entry.getKey();
					final Point3D belowKey = SchematicHandler.getPoint3DWithValue(schematicMap, new Point3D(key.iPosX, key.iPosY - 1, key.iPosZ));

					if (belowKey != null && cropEntry.getCategory() != EnumCropCategory.SUGARCANE) 
					{
						changes.put(belowKey, farmland);
					}
					
					changes.put(entry.getKey(), cropBlock);
				}

				else if (entry.getValue().equals(waterRefBlock))
				{
					changes.put(entry.getKey(), waterBlock);
				}
			}

			for (final Map.Entry<Point3D, BlockObj> entry : changes.entrySet())
			{
				schematicMap.put(entry.getKey(), entry.getValue());
			}
		}
                
//                MCA.getLog().debug("Dump villager inventory.");
//                for (int i = 0; i < owner.getVillagerInventory().getSizeInventory(); ++i)
//		{
//			ItemStack stackInInventory = owner.getVillagerInventory().getStackInSlot(i);
//
//			if (stackInInventory != null)
//			{
//				final String itemClassName = stackInInventory.getItem().getClass().getName();
//
//				MCA.getLog().debug(itemClassName);
//			}
//		}
	}

	@Override
	public void setIsActive(boolean value) 
	{
		isAIActive.setValue(value);
	}

	@Override
	public boolean getIsActive() 
	{
		return isAIActive.getBoolean();
	}

	@Override
	protected String getName() 
	{
		return "Building";
	}
        
        private static SortedMap<Point3D, BlockObj> readExternalSchematic(String location) 
	{ // stolen from RadixCore schematic handler with some modifications
		Point3D origin = null;
		Point3D offset = null;

		SortedMap<Point3D, BlockObj> map = new TreeMap<Point3D, BlockObj>();

		try
		{
			NBTTagCompound nbtdata = CompressedStreamTools.readCompressed(new FileInputStream(location)); // edited line

			short width = nbtdata.getShort("Width");
			short height = nbtdata.getShort("Height");
			short length = nbtdata.getShort("Length");

			byte[] blockIds = nbtdata.getByteArray("Blocks");
			byte[] data	= nbtdata.getByteArray("Data");
			byte[] addIds = new byte[0];
			short[] blocks = new short[blockIds.length];

			if (nbtdata.hasKey("AddBlocks")) 
			{
				addIds = nbtdata.getByteArray("AddBlocks");
			}

			try 
			{
				int originX = nbtdata.getInteger("WEOriginX");
				int originY = nbtdata.getInteger("WEOriginY");
				int originZ = nbtdata.getInteger("WEOriginZ");
				Point3D min = new Point3D(originX, originY, originZ);

				int offsetX = nbtdata.getInteger("WEOffsetX");
				int offsetY = nbtdata.getInteger("WEOffsetY");
				int offsetZ = nbtdata.getInteger("WEOffsetZ");
				offset = new Point3D(offsetX, offsetY, offsetZ);

				origin = new Point3D(min.iPosX - offset.iPosX, min.iPosY - offset.iPosY, min.iPosZ - offset.iPosZ);
			} 

			catch (Exception ignore) 
			{
				origin = Point3D.ZERO;
			}

			for (int index = 0; index < blockIds.length; index++) 
			{
				if ((index >> 1) >= addIds.length) 
				{
					blocks[index] = (short) (blockIds[index] & 0xFF);
				} 

				else 
				{
					if ((index & 1) == 0) 
					{
						blocks[index] = (short) (((addIds[index >> 1] & 0x0F) << 8) + (blockIds[index] & 0xFF));
					} 

					else 
					{
						blocks[index] = (short) (((addIds[index >> 1] & 0xF0) << 4) + (blockIds[index] & 0xFF));
					}
				}
			}

			for (int x = 0; x < width; ++x) 
			{
				for (int y = 0; y < height; ++y) 
				{
					for (int z = 0; z < length; ++z) 
					{
						int index = y * width * length + z * width + x;
						Point3D point = new Point3D(x + offset.iPosX, y + offset.iPosY - 1, z + offset.iPosZ);
						BlockObj block = new BlockObj(Block.getBlockById(blocks[index]), data[index]);

						map.put(point, block);
					}
				}
			}
		}

		catch (IOException e)
		{
			RadixExcept.logFatalCatch(e, "Encountered a fatal error while reading a schematic.");
		}

		return map;
	}

}

