package mca.enums;

import mca.api.API;
import mca.core.MCA;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.math.MathHelper;

public enum EnumMood {

    DEPRESSED(RangedInteger.of(-100, -15), EnumMoodGroup.GENERAL),
    SAD(RangedInteger.of(-14, -7), EnumMoodGroup.GENERAL),
    UNHAPPY(RangedInteger.of(-6, -1), EnumMoodGroup.GENERAL),
    PASSIVE(RangedInteger.of(0, 0), EnumMoodGroup.UNASSIGNED),
    FINE(RangedInteger.of(1, 6), EnumMoodGroup.GENERAL),
    HAPPY(RangedInteger.of(7, 14), EnumMoodGroup.GENERAL),
    OVERJOYED(RangedInteger.of(15, 15), EnumMoodGroup.GENERAL),

    BORED_TO_TEARS(RangedInteger.of(-100, -15), EnumMoodGroup.PLAYFUL),
    BORED(RangedInteger.of(-14, -7), EnumMoodGroup.PLAYFUL),
    UNINTERESTED(RangedInteger.of(-6, -1), EnumMoodGroup.PLAYFUL),
    SILLY(RangedInteger.of(1, 6), EnumMoodGroup.PLAYFUL),
    GIGGLY(RangedInteger.of(7, 14), EnumMoodGroup.PLAYFUL),
    ENTERTAINED(RangedInteger.of(15, 15), EnumMoodGroup.PLAYFUL),

    INFURIATED(RangedInteger.of(-100, -15), EnumMoodGroup.SERIOUS),
    ANGRY(RangedInteger.of(-14, -7), EnumMoodGroup.SERIOUS),
    ANNOYED(RangedInteger.of(-6, -1), EnumMoodGroup.SERIOUS),
    INTERESTED(RangedInteger.of(1, 6), EnumMoodGroup.SERIOUS),
    TALKATIVE(RangedInteger.of(7, 14), EnumMoodGroup.SERIOUS),
    PLEASED(RangedInteger.of(15, 15), EnumMoodGroup.SERIOUS);

    public final static int minLevel = -100;
    public final static int maxLevel = 15;
    private final RangedInteger level;
    private final EnumMoodGroup moodGroup;

    EnumMood(RangedInteger level, EnumMoodGroup moodGroup) {
        this.level = level;
        this.moodGroup = moodGroup;
    }

    public static int getLevel(int mood) {
        return MathHelper.clamp(mood, minLevel, maxLevel);
    }

    public EnumMoodGroup getMoodGroup() {
        return this.moodGroup;
    }

    public boolean isInRange(int level) {
        return this.level.getMaxInclusive() >= level && this.level.getMinInclusive() <= level;
    }

    public String getLocalizedName() {
        String name = "mood." + this.name().toLowerCase();
        return MCA.localize(name);
    }

    public int getSuccessModifierForInteraction(EnumInteraction interaction) {
        //no need for custom values
        return getHeartsModifierForInteraction(interaction);
    }

    public int getHeartsModifierForInteraction(EnumInteraction interaction) {
        //sad people need a hug
        // TODO adapt values
        switch (interaction) {
            case CHAT:
            case JOKE:
            case TELL_STORY:
            case HUG:
                return level.randomValue(API.getRng());
            case SHAKE_HAND:
            case FLIRT:
            case KISS:
                return -level.randomValue(API.getRng());
            default:
                return 0;
        }
    }
}
