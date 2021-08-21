package mca.resources.data;

public class AnswerAction {
    private final float threshold;
    private final String name;

    public AnswerAction(float threshold, String name) {
        this.threshold = threshold;
        this.name = name;
    }

    public float getThreshold() {
        return threshold;
    }

    public String getName() {
        return name;
    }
}
