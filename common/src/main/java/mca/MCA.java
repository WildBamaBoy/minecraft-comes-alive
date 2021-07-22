package mca;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MCA {
    public static final String MOD_ID = "mca";
    public static final Logger LOGGER = LogManager.getLogger();

    public static Config getConfig() {
        return Config.getInstance();
    }
}

