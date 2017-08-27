package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumDestinyChoice;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.enums.EnumProfession;
import mca.tile.TileTombstone;
import mca.util.Either;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;
import radixcore.modules.net.AbstractPacket;
import radixcore.modules.schematics.RadixSchematics;

public class PacketDestinyChoice extends AbstractPacket<PacketDestinyChoice>
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
	public void processOnGameThread(PacketDestinyChoice packet, MessageContext context) 
	{
		final EntityPlayerMP player = (EntityPlayerMP)this.getPlayer(context);
		final NBTPlayerData data = MCA.getPlayerData(player);
		final WorldServer world = (WorldServer)player.world;

		if (packet.choice == EnumDestinyChoice.NONE || packet.choice == EnumDestinyChoice.CANCEL)
		{
			//Update the region around the player so that the destiny room disappears.
			final PlayerChunkMap manager = world.getPlayerChunkMap();

			for (int x = -20; x < 20; x++)
			{
				for (int z = -20; z < 20; z++)
				{
					for (int y = -5; y < 10; y++)
					{
						manager.markBlockForUpdate(new BlockPos((int)player.posX + x, (int)player.posY + y, (int)player.posZ + z));
					}
				}
			}

			if (packet.choice == EnumDestinyChoice.CANCEL)
			{
				player.inventory.addItemStackToInventory(new ItemStack(ItemsMCA.CRYSTAL_BALL));
			}
		}

		else
		{
			// Players have previously been able to spawn in structures on dedicated servers.
			// Add a check for the dedicated server and prevent anything from happening.
			if (FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer() && !MCA.getConfig().serverEnableStructureSpawning)
			{
				return;
			}
			
			else if (packet.choice == EnumDestinyChoice.FAMILY)
			{
				RadixSchematics.spawnStructureRelativeToPlayer("/assets/mca/schematic/family.schematic", player);

				boolean isSpouseMale = data.getGenderPreference() == EnumGender.MALE ? true : data.getGenderPreference() == EnumGender.FEMALE ? false : world.rand.nextBoolean();

				EntityVillagerMCA spouse = new EntityVillagerMCA(world);
				spouse.attributes.setGender(isSpouseMale ? EnumGender.MALE : EnumGender.FEMALE);
				spouse.attributes.assignRandomName();
				spouse.attributes.assignRandomProfession();
				spouse.attributes.assignRandomPersonality();
				spouse.attributes.assignRandomSkin();
				spouse.setPosition(player.posX - 2, player.posY, player.posZ);
				world.spawnEntity(spouse);

				PlayerMemory spouseMemory = spouse.attributes.getPlayerMemory(player);
				spouse.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withR(player));
				spouseMemory.setHearts(100);
				spouseMemory.setDialogueType(EnumDialogueType.SPOUSE);

				int numChildren = RadixMath.getNumberInRange(0, 2);

				while (numChildren > 0)
				{
					boolean isPlayerMale = data.getGender() == EnumGender.MALE;

					Entity father = isPlayerMale ? player : spouse;
					Entity mother = father == player ? spouse : player;
										
					final EntityVillagerMCA child = new EntityVillagerMCA(world);
					child.attributes.assignRandomGender();
					child.attributes.assignRandomName();
					child.attributes.assignRandomPersonality();
					child.attributes.setProfession(EnumProfession.Child);
					child.attributes.setIsChild(true);
					
					if (father instanceof EntityPlayer)
					{
						child.attributes.setFather(Either.<EntityVillagerMCA, EntityPlayer>withR((EntityPlayer)father));
						child.attributes.setMother(Either.<EntityVillagerMCA, EntityPlayer>withL((EntityVillagerMCA)mother));
					}
					
					else
					{
						child.attributes.setFather(Either.<EntityVillagerMCA, EntityPlayer>withL((EntityVillagerMCA)father));
						child.attributes.setMother(Either.<EntityVillagerMCA, EntityPlayer>withR((EntityPlayer)mother));						
					}
					
					child.setPosition(player.posX + RadixMath.getNumberInRange(1, 3), player.posY, player.posZ);
					world.spawnEntity(child);

					PlayerMemory childMemory = child.attributes.getPlayerMemory(player);
					childMemory.setHearts(100);
					childMemory.setDialogueType(EnumDialogueType.CHILDP);
					numChildren--;
				}
			}

			else if (packet.choice == EnumDestinyChoice.ALONE)
			{
				RadixSchematics.spawnStructureRelativeToPlayer("/assets/mca/schematic/bachelor.schematic", player);				
			}

			else if (packet.choice == EnumDestinyChoice.VILLAGE)
			{
				RadixSchematics.spawnStructureRelativeToPlayer("/assets/mca/schematic/village1.schematic", player);

				for (Point3D point : RadixLogic.getNearbyBlocks(player, Blocks.MOB_SPAWNER, 70))
				{
					RadixBlocks.setBlock(player.world, point, Blocks.AIR);
					MCA.naturallySpawnVillagers(new Point3D(point.iX(), point.iY(), point.iZ()), world, -1);
				}

				for (Point3D point : RadixLogic.getNearbyBlocks(player, Blocks.BEDROCK, 70))
				{
					RadixBlocks.setBlock(player.world, point, BlocksMCA.tombstone);

					final TileTombstone tile = (TileTombstone) player.world.getTileEntity(point.toBlockPos());

					if (tile != null)
					{
						tile.signText[1] = new TextComponentString(RadixLogic.getBooleanWithProbability(50) ? MCA.getLocalizer().getString("name.male") : MCA.getLocalizer().getString("name.female"));
						tile.signText[2] = new TextComponentString("RIP");
					}
				}
			}
		}
	}
}
