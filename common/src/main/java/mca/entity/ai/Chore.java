package mca.entity.ai;

import java.util.Locale;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

public enum Chore {
    NONE    ("none", null),
    PROSPECT("prospecting", PickaxeItem.class),
    HARVEST ("harvesting", HoeItem.class),
    CHOP    ("chopping", AxeItem.class),
    HUNT    ("hunting", SwordItem.class),
    FISH    ("fishing", FishingRodItem.class);

    private static final Chore[] VALUES = values();
    private static final Map<String, Chore> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(
            c -> c.friendlyName,
            Function.identity())
    );

    protected final String friendlyName;

    @Nullable
    private final Class<?> toolType;

    Chore(String friendlyName, @Nullable Class<?> toolType) {
        this.friendlyName = friendlyName;
        this.toolType = toolType;
    }

    public Text getName() {
        return new TranslatableText("gui.label." + friendlyName);
    }

    @Nullable
    public Class<?> getToolType() {
        return toolType;
    }

    public static Optional<Chore> byCommand(String action) {
        return Optional.ofNullable(REGISTRY.get(action.toLowerCase(Locale.ENGLISH)));
    }

    public static Chore byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return NONE;
        }
        return VALUES[id];
    }
}

