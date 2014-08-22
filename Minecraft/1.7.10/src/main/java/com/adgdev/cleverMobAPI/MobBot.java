package com.adgdev.cleverMobAPI;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

public class MobBot 
{
    public static ChatterBotFactory factory = new ChatterBotFactory();
	
    public static ChatterBot bot;
    public static ChatterBotSession botSession;
    
    public void init(ChatterBotType type, String pandoraID)
    {
    	if(pandoraID.isEmpty() || pandoraID == null)
    	{
        	bot = factory.create(type);
        	botSession = bot.createSession();
    	}
    	else
    	{
        	bot = factory.create(type, pandoraID);
        	botSession = bot.createSession();
    	}
    }
    
    public String getRespond(String input)
    {
    	try {
			return botSession.think(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
		return "Can't connect to the CleverMob API, please fix the problems.";
    }
}
