package mca.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Gift {
    private final String type;
    private final String name;
    private final int value;

    /**
     * Used for verifying if a given gift exists in the game's registries.
     *
     * @return True if the item/block exists.
     */
    public boolean exists() {
        //TODO
        return true;
    }
}
