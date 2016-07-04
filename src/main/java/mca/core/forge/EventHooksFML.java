package mca.core.forge;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mca.core.MCA;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModItems;
import mca.data.NBTPlayerData;
import mca.data.PlayerData;
import mca.data.PlayerDataCollection;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityHuman;
import mca.enums.EnumBabyState;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionGroup;
import mca.items.ItemGemCutter;
import mca.packets.PacketPlaySoundOnPlayer;
import mca.packets.PacketPlayerDataLogin;
import mca.packets.PacketSpawnLightning;
import mca.packets.PacketSyncConfig;
import mca.util.Utilities;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import radixcore.constant.Particle;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;
import radixcore.util.SchematicHandler;

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
		if (eventArgs.modID.equals(MCA.ID))
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
		//We continue to create the old player data object so any existing player data file is read.
		PlayerDataCollection dataCollection = PlayerDataCollection.get();
		EntityPlayer player = event.player;
		NBTPlayerData nbtData = null;
		PlayerData oldData = new PlayerData(player);
		boolean setPermanentId = false;

		//Check for old data and migrate it into the nbtData object.
		if (oldData.dataExists())
		{
			oldData = oldData.readDataFromFile(event.player, PlayerData.class, null);
			dataCollection.migrateOldPlayerData(player, oldData);
			nbtData = dataCollection.getPlayerData(player.getUniqueID());
		}

		//If no old data, see if this player's data is empty in the world data.
		else if (dataCollection.getPlayerData(player.getUniqueID()) == null)
		{
			//If so, they need a new data object and permanent ID since this is their first login.
			NBTPlayerData nbtPlayerData = new NBTPlayerData();
			dataCollection.putPlayerData(player.getUniqueID(), nbtPlayerData);
			nbtData = nbtPlayerData;
			setPermanentId = true;
		}

		else //If new data is already contained in the data collection, just grab what has already been loaded from disk.
		{
			nbtData = dataCollection.getPlayerData(player.getUniqueID());
		}
		
		//Send copy of the player data to the client.
		if (nbtData != null)
		{
			//Send the object before making any changes.
			MCA.getPacketHandler().sendPacketToPlayer(new PacketPlayerDataLogin(nbtData), (EntityPlayerMP) player);

			//Assign permanent ID if the flag is set.
			if (setPermanentId)
			{
				nbtData.setPermanentId(RadixLogic.generatePermanentEntityId(player));
			}
			
			//Add the crystal ball to the inventory if needed.
			if (!nbtData.getHasChosenDestiny() && !player.inventory.hasItem(ModItems.crystalBall) && MCA.getConfig().giveCrystalBall)
			{
				player.inventory.addItemStackToInventory(new ItemStack(ModItems.crystalBall));
			}
		}
		
		//Sync the server's configuration, for display settings.
		MCA.getPacketHandler().sendPacketToPlayer(new PacketSyncConfig(MCA.getConfig()), (EntityPlayerMP)event.player);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTickEventHandler(ClientTickEvent event)
	{	
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
				return; //Crash when kicked from a server while using the ball. Client-side, so throw it out.
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
		//Tick down reaper counter.
		if (summonCounter > 0)
		{
			summonCounter--;
			
			//Spawn particles around the summon point.
			Utilities.spawnParticlesAroundPointS(Particle.PORTAL, summonWorld, summonPos.iPosX, summonPos.iPosY, summonPos.iPosZ, 2);
			
			//Lightning will strike periodically.
			if (summonCounter % (Time.SECOND * 2) == 0)
			{
				double dX = summonPos.iPosX + (summonWorld.rand.nextInt(6) * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1));
				double dZ = summonPos.iPosZ + (summonWorld.rand.nextInt(6) * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1));
				double y = (double)RadixLogic.getSpawnSafeTopLevel(summonWorld, (int)dX, (int)dZ);
				NetworkRegistry.TargetPoint lightningTarget = new NetworkRegistry.TargetPoint(summonWorld.provider.dimensionId, dX, y, dZ, 64);
				EntityLightningBolt lightning = new EntityLightningBolt(summonWorld, dX, y, dZ);
								
				summonWorld.spawnEntityInWorld(lightning);
				MCA.getPacketHandler().sendPacketToAllAround(new PacketSpawnLightning(new Point3D(dX, y, dZ)), lightningTarget);
				
				//On the first lightning bolt, send the summon sound to all around the summon point.
				if (summonCounter == 80)
				{
					NetworkRegistry.TargetPoint summonTarget = new NetworkRegistry.TargetPoint(summonWorld.provider.dimensionId, summonPos.iPosX, summonPos.iPosY, summonPos.iPosZ, 32);
					MCA.getPacketHandler().sendPacketToAllAround(new PacketPlaySoundOnPlayer("mca:reaper.summon"), summonTarget);
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
			List<EntityHuman> humans = new ArrayList<EntityHuman>();

			for (World world : MinecraftServer.getServer().worldServers)
			{
				for (Object obj : world.loadedEntityList)
				{
					if (obj instanceof EntityHuman)
					{
						humans.add((EntityHuman)obj);
					}
				}
			}

			if (!humans.isEmpty())
			{
				//Pick three humans at random to perform guard spawning around.
				for (int i = 0; i < 3; i++)
				{
					EntityHuman human = humans.get(RadixMath.getNumberInRange(0, humans.size() - 1));

					//Don't count guards in the total count of villagers.
					List<Entity> villagersAroundMe = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityHuman.class, human, 50);
					int numberOfGuardsAroundMe = getNumberOfGuardsFromEntityList(villagersAroundMe);
					int numberOfVillagersAroundMe = villagersAroundMe.size() - numberOfGuardsAroundMe; 
					int neededNumberOfGuards = numberOfVillagersAroundMe / MCA.getConfig().guardSpawnRate;
					
					if (numberOfGuardsAroundMe < neededNumberOfGuards)
					{
						final EntityHuman guard = new EntityHuman(human.worldObj, RadixLogic.getBooleanWithProbability(50), EnumProfession.Guard.getId(), false);
						final Vec3 pos = RandomPositionGenerator.findRandomTarget(human, 10, 1);

						if (pos != null) //Ensure a random position was actually found.
						{
							final Point3D posAsPoint = new Point3D(pos.xCoord, pos.yCoord, pos.zCoord);

							//Check that we can see the sky, no guards in caves or stuck in blocks.
							if (BlockHelper.canBlockSeeTheSky(human.worldObj, posAsPoint.iPosX, (int)human.posY, posAsPoint.iPosZ))
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
			for (World world : MinecraftServer.getServer().worldServers)
			{
				for (Object obj : world.villageCollectionObj.getVillageList())
				{
					Village village = (Village)obj;
					
					int populationCapacity = village.getNumVillageDoors();
					int population = 0;
					double posX = village.getCenter().posX;
					double posY = village.getCenter().posY;
					double posZ = village.getCenter().posZ;
					
					for (Entity entity : RadixLogic.getAllEntitiesWithinDistanceOfCoordinates(world, posX, posY, posZ, village.getVillageRadius()))
					{
						if (entity instanceof EntityHuman)
						{
							EntityHuman human = (EntityHuman) entity;
							
							//Count everyone except guards
							if (human.getProfessionGroup() != EnumProfessionGroup.Guard)
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

		if (craftedItem == ModItems.diamondDust)
		{
			player.triggerAchievement(ModAchievements.craftDiamondDust);
		}

		else if (craftedItem == ModItems.coloredDiamondDust)
		{
			player.triggerAchievement(ModAchievements.craftColoredDiamondDust);
		}

		else if (craftedItem == ModItems.diamondHeart || craftedItem == ModItems.diamondOval || craftedItem == ModItems.diamondSquare
				|| craftedItem == ModItems.diamondStar || craftedItem == ModItems.diamondTiny || craftedItem == ModItems.diamondTriangle
				|| craftedItem == ModItems.coloredDiamondHeart || craftedItem == ModItems.coloredDiamondOval || craftedItem == ModItems.coloredDiamondSquare
				|| craftedItem == ModItems.coloredDiamondStar || craftedItem == ModItems.coloredDiamondTiny || craftedItem == ModItems.coloredDiamondTriangle)
		{
			player.triggerAchievement(ModAchievements.craftShapedDiamond);
		}

		else if (craftedItem == ModItems.engagementRingHeart || craftedItem == ModItems.engagementRingOval || craftedItem == ModItems.engagementRingSquare
				|| craftedItem == ModItems.engagementRingStar || craftedItem == ModItems.engagementRingTiny || craftedItem == ModItems.engagementRingTriangle
				|| craftedItem == ModItems.ringHeartColored || craftedItem == ModItems.ringOvalColored || craftedItem == ModItems.ringSquareColored
				|| craftedItem == ModItems.ringStarColored || craftedItem == ModItems.ringTinyColored || craftedItem == ModItems.ringTriangleColored
				|| craftedItem == ModItems.engagementRingHeartRG || craftedItem == ModItems.engagementRingOvalRG || craftedItem == ModItems.engagementRingSquareRG
				|| craftedItem == ModItems.engagementRingStarRG || craftedItem == ModItems.engagementRingTinyRG || craftedItem == ModItems.engagementRingTriangleRG
				|| craftedItem == ModItems.ringHeartColoredRG || craftedItem == ModItems.ringOvalColoredRG || craftedItem == ModItems.ringSquareColoredRG
				|| craftedItem == ModItems.ringStarColoredRG || craftedItem == ModItems.ringTinyColoredRG || craftedItem == ModItems.ringTriangleColoredRG)
		{
			player.triggerAchievement(ModAchievements.craftShapedRing);
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

				break;
			}
		}
	}

	@SubscribeEvent
	public void itemSmeltedEventHandler(ItemSmeltedEvent event)
	{
		Item smeltedItem = event.smelting.getItem();
		EntityPlayer player = event.player;

		if (smeltedItem == ModItems.coloredDiamond)
		{
			player.triggerAchievement(ModAchievements.smeltColoredDiamond);
		}
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
			if (entity instanceof EntityHuman)
			{
				EntityHuman human = (EntityHuman)entity;
				
				if (human.getProfessionGroup() == EnumProfessionGroup.Guard)
				{
					returnValue++;
				}
			}
		}
		
		return returnValue;
	}
}
