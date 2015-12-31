package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.core.minecraft.ModBlocks;
import mca.core.minecraft.ModItems;
import mca.data.PlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumDestinyChoice;
import mca.enums.EnumDialogueType;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.data.DataWatcherEx;
import radixcore.math.Point3D;
import radixcore.packets.AbstractPacket;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;
import radixcore.util.SchematicHandler;

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
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		DataWatcherEx.allowClientSideModification = true;
		
		final PacketDestinyChoice packet = (PacketDestinyChoice)message;
		final EntityPlayerMP player = (EntityPlayerMP)this.getPlayer(context);
		final PlayerData data = MCA.getPlayerData(player);
		final WorldServer world = (WorldServer)player.worldObj;

		if (packet.choice == EnumDestinyChoice.NONE || packet.choice == EnumDestinyChoice.CANCEL)
		{
			//Update the region around the player so that the destiny room disappears.
			final PlayerManager manager = world.getPlayerManager();

			for (int x = -20; x < 20; x++)
			{
				for (int z = -20; z < 20; z++)
				{
					for (int y = -5; y < 10; y++)
					{
//						manager.markBlockForUpdate((int)player.posX + x, (int)player.posY + y, (int)player.posZ + z);
						manager.func_180244_a(new BlockPos((int)player.posX + x, (int)player.posY + y, (int)player.posZ + z));
					}
				}
			}

			if (packet.choice == EnumDestinyChoice.CANCEL)
			{
				player.inventory.addItemStackToInventory(new ItemStack(ModItems.crystalBall));
			}
		}

		else
		{
			// Players have previously been able to spawn in structures on dedicated servers.
			// Add a check for the dedicated server and prevent anything from happening.
			if (MinecraftServer.getServer().isDedicatedServer())
			{
				return;
			}
			
			else if (packet.choice == EnumDestinyChoice.FAMILY)
			{
				SchematicHandler.spawnStructureRelativeToPlayer("/assets/mca/schematic/family.schematic", player);

				boolean isSpouseMale = data.getGenderPreference() == 0 ? true : data.getGenderPreference() == 2 ? false : world.rand.nextBoolean();
				EntityHuman spouse = new EntityHuman(world, isSpouseMale);
				spouse.setPosition(player.posX - 2, player.posY, player.posZ);
				world.spawnEntityInWorld(spouse);

				PlayerMemory spouseMemory = spouse.getPlayerMemory(player);
				spouse.setMarriedTo(player);
				spouseMemory.setHearts(100);
				spouseMemory.setDialogueType(EnumDialogueType.SPOUSE);

				int numChildren = RadixMath.getNumberInRange(0, 2);

				while (numChildren > 0)
				{
					boolean isPlayerMale = data.getIsMale();

					String motherName = "N/A";
					int motherId = 0;
					String fatherName = "N/A";
					int fatherId = 0;

					if (isPlayerMale)
					{
						fatherName = player.getName();
						fatherId = data.getPermanentId();
						motherName = spouse.getName();
						motherId = spouse.getPermanentId();
					}

					else
					{
						motherName = player.getName();
						motherId = data.getPermanentId();
						fatherName = spouse.getName();
						fatherId = spouse.getPermanentId();
					}
					
					final EntityHuman child = new EntityHuman(world, RadixLogic.getBooleanWithProbability(50), true, motherName, fatherName, motherId, fatherId, true);
					child.setPosition(player.posX + RadixMath.getNumberInRange(1, 3), player.posY, player.posZ);
					world.spawnEntityInWorld(child);

					PlayerMemory childMemory = child.getPlayerMemory(player);
					childMemory.setHearts(100);
					childMemory.setDialogueType(EnumDialogueType.CHILDP);
					numChildren--;
				}
			}

			else if (packet.choice == EnumDestinyChoice.ALONE)
			{
				SchematicHandler.spawnStructureRelativeToPlayer("/assets/mca/schematic/bachelor.schematic", player);				
			}

			else if (packet.choice == EnumDestinyChoice.VILLAGE)
			{
				SchematicHandler.spawnStructureRelativeToPlayer("/assets/mca/schematic/village1.schematic", player);

				for (Point3D point : RadixLogic.getNearbyBlocks(player, Blocks.mob_spawner, 70))
				{
					BlockHelper.setBlock(player.worldObj, point.iPosX, point.iPosY, point.iPosZ, Blocks.air);
					MCA.naturallySpawnVillagers(new Point3D(point.iPosX, point.iPosY, point.iPosZ), world, -1);
				}

				for (Point3D point : RadixLogic.getNearbyBlocks(player, Blocks.bedrock, 70))
				{
					BlockHelper.setBlock(player.worldObj, point.iPosX, point.iPosY, point.iPosZ, ModBlocks.tombstone);

					final TileTombstone tile = (TileTombstone) BlockHelper.getTileEntity(player.worldObj, point.iPosX, point.iPosY, point.iPosZ);

					if (tile != null)
					{
						tile.signText[1] = RadixLogic.getBooleanWithProbability(50) ? MCA.getLanguageManager().getString("name.male") : MCA.getLanguageManager().getString("name.female");
						tile.signText[2] = "RIP";
					}
				}
			}
		}

		player.worldObj.playSoundAtEntity(player, "portal.travel", 0.5F, 2.0F);
		DataWatcherEx.allowClientSideModification = false;
	}
}
