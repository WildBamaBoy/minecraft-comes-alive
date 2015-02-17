package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.PlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumDestinyChoice;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.world.WorldServer;
import radixcore.helpers.LogicHelper;
import radixcore.helpers.MathHelper;
import radixcore.packets.AbstractPacket;
import radixcore.util.SchematicHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketDestinyChoice extends AbstractPacket implements IMessage, IMessageHandler<PacketDestinyChoice, IMessage>
{
	private EnumDestinyChoice choice;

	public PacketDestinyChoice()
	{
	}

	public PacketDestinyChoice(EnumDestinyChoice choice)
	{
		this.choice = choice;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		choice = EnumDestinyChoice.fromId(byteBuf.readInt());
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(choice.getId());
	}

	@Override
	public IMessage onMessage(PacketDestinyChoice packet, MessageContext context)
	{
		final EntityPlayerMP player = (EntityPlayerMP)this.getPlayer(context);
		final PlayerData data = MCA.getPlayerData(player);
		final WorldServer world = (WorldServer)player.worldObj;
		
		if (packet.choice == EnumDestinyChoice.NONE)
		{
			//Update the region around the player so that the destiny room disappears.
			final PlayerManager manager = world.getPlayerManager();
			
			for (int x = -20; x < 20; x++)
			{
				for (int z = -20; z < 20; z++)
				{
					for (int y = -5; y < 10; y++)
					{
						manager.markBlockForUpdate((int)player.posX + x, (int)player.posY + y, (int)player.posZ + z);						
					}
				}
			}
		}

		else
		{
			if (packet.choice == EnumDestinyChoice.FAMILY)
			{
				SchematicHandler.spawnStructureRelativeToPlayer("/assets/mca/schematic/family.schematic", player);
				
				boolean isSpouseMale = data.genderPreference.getInt() == 0 ? true : data.genderPreference.getInt() == 2 ? false : world.rand.nextBoolean();
				EntityHuman spouse = new EntityHuman(world, isSpouseMale);
				spouse.setPosition(player.posX - 2, player.posY, player.posZ);
				world.spawnEntityInWorld(spouse);
				
				PlayerMemory spouseMemory = spouse.getPlayerMemory(player);
				spouse.setIsMarried(true, player);
				spouseMemory.setHearts(100);
				
				int numChildren = MathHelper.getNumberInRange(0, 2);
				
				while (numChildren > 0)
				{
					boolean isPlayerMale = data.isMale.getBoolean();

					String motherName = "N/A";
					int motherId = 0;
					String fatherName = "N/A";
					int fatherId = 0;

					if (isPlayerMale)
					{
						fatherName = player.getCommandSenderName();
						fatherId = data.permanentId.getInt();
						motherName = spouse.getName();
						motherId = spouse.getPermanentId();
					}

					else
					{
						motherName = player.getCommandSenderName();
						motherId = data.permanentId.getInt();
						fatherName = spouse.getName();
						fatherId = spouse.getPermanentId();
					}

					final EntityHuman child = new EntityHuman(world, LogicHelper.getBooleanWithProbability(50), true, motherName, fatherName, motherId, fatherId, true);
					child.setPosition(player.posX + MathHelper.getNumberInRange(1, 3), player.posY, player.posZ);
					world.spawnEntityInWorld(child);
					
					PlayerMemory childMemory = child.getPlayerMemory(player);
					childMemory.setHearts(100);
					numChildren--;
				}
			}
			
			else if (packet.choice == EnumDestinyChoice.ALONE)
			{
				SchematicHandler.spawnStructureRelativeToPlayer("/assets/mca/schematic/bachelor.schematic", player);				
			}
		}

		return null;
	}
}
