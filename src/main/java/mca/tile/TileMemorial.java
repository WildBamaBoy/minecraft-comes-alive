package mca.tile;

import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.data.VillagerSaveData;
import mca.entity.EntityHuman;
import mca.enums.EnumDialogueType;
import mca.enums.EnumMemorialType;
import mca.enums.EnumRelation;
import mca.packets.PacketMemorialUpdateGet;
import mca.util.MarriageHandler;
import mca.util.Utilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import radixcore.constant.Time;
import radixcore.util.BlockHelper;

public class TileMemorial extends TileEntity implements ITickable
{
	private EnumMemorialType type;
	private VillagerSaveData data;
	private String ownerName;
	private EnumRelation ownerRelation;
	private int revivalTicks;
	private EntityPlayer player;
	private boolean hasSynced;

	public TileMemorial()
	{

	}

	@Override
	public void update()
	{
		if (worldObj.isRemote && !hasSynced)
		{
			hasSynced = true;
			MCA.getPacketHandler().sendPacketToServer(new PacketMemorialUpdateGet(this));
		}

		if (!worldObj.isRemote)
		{
			int xCoord = pos.getX();
			int yCoord = pos.getY();
			int zCoord = pos.getZ();
			
			if (player == null || player.isDead) //Skip if the player is gone somehow, either on reload or logout.
			{
				revivalTicks = 0;
			}

			if (revivalTicks == 1) //Last tick
			{	
				EntityHuman human = new EntityHuman(worldObj);

				data.applyToHuman(human);
				human.setPosition(xCoord + 0.5D, yCoord, zCoord + 0.5D);
				worldObj.spawnEntityInWorld(human);

				BlockHelper.setBlock(worldObj, xCoord, yCoord, zCoord, Blocks.air);
				Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.VILLAGER_HAPPY, human, 32);
				Utilities.spawnParticlesAroundPointS(EnumParticleTypes.FIREWORKS_SPARK, worldObj, xCoord + 0.5D, yCoord, zCoord + 0.5D, 16);
				this.worldObj.playSoundAtEntity(player, "fireworks.largeBlast", 3.0F, 1.0F);

				if (this.getType() == EnumMemorialType.BROKEN_RING)
				{
					MarriageHandler.startMarriage(player, human);
					human.getPlayerMemory(player).setHearts(100);
				}

				else
				{
					PlayerMemory memory = human.getPlayerMemory(player);
					memory.setHearts(100);
					memory.setDialogueType(EnumDialogueType.CHILDP);
					memory.setRelation(human.getIsMale() ? EnumRelation.SON : EnumRelation.DAUGHTER);
				}
			}

			else if (revivalTicks > 0)
			{
				revivalTicks--;
				Utilities.spawnParticlesAroundPointS(EnumParticleTypes.SPELL_INSTANT, worldObj, xCoord + 0.5D, yCoord, zCoord + 0.5D, 2);

				if (revivalTicks == Time.SECOND * 2 || revivalTicks == Time.SECOND * 1)
				{
					this.worldObj.playSoundAtEntity(player, "fireworks.largeBlast", 3.0F, 1.0F);
					Utilities.spawnParticlesAroundPointS(EnumParticleTypes.FIREWORKS_SPARK, worldObj, xCoord + 0.5D, yCoord, zCoord + 0.5D, 32);	
				}

				if (revivalTicks < Time.SECOND * 2)
				{
					Utilities.spawnParticlesAroundPointS(EnumParticleTypes.VILLAGER_HAPPY, worldObj, xCoord + 0.5D, yCoord, zCoord + 0.5D, 2);
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setInteger("type", type.getId());
		data.writeDataToNBT(nbt);
		nbt.setString("ownerName", ownerName);
		nbt.setInteger("relation", ownerRelation.getId());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		type = EnumMemorialType.fromId(nbt.getInteger("type"));
		data = VillagerSaveData.fromNBT(nbt);
		ownerName = nbt.getString("ownerName");
		ownerRelation = EnumRelation.getById(nbt.getInteger("relation"));
	}

	public void setType(EnumMemorialType type)
	{
		this.type = type;
	}

	public EnumMemorialType getType()
	{
		return type;
	}

	public VillagerSaveData getVillagerSaveData()
	{
		return data;
	}

	public void setVillagerSaveData(VillagerSaveData data)
	{
		this.data = data;
	}

	public void setRevivalTicks(int value)
	{
		this.revivalTicks = value;
	}

	public void setPlayer(EntityPlayer player)
	{
		this.player = player;
	}

	public void setOwnerName(String ownerName)
	{
		this.ownerName = ownerName;
	}

	public void setRelation(EnumRelation relation)
	{
		this.ownerRelation = relation;
	}

	public EnumRelation getRelation()
	{
		return this.ownerRelation;
	}

	public String getOwnerName()
	{
		return this.ownerName;
	}

	public int getRevivalTicks()
	{
		return revivalTicks;
	}
}
