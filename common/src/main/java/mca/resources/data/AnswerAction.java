package mca.resources.data;

import net.minecraft.util.Identifier;

public class AnswerAction {
    private final float threshold;
    private final String id;

    public AnswerAction(float threshold, String name) {
        this.threshold = threshold;
        this.id = name;
    }

    public float getThreshold() {
        return threshold;
    }

    public String getId() {
        return id;
    }
}
