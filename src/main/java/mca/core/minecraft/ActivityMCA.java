package mca.core.minecraft;

import mca.core.MCA;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.util.Identifier;

public interface ActivityMCA {
    Activity CHORE = register("chore");

    static void bootstrap() { }

    private static Activity register(String name) {
        return Activity.register(new Identifier(MCA.MOD_ID, name).toString());
    }
}
