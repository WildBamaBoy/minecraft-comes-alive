package mca.core.forge;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mca.core.MCA;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModItems;
import mca.data.PlayerData;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityHuman;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionGroup;
import mca.items.ItemGemCutter;
import mca.packets.PacketPlaySoundOnPlayer;
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
import net.minecraft.world.World;
import radixcore.constant.Particle;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.packets.PacketDataContainer;
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
		EntityPlayer player = event.player;
		PlayerData data = null;

		if (!MCA.playerDataMap.containsKey(player.getUniqueID().toString()))
		{
			data = new PlayerData(player);

			if (data.dataExists())
			{
				data = data.readDataFromFile(event.player, PlayerData.class, null);
			}

			else
			{
				data.initializeNewData(event.player);
			}

			MCA.playerDataMap.put(event.player.getUniqueID().toString(), data);
		}

		else
		{
			data = MCA.getPlayerData(player);
			data = data.readDataFromFile(event.player, PlayerData.class, null);  //Read from the file again to assign owner.
			MCA.playerDataMap.put(event.player.getUniqueID().toString(), data);  //Put updated data back into the map.
		}

		MCA.getPacketHandler().sendPacketToPlayer(new PacketDataContainer(MCA.ID, data), (EntityPlayerMP)event.player);
		MCA.getPacketHandler().sendPacketToPlayer(new PacketSyncConfig(MCA.getConfig()), (EntityPlayerMP)event.player);

		if (!data.getHasChosenDestiny() && !player.inventory.hasItem(ModItems.crystalBall) && MCA.getConfig().giveCrystalBall)
		{
			player.inventory.addItemStackToInventory(new ItemStack(ModItems.crystalBall));
		}
	}

	@SubscribeEvent
	public void playerLoggedOutEventHandler(PlayerLoggedOutEvent event)
	{
		PlayerData data = MCA.getPlayerData(event.player);

		if (data != null)
		{
			data.saveDataToFile();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTickEventHandler(ClientTickEvent event)
	{	
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
		net.minecraft.client.gui.GuiScreen currentScreen = mc.currentScreen;

		if (currentScreen instanceof net.minecraft.client.gui.GuiMainMenu && MCA.playerDataContainer != null)
		{
			playPortalAnimation = false;
			MCA.destinyCenterPoint = null;
			MCA.destinySpawnFlag = false;
			MCA.playerDataContainer = null;
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
