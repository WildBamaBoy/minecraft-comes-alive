package com.google.code.chatterbotapi;

public class ChatterBotFactory {

    public ChatterBot create(ChatterBotType type) {
        return create(type, null);
    }

    public ChatterBot create(ChatterBotType type, Object arg){
    	try
    	{
            switch (type) {
            case CLEVERBOT:
                return new Cleverbot("http://www.cleverbot.com/webservicemin", 35);
            case JABBERWACKY:
                return new Cleverbot("http://jabberwacky.com/webservicemin", 29);
            case PANDORABOTS:
                if (arg == null) {
                    throw new Exception("PANDORABOTS needs a botid arg");
                }
                return new PandoraBots(arg.toString());
            }
            return null;
    	}
    	catch(Exception ex)
    	{
    		try {
				throw ex;
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
		return null;
    }
}