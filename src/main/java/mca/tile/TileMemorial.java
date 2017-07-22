package mca.tile;

import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.entity.EntityVillagerMCA;
import mca.entity.VillagerAttributes;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.enums.EnumMemorialType;
import mca.enums.EnumRelation;
import mca.packets.PacketMemorialUpdateGet;
import mca.util.Either;
import mca.util.Utilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import radixcore.constant.Time;
import radixcore.modules.RadixBlocks;

public class TileMemorial extends TileEntity implements ITickable
{
	private EnumMemorialType type;
	private VillagerAttributes data;
	private String ownerName;
	private EnumRelation ownerRelation;
	private int revivalTicks;
	private EntityPlayer player;
	private boolean hasSynced;

	@Override
	public void update()
	{
		if (world.isRemote && !hasSynced)
		{
			hasSynced = true;
			MCA.getPacketHandler().sendPacketToServer(new PacketMemorialUpdateGet(this));
		}

		if (!world.isRemote)
		{
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			
			if (player == null || player.isDead) //Skip if the player is gone somehow, either on reload or logout.
			{
				revivalTicks = 0;
			}

			if (revivalTicks == 1) //Last tick
			{	
				EntityVillagerMCA human = new EntityVillagerMCA(world);

				human.attributes.copyFrom(data);
				human.setPosition(x + 0.5D, y, z + 0.5D);
				world.spawnEntity(human);

				RadixBlocks.setBlock(world, x, y, z, Blocks.AIR);
				Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.VILLAGER_HAPPY, human, 32);
				Utilities.spawnParticlesAroundPointS(EnumParticleTypes.FIREWORKS_SPARK, world, x + 0.5D, y, z + 0.5D, 16);
				player.playSound(SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, 3.0F, 1.0F);

				if (this.ownerRelation == EnumRelation.NONE)
				{
					return;
				}
				
				else if (this.getType() == EnumMemorialType.BROKEN_RING)
				{
					human.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withR(player));
					human.attributes.getPlayerMemory(player).setHearts(100);
				}

				else
				{
					PlayerMemory memory = human.attributes.getPlayerMemory(player);
					memory.setHearts(100);
					memory.setDialogueType(EnumDialogueType.CHILDP);
					memory.setRelation(human.attributes.getGender() == EnumGender.MALE ? EnumRelation.SON : EnumRelation.DAUGHTER);
				}
			}

			else if (revivalTicks > 0)
			{
				revivalTicks--;
				Utilities.spawnParticlesAroundPointS(EnumParticleTypes.SPELL_INSTANT, world, x + 0.5D, y, z + 0.5D, 2);

				if (revivalTicks == Time.SECOND * 2 || revivalTicks == Time.SECOND * 1)
				{
					player.playSound(SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, 3.0F, 1.0F);
					Utilities.spawnParticlesAroundPointS(EnumParticleTypes.FIREWORKS_SPARK, world, x + 0.5D, y, z + 0.5D, 32);	
				}

				if (revivalTicks < Time.SECOND * 2)
				{
					Utilities.spawnParticlesAroundPointS(EnumParticleTypes.VILLAGER_HAPPY, world, x + 0.5D, y, z + 0.5D, 2);
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setInteger("type", type.getId());
		data.writeToNBT(nbt);
		nbt.setString("ownerName", ownerName);
		nbt.setInteger("relation", ownerRelation.getId());
		
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		data = new VillagerAttributes(nbt);
		type = EnumMemorialType.fromId(nbt.getInteger("type"));
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

	public VillagerAttributes getVillagerSaveData()
	{
		return data;
	}

	public void setVillagerSaveData(VillagerAttributes data)
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
