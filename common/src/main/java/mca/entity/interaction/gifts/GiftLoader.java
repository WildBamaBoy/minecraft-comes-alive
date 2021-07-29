package mca.entity.interaction.gifts;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import mca.MCA;
import mca.resources.Resources;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class GiftLoader extends JsonDataLoader {
    protected static final Identifier ID = new Identifier(MCA.MOD_ID, "gifts");

    public GiftLoader() {
        super(Resources.GSON, "gifts");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        GiftType.REGISTRY.clear();
        data.forEach((id, json) -> {
            try {
                GiftType.REGISTRY.add(GiftType.fromJson(id, JsonHelper.asObject(json, "root")));
            } catch (JsonParseException e) {
                MCA.LOGGER.error("Could not load gift type for id {}", id, e);
            }
        });
    }
}
