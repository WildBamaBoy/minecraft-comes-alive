package mca.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

@AllArgsConstructor
@Getter
public class Gift {
    private String type;
    private String name;
    private int value;

    /**
     * Used for verifying if a given gift exists in the game's registries.
     * @return True if the item/block exists.
     */
    public boolean exists() {
        if (getType().equals("block")) {
            return Block.getBlockFromName(getName()) != null;
        } else if (getType().equals("item")) {
            return Item.getByNameOrId(getName()) != null;
        } else {
            MCA.getLog().warn("Could not process gift '" + getName() + "'- bad type name of '" + getType() + "'. Must be 'item' or 'block'");
            return false;
        }
    }
}
