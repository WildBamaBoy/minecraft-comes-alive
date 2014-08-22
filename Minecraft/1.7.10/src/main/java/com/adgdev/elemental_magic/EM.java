package com.adgdev.elemental_magic;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.adgdev.cleverMobAPI.MobBot;
import com.adgdev.elemental_magic.content.RaceHandler;
import com.adgdev.elemental_magic.content.RaceSubitemClass;
import com.adgdev.elemental_magic.content.TabEM;
import com.adgdev.elemental_magic.content.blocks.IceBlock;
import com.adgdev.elemental_magic.content.commands.CommandTalk;
import com.google.code.chatterbotapi.ChatterBotType;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = EM.MODID, version = EM.VERSION)
public class EM
{
    public static final String MODID = "elemental_magic";
    public static final String VERSION = "0.1a";
    
    public static CreativeTabs tabEM = new TabEM("Elemental Magic");
    
    public static Block iceBlock;
    
    public static RaceSubitemClass raceSubItem;
    public static RaceHandler rHandler = new RaceHandler("ice");
    
    int playerX;
    int playerY;
    int playerZ;
    
    EntityPlayer entityPlayer = null;
    
    @Instance("elemental_magic") //The instance, this is very important later on
    public static EM instance = new EM();

    @SidedProxy(clientSide = "com.adgdev.elemental_magic.EMCommonProxy", serverSide = "com.adgdev.elemental_magic.EMCommonProxy") //Tells Forge the location of your proxies
    public static EMCommonProxy proxy;
    
    //public static ChatterBotSession clever = new Cleverbot("http://www.cleverbot.com/webservicemin").createSession();
    
    public static MobBot bot = new MobBot();
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	FMLCommonHandler.instance().bus().register(this);
    	
    	bot.init(ChatterBotType.CLEVERBOT, "");
    	
    	raceSubItem = new RaceSubitemClass("ice");
    	
    	iceBlock = new IceBlock().setBlockName("iceBlock");
    	GameRegistry.registerBlock(iceBlock, iceBlock.getUnlocalizedName().substring(5));
    	
    	MinecraftForge.EVENT_BUS.register(rHandler);
    }
    
    @EventHandler
    public void serverStart(FMLServerStartingEvent event)
    {
         MinecraftServer server = MinecraftServer.getServer();
         
         ICommandManager command = server.getCommandManager();
         ServerCommandManager manager = (ServerCommandManager) command;
         
         manager.registerCommand(new CommandTalk());
    }
    
    @SubscribeEvent
    public void tickPlayer(PlayerTickEvent event) 
    {
    	playerX = (int) event.player.posX;
    	playerY = (int) event.player.posY;
    	playerZ = (int) event.player.posZ;
    	
    	entityPlayer = event.player;
    }
    
    @SubscribeEvent
    public void tickWorld(WorldTickEvent event) 
    {
    	//if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
    	//{
    	
    	boolean leftButtonDown = Mouse.isButtonDown(0); // is left mouse button down.
    	boolean rightButtonDown = Mouse.isButtonDown(1); // is right mouse button down.
    	
    	if(Keyboard.isKeyDown(Keyboard.KEY_F))
    	{
    		RaceSubitemClass item = null;
    		
    		if(rightButtonDown)
        	{    			
    			if(ItemStack.areItemStacksEqual(EM.raceSubItem.is, entityPlayer.inventory.getStackInSlot(9)))
        		{
    				item = (RaceSubitemClass) entityPlayer.inventory.getStackInSlot(9).getItem();
    				
    				if(!item.nbt.getString("race").isEmpty() && item.nbt.getString("race").equals("fire"))
    				{	
    	    			if (!event.world.isRemote)
    	        		{
    	            		Vec3 look = entityPlayer.getLookVec();
    	            		
    	            		int numFireBalls = 2;
    	            		
    	            		EntityLargeFireball fireBalls[] = new EntityLargeFireball[numFireBalls];
    	            		
    	            		for(int i = 0; i < numFireBalls; i++)
    	            		{
    	                		for(int j = 0; j < numFireBalls; j++)
    	                		{
    	                			fireBalls[i] = new EntityLargeFireball(event.world, entityPlayer, 1, 1, 1);
    	                			
    	                			fireBalls[i].setPosition(entityPlayer.posX + look.xCoord * 5 + i, entityPlayer.posY + look.yCoord * 5 + j, entityPlayer.posZ + look.zCoord * 5 - i);
    	                    		
    	                			fireBalls[i].accelerationX = (look.xCoord * 0.1);
    	                			fireBalls[i].accelerationY = (look.yCoord * 0.1);
    	                			fireBalls[i].accelerationZ = (look.zCoord * 0.1);
    	                			
    	                			event.world.spawnEntityInWorld(fireBalls[i]);
    	                		}
    	            		}
    	        		}
    				}
        		}
        	}
    		
    		if(ItemStack.areItemStacksEqual(EM.raceSubItem.is, entityPlayer.inventory.getStackInSlot(9)))
    		{
    			item = (RaceSubitemClass) entityPlayer.inventory.getStackInSlot(9).getItem();	

    	    	if(!item.nbt.getString("race").isEmpty() && item.nbt.getString("race").equals("ice"))
    	    	{        	
    				for(int i = 0; i <= 30; i++){
    					entityPlayer.worldObj.spawnParticle("snowshovel", playerX, playerY + 0.5f, playerZ, 0, 0.5, 0);		
    				}
    	    		
    	    		float r = 10;
    				
    	    		for(int x = 0; x < r; x++)
    	    		{
    	    			for(int z = 0; z < r; z++)
    	    			{
    						if (Math.sqrt((float)(x - (r / 2))*(x - (r / 2)) + (z - (r / 2))*(z - (r / 2))) <= r / 2)
    						{
    							int x2 = x - (int)(r / 2);
    							int z2 = z - (int)(r / 2);
    							
    							if(event.world.getBlock(playerX + x2, playerY - 1, playerZ + z2) == Block.getBlockById(8) || event.world.getBlock(playerX + x2, playerY - 1, playerZ + z2) == Block.getBlockById(9))
    							{
    								event.world.setBlock(playerX + x2, playerY - 1, playerZ + z2, EM.iceBlock);
    							}
    						}
    	    			}
    	    		}
    	    	}
    		}
		}
    }
}
