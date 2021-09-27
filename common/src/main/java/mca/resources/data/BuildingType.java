package mca.resources.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class BuildingType {

    private final String name;
    private final int size;
    private final String color;
    private final int priority;
    private final boolean visible;
    private final Map<String, Integer> blocks;
    private Map<Identifier, Integer> blockIds;
    private final boolean icon;
    private final int iconU;
    private final int iconV;
    private final boolean grouped;
    private final int mergeRange;
    private final boolean noBeds;

    public BuildingType() {
        this("?", 0, "ffffffff", 0, true, false);
    }

    public BuildingType(String name, int size, String color, int priority, boolean visible, boolean noBeds) {
        this.name = name;
        this.size = size;
        this.color = color;
        this.priority = priority;
        this.visible = visible;
        this.noBeds = noBeds;
        this.blocks = Collections.emptyMap();
        this.blockIds = null;
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

    public int getColor() {
        return (int)Long.parseLong(color, 16);
    }

    public Map<Identifier, Integer> blockIds() {
        if (blockIds == null) {
            blockIds = new HashMap<>();
            for (Map.Entry<String, Integer> id : blocks.entrySet()) {
                blockIds.put(new Identifier(id.getKey()), id.getValue());
            }
        }
        return blockIds;
    }

    public Set<Block> getBlockIds() {
        Set<Block> b = new HashSet<>();
        TagGroup<Block> blocks = ServerTagManagerHolder.getTagManager().getBlocks();
        for (Identifier id : blockIds().keySet()) {
            Tag<Block> tag = blocks.getTag(id);
            if (tag == null) {
                if (Registry.BLOCK.containsId(id)) {
                    b.add(Registry.BLOCK.get(id));
                }
            } else {
                b.addAll(tag.values());
            }
        }
        return b;
    }

    public Optional<Identifier> getGroup(Block b) {
        return getGroup(Registry.BLOCK.getId(b));
    }

    public Optional<Identifier> getGroup(Identifier b) {
        //look for id match
        if (blockIds().containsKey(b)) {
            return Optional.ofNullable(b);
        }

        //look for matching tag
        TagGroup<Block> blocks = ServerTagManagerHolder.getTagManager().getBlocks();
        for (Identifier id : blockIds().keySet()) {
            Tag<Block> tag = blocks.getTag(id);
            if (tag != null && tag.contains(Registry.BLOCK.get(b))) {
                return Optional.of(id);
            }
        }

        return Optional.empty();
    }

    public boolean isIcon() {
        return icon;
    }

    public int iconU() {
        return iconU * 20;
    }

    public int iconV() {
        return iconV * 60;
    }

    public boolean grouped() {
        return grouped;
    }

    public int mergeRange() {
        return mergeRange;
    }

    public boolean noBeds() {
        return noBeds;
    }
}
