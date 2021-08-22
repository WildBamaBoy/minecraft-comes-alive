package mca.resources.data;

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
