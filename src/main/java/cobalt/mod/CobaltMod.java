package cobalt.mod;

import cobalt.localizer.Localizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Basic Cobalt mod which serves as the outline for a core mod class.
 */
public abstract class CobaltMod {
    protected Logger logger;
    protected Localizer localizer;

    protected long startupTimestamp;

    public CobaltMod() {
        logger = LogManager.getLogger();
        localizer = new Localizer();
    }

    /*
        Abstract events for mods to implement
     */
    public abstract String getModId();
    public abstract void onSetup();
    public abstract void onClientSetup();
    public abstract void registerContent();
}
