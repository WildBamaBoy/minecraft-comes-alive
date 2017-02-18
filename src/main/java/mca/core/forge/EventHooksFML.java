package mca.core.forge;

import java.util.ArrayList;
import java.util.List;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModItems;
import mca.data.NBTPlayerData;
import mca.data.PlayerData;
import mca.data.PlayerDataCollection;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumBabyState;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionSkinGroup;
import mca.items.ItemGemCutter;
import mca.packets.PacketPlayerDataLogin;
import mca.packets.PacketSpawnLightning;
import mca.packets.PacketSyncConfig;
import mca.util.Utilities;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class EventHooksFML 
{
	public static boolean playPortalAnimation;
	private static int summonCounter;
	private static Point3D summonPos;
	private static World summonWorld;
	
	private int clientTickCounter;
	private int serverTickCounter;
	
	@SubscribeEvent
	public void onConfigChanges(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
	{
		if (eventArgs.getModID().equals(MCA.ID))
		{
			MCA.getConfig().getInstance().save();
			MCA.getConfig().syncConfiguration();
		}
	}

	@SubscribeEvent
	public void playerLoggedInEventHandler(PlayerLoggedInEvent event)
	{
		//In 5.2, we've migrated from storing our own data files to using WorldSavedData.
		//Upon login, we check for this old data and migrate it to the new object we use.
		EntityPlayer player = event.player;
		PlayerDataCollection dataCollection = PlayerDataCollection.get();
		boolean setPermanentId = false;
		
		//Continue to create the old player data object so any existing player data file is read.
		NBTPlayerData nbtData = null;
		PlayerData data = null;
		data = new PlayerData(player);

		if (data.dataExists())
		{
			data = data.readDataFromFile(event.player, PlayerData.class, null);
			dataCollection.migrateOldPlayerData(player, data);
			nbtData = dataCollection.getPlayerData(player.getUniqueID());
		}

		//If no old data exists, check to see if new data exists. If not, create and store it.
		else if (dataCollection.getPlayerData(player.getUniqueID()) == null)
		{
			//A permanent ID is generated if no ID exists after reading from NBT.
			NBTPlayerData nbtPlayerData = new NBTPlayerData();
			dataCollection.putPlayerData(player.getUniqueID(), nbtPlayerData);
			nbtData = nbtPlayerData;
			setPermanentId = true;
		}

		else
		{
			nbtData = dataCollection.getPlayerData(player.getUniqueID());
		}
				
		//Sync the server's configuration, for display settings.
		MCA.getPacketHandler().sendPacketToPlayer(new PacketSyncConfig(MCA.getConfig()), (EntityPlayerMP)event.player);

		//Send copy of the player data to the client.
		if (nbtData != null)
		{
			MCA.getPacketHandler().sendPacketToPlayer(new PacketPlayerDataLogin(nbtData), (EntityPlayerMP) player);
			
			if (setPermanentId)
			{
				nbtData.setPermanentId(RadixLogic.generatePermanentEntityId(player));
			}
			
			//Add the crystal ball to the inventory if needed.
			if (!nbtData.getHasChosenDestiny() && !player.inventory.hasItemStack(new ItemStack(ModItems.crystalBall)) && MCA.getConfig().giveCrystalBall)
			{
				player.inventory.addItemStackToInventory(new ItemStack(ModItems.crystalBall));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTickEventHandler(ClientTickEvent event)
	{
		MCA.getPacketHandler().processPackets(Side.CLIENT);

		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
		net.minecraft.client.gui.GuiScreen currentScreen = mc.currentScreen;

		if (currentScreen instanceof net.minecraft.client.gui.GuiMainMenu && MCA.myPlayerData != null)
		{
			playPortalAnimation = false;
			MCA.destinyCenterPoint = null;
			MCA.destinySpawnFlag = false;
			MCA.myPlayerData = null;
			MCA.resetConfig();
		}

		//Check for setting/processing the flag for loading language again.
		if (currentScreen instanceof net.minecraft.client.gui.GuiLanguage)
		{
			MCA.reloadLanguage = true;
		}

		else if (MCA.reloadLanguage)
		{
			MCA.reloadLanguage = false;
			MCA.getLanguageManager().loadLanguage(MCA.getLanguageManager().getGameLanguageID());
		}

		if (playPortalAnimation)
		{
			EntityPlayerSP player = (EntityPlayerSP)mc.thePlayer;

			if (player == null)
			{
				return; //Crash when kicked from a server while using the ball. Client-side, so just throw it out.
			}

			player.prevTimeInPortal = player.timeInPortal;
			player.timeInPortal -= 0.0125F;

			if (player.timeInPortal <= 0.0F)
			{
				playPortalAnimation = false;
			}
		}

		if (clientTickCounter <= 0)
		{
			clientTickCounter = Time.SECOND / 2;

			if (MCA.destinySpawnFlag)
			{
				SchematicHandler.spawnStructureRelativeToPoint("/assets/mca/schematic/destiny-test.schematic", MCA.destinyCenterPoint, mc.theWorld);
			}
		}

		else
		{
			clientTickCounter--;
		}
	}

	@SubscribeEvent
	public void serverTickEventHandler(ServerTickEvent event)
	{
		MCA.getPacketHandler().processPackets(Side.SERVER);

		// This block prevents the long-standing issue of crashing while using a world that previously contained villagers.
		// It will check every second for a villager that has not been converted, and see if it should be. These villagers
		// are identified by having the value of 3577 for watched object number 28.
		if (serverTickCounter % 40 == 0)
		{
			for (World world : FMLCommonHandler.instance().getMinecraftServerInstance().worldServers)
			{
				for (int i = 0; i < world.loadedEntityList.size(); i++)
				{
					Object obj = world.loadedEntityList.get(i);

					if (obj instanceof EntityVillager)
					{
						EntityVillager villager = (EntityVillager)obj;

						try
						{
							if (villager.getDataManager().get(Constants.OVERWRITE_KEY) == 3577)
							{
								doOverwriteVillager(villager);
							}
						}

						catch (Exception e)
						{
							continue;
						}
					}
				}
			}
		}

		//Tick down reaper counter.
		if (summonCounter > 0)
		{
			summonCounter--;
			
			//Spawn particles around the summon point.
			Utilities.spawnParticlesAroundPointS(EnumParticleTypes.PORTAL, summonWorld, summonPos.iPosX, summonPos.iPosY, summonPos.iPosZ, 2);
			
			//Lightning will strike periodically.
			if (summonCounter % (Time.SECOND * 2) == 0)
			{
				double dX = summonPos.iPosX + (summonWorld.rand.nextInt(6) * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1));
				double dZ = summonPos.iPosZ + (summonWorld.rand.nextInt(6) * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1));
				double y = (double)RadixLogic.getSpawnSafeTopLevel(summonWorld, (int)dX, (int)dZ);
				NetworkRegistry.TargetPoint lightningTarget = new NetworkRegistry.TargetPoint(summonWorld.provider.getDimension(), dX, y, dZ, 64);
				EntityLightningBolt lightning = new EntityLightningBolt(summonWorld, dX, y, dZ, false);
								
				summonWorld.spawnEntityInWorld(lightning);
				MCA.getPacketHandler().sendPacketToAllAround(new PacketSpawnLightning(new Point3D(dX, y, dZ)), lightningTarget);
				
				//On the first lightning bolt, send the summon sound to all around the summon point.
				if (summonCounter == 80)
				{
					NetworkRegistry.TargetPoint summonTarget = new NetworkRegistry.TargetPoint(summonWorld.provider.getDimension(), summonPos.iPosX, summonPos.iPosY, summonPos.iPosZ, 32);
					//FIXME
					//MCA.getPacketHandler().sendPacketToAllAround(new PacketPlaySoundOnPlayer("mca:reaper.summon"), summonTarget);
				}
			}
			
			if (summonCounter == 0)
			{
				EntityGrimReaper reaper = new EntityGrimReaper(summonWorld);
				reaper.setPosition(summonPos.iPosX, summonPos.iPosY, summonPos.iPosZ);
				summonWorld.spawnEntityInWorld(reaper);
				
				summonPos = null;
				summonWorld = null;
			}
		}

		if (serverTickCounter <= 0 && MCA.getConfig().guardSpawnRate > 0)
		{
			//Build a list of all humans on the server.
			List<EntityVillagerMCA> humans = new ArrayList<EntityVillagerMCA>();

			for (World world : FMLCommonHandler.instance().getMinecraftServerInstance().worldServers)
			{
				for (Object obj : world.loadedEntityList)
				{
					if (obj instanceof EntityVillagerMCA)
					{
						humans.add((EntityVillagerMCA)obj);
					}
				}
			}

			if (!humans.isEmpty())
			{
				//Pick three humans at random to perform guard spawning around.
				for (int i = 0; i < 3; i++)
				{
					EntityVillagerMCA human = humans.get(RadixMath.getNumberInRange(0, humans.size() - 1));

					//Don't count guards in the total count of villagers.
					List<Entity> villagersAroundMe = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityVillagerMCA.class, human, 50);
					int numberOfGuardsAroundMe = getNumberOfGuardsFromEntityList(villagersAroundMe);
					int numberOfVillagersAroundMe = villagersAroundMe.size() - numberOfGuardsAroundMe; 
					int neededNumberOfGuards = numberOfVillagersAroundMe / MCA.getConfig().guardSpawnRate;
					
					if (numberOfGuardsAroundMe < neededNumberOfGuards)
					{
						final EntityVillagerMCA guard = new EntityVillagerMCA(human.worldObj, RadixLogic.getBooleanWithProbability(50), EnumProfession.Guard.getId(), false);
						final Vec3d pos = RandomPositionGenerator.findRandomTarget(human, 10, 1);

						if (pos != null) //Ensure a random position was actually found.
						{
							final Point3D posAsPoint = new Point3D(pos.xCoord, pos.yCoord, pos.zCoord);

							//Check that we can see the sky, no guards in caves or stuck in blocks.
							if (RadixBlocks.canBlockSeeTheSky(human.worldObj, posAsPoint.iPosX, (int)human.posY, posAsPoint.iPosZ))
							{
								guard.setPosition(pos.xCoord, (int)human.posY, pos.zCoord);
								human.worldObj.spawnEntityInWorld(guard);
							}
						}
					}
				}
			}

			serverTickCounter = Time.MINUTE;
		}
		
		if (serverTickCounter <= 0 && MCA.getConfig().replenishEmptyVillages && RadixLogic.getBooleanWithProbability(25))
		{
			for (World world : FMLCommonHandler.instance().getMinecraftServerInstance().worldServers)
			{
				for (Object obj : world.villageCollectionObj.getVillageList())
				{
					Village village = (Village)obj;
					
					int populationCapacity = village.getNumVillageDoors();
					int population = 0;
					double posX = village.getCenter().getX();
					double posY = village.getCenter().getY();
					double posZ = village.getCenter().getZ();
					
					for (Entity entity : RadixLogic.getAllEntitiesWithinDistanceOfCoordinates(world, posX, posY, posZ, village.getVillageRadius()))
					{
						if (entity instanceof EntityVillagerMCA)
						{
							EntityVillagerMCA human = (EntityVillagerMCA) entity;
							
							//Count everyone except guards
							if (human.getProfessionSkinGroup() != EnumProfessionSkinGroup.Guard)
							{
								population++;
							}
							
							//Count babies with the villager population.
							if (human.getBabyState() != EnumBabyState.NONE)
							{
								population++;
							}
						}
					}

					//If the village can support more villagers, spawn.
					int tries = 0;

					if (population < populationCapacity)
					{
						while (tries < 3)
						{
							posX = posX + (world.rand.nextInt(village.getVillageRadius())) * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1);
							posZ = posZ + (world.rand.nextInt(village.getVillageRadius())) * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1);
							
							//Offset to the center of the block
							posX += 0.5D;
							posZ += 0.5D;
							double dY = RadixLogic.getSpawnSafeTopLevel(world, (int)posX, (int)posZ);
							
							//Prevent spawning on roof by checking the safe spawn level against the center level
							//and making sure it's not too high.
							if (dY - posY <= 4.0F)
							{
								Point3D pointOfSpawn = new Point3D(posX, dY, posZ);
								MCA.naturallySpawnVillagers(pointOfSpawn, world, -1);
								break;
							}
							
							else //Try again up to 3 times if not.
							{
								tries++;
							}
						}
					}
				}
			}
		}

		serverTickCounter--;
	}

	@SubscribeEvent
	public void itemCraftedEventHandler(ItemCraftedEvent event)
	{
		Item craftedItem = event.crafting.getItem();
		EntityPlayer player = event.player;

		if (craftedItem == ModItems.diamondHeart || craftedItem == ModItems.diamondOval || craftedItem == ModItems.diamondSquare
				|| craftedItem == ModItems.diamondStar || craftedItem == ModItems.diamondTiny || craftedItem == ModItems.diamondTriangle)
		{
			player.addStat(ModAchievements.craftShapedDiamond);
		}

		else if (craftedItem == ModItems.engagementRingHeart || craftedItem == ModItems.engagementRingOval || craftedItem == ModItems.engagementRingSquare
				|| craftedItem == ModItems.engagementRingStar || craftedItem == ModItems.engagementRingTiny || craftedItem == ModItems.engagementRingTriangle
				|| craftedItem == ModItems.engagementRingHeartRG || craftedItem == ModItems.engagementRingOvalRG || craftedItem == ModItems.engagementRingSquareRG
				|| craftedItem == ModItems.engagementRingStarRG || craftedItem == ModItems.engagementRingTinyRG || craftedItem == ModItems.engagementRingTriangleRG)
		{
			player.addStat(ModAchievements.craftShapedRing);
		}

		//Return damageable items to the inventory.
		for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++)
		{
			ItemStack stack = event.craftMatrix.getStackInSlot(i);

			if (stack != null && (stack.getItem() instanceof ItemGemCutter || stack.getItem() == ModItems.needleAndString))
			{
				stack.attemptDamageItem(1, event.player.getRNG());

				if (stack.getItemDamage() < stack.getMaxDamage())
				{
					event.player.inventory.addItemStackToInventory(stack);
				}
				player.addStat(ModAchievements.craftShapedDiamond);
			}

			break;
		}
	}
	@SubscribeEvent
	public void itemSmeltedEventHandler(ItemSmeltedEvent event)
	{
		Item smeltedItem = event.smelting.getItem();
		EntityPlayer player = event.player;
	}

	private void doOverwriteVillager(EntityVillager entity) 
	{
		entity.setDead();
		MCA.naturallySpawnVillagers(new Point3D(entity.posX, entity.posY, entity.posZ), entity.worldObj, entity.getProfession());
	}

	public static void setReaperSummonPoint(World worldObj, Point3D point)
	{
		summonWorld = worldObj;
		summonPos = point;
		summonCounter = Time.SECOND * 6;
	}
	
	private int getNumberOfGuardsFromEntityList(List<Entity> entityList) 
	{
		int returnValue = 0;
		
		for (Entity entity : entityList)
		{
			if (entity instanceof EntityVillagerMCA)
			{
				EntityVillagerMCA human = (EntityVillagerMCA)entity;
				
				if (human.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard)
				{
					returnValue++;
				}
			}
		}
		
		return returnValue;
	}
}
