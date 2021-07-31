package mca.resources.data;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;

public final class BuildingType {

    private final String name;
    private final int size;
    private final String color;
    private final int priority;
    private final boolean visible;
    private final Map<String, Integer> blocks;
    private final boolean icon;
    private final int iconU;
    private final int iconV;
    private final boolean grouped;
    private final int mergeRange;

    public BuildingType() {
        this("?", 0, "ffffffff", 0, true, new HashMap<>());
    }

    public BuildingType(String name, int size, String color, int priority, boolean visible, Map<String, Integer> blocks) {
        this.name = name;
        this.size = size;
        this.color = color;
        this.priority = priority;
        this.visible = visible;
        this.blocks = blocks;
        this.icon = false;
        this.iconU = 0;
        this.iconV = 0;
        this.grouped = false;
        this.mergeRange = 32;
    }

    public String name() {
        return name;
    }

    public int size() {
        return size;
    }

    public String color() {
        return color;
    }

    public int priority() {
        return priority;
    }

    public boolean visible() {
        return visible;
    }

    public Map<String, Integer> blocks() {
        return blocks;
    }

    public int getColor() {
        return (int)Long.parseLong(color, 16);
    }

    public boolean requiresBlock(Block block) {
        TagGroup<Block> blocks = ServerTagManagerHolder.getTagManager().getBlocks();
        for (String id : blocks().keySet()) {
            Tag<Block> tag = blocks.getTag(new Identifier(id));
            if (tag != null && tag.contains(block)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIcon() {
        return icon;
    }

    public int iconU() {
        return iconU;
    }

    public int iconV() {
        return iconV;
    }

    public boolean grouped() {
        return grouped;
    }

    public int mergeRange() {
        return mergeRange;
    }
}
