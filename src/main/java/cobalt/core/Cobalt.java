package cobalt.core;

import cobalt.mod.forge.CobaltForgeMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Cobalt extends CobaltForgeMod {
    private static Logger logger = LogManager.getLogger("Cobalt");

    public static Logger getLog() {
        return logger;
    }

    public String getModId() {
        return "cobalt";
    }

    @Override
    public void registerContent() {

    }

    @Override
    public void onSetup() {
        logger.info("Hello from Cobalt!");
    }

    @Override
    public void onClientSetup() {}

    @Override
    public void loadRegistries() {

    }
}
