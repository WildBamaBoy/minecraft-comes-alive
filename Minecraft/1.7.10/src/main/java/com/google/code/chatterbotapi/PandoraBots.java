package com.google.code.chatterbotapi;

import java.rmi.server.UID;
import java.util.LinkedHashMap;
import java.util.Map;

public class PandoraBots implements ChatterBot {
    private final String botid;

    public PandoraBots(String botid) {
        this.botid = botid;
    }

    @Override
    public ChatterBotSession createSession() {
        return new Session();
    }
    
    private class Session implements ChatterBotSession {
        private final Map<String, String> vars;

        public Session() {
            vars = new LinkedHashMap<String, String>();
            vars.put("botid", botid);
            vars.put("custid", new UID().toString());
        }
        
        @Override
        public ChatterBotThought think(ChatterBotThought thought) throws Exception {
            vars.put("input", thought.getText());
            
            String response = Utils.post("http://www.pandorabots.com/pandora/talk-xml", vars);
            
            ChatterBotThought responseThought = new ChatterBotThought();
            
            responseThought.setText(Utils.xPathSearch(response, "//result/that/text()"));
            
            return responseThought;
        }

        @Override
        public String think(String text) throws Exception {
            ChatterBotThought thought = new ChatterBotThought();
            thought.setText(text);
            return think(thought).getText();
        }
    }
}