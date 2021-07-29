package mca.entity.interaction.gifts;

public enum Response {
    FAIL,
    GOOD,
    BETTER,
    BEST;

    public String getDefaultDialogue() {
        return "gift." + name().toLowerCase();
    }
}
