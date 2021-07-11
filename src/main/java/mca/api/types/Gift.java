package mca.api.types;

public record Gift (String type, String name, int value) {
    @Deprecated
    public String getType() {
        return type;
    }
    @Deprecated
    public String getName() {
        return name;
    }
    @Deprecated
    public int getValue() {
        return value;
    }
    /**
     * Used for verifying if a given gift exists in the game's registries.
     *
     * @return True if the item/block exists.
     */
    public boolean exists() {
        //TODO Check for registration
        return true;
    }
}
