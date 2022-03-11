package mca.resources.data;

public class AnswerAction {
    private final float threshold;
    private final String id;
    private final String command;
    private final boolean success;
    private final boolean fail;

    public AnswerAction(float threshold, String name, String command, boolean success, boolean fail) {
        this.threshold = threshold;
        this.id = name;
        this.command = command;
        this.success = success;
        this.fail = fail;
    }

    public float getThreshold() {
        return threshold;
    }

    public String getId() {
        return id;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFail() {
        return fail;
    }

    public String getCommand() {
        return command;
    }
}
