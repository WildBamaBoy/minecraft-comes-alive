package mca.resources.data.tasks;

import com.google.gson.JsonObject;
import java.io.Serializable;
import mca.server.world.data.Village;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.JsonHelper;

public abstract class Task implements Serializable {
    private static final long serialVersionUID = 6029812512760976500L;

    private final String id;

    public Task(JsonObject json) {
        this(JsonHelper.getString(json, "id"));
    }

    public Task(String id) {
        this.id = id;
    }

    abstract public boolean isCompleted(Village village, ServerPlayerEntity player);

    public boolean isRequired() {
        return false;
    }

    public TranslatableText getTranslatable() {
        return new TranslatableText("task." + id);
    }

    public String getId() {
        return id;
    }
}
