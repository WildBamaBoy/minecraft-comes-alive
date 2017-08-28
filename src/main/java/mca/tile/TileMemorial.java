package mca.tile;

import java.util.UUID;

import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.data.TransitiveVillagerData;
import mca.entity.EntityVillagerMCA;
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
import net.minecraft.util.SoundCategory;
import radixcore.constant.Time;
import radixcore.modules.RadixBlocks;

public class TileMemorial extends TileEntity implements ITickable
{
	private EnumMemorialType type;
	private TransitiveVillagerData data;
	private String ownerName;
	private EnumRelation ownerRelation;
	private UUID ownerUUID;
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
				world.playSound(null, pos, SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, SoundCategory.AMBIENT, 3.0F, 1.0F);

				if (this.ownerRelation == EnumRelation.NONE)
				{
					return;
				}
				
				else if (this.getType() == EnumMemorialType.BROKEN_RING)
				{
					PlayerMemory memory = human.attributes.getPlayerMemory(player);
					human.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withR(player));
					memory.setHearts(100);
					memory.setDialogueType(EnumDialogueType.SPOUSE);
					memory.setRelation(human.attributes.getGender() == EnumGender.MALE ? EnumRelation.HUSBAND : EnumRelation.WIFE);
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
					world.playSound(null, pos, SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, SoundCategory.AMBIENT, 3.0F, 1.0F);
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
		nbt.setUniqueId("ownerUUID", ownerUUID);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		data = new TransitiveVillagerData(nbt);
		type = EnumMemorialType.fromId(nbt.getInteger("type"));
		ownerName = nbt.getString("ownerName");
		ownerRelation = EnumRelation.getById(nbt.getInteger("relation"));
		ownerUUID = nbt.getUniqueId("ownerUUID");
	}

	public void setType(EnumMemorialType type)
	{
		this.type = type;
	}

	public EnumMemorialType getType()
	{
		return type;
	}

	public TransitiveVillagerData getTransitiveVillagerData()
	{
		return data;
	}

	public void setTransitiveVillagerData(TransitiveVillagerData data)
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
		return this.revivalTicks;
	}
	
	public UUID getOwnerUUID()
	{
		return this.ownerUUID;
	}

	public void setOwnerUUID(UUID uuid) 
	{
		this.ownerUUID = uuid;
	}
}

