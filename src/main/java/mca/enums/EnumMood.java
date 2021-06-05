package mca.enums;

import mca.core.MCA;
import net.minecraft.util.math.MathHelper;

public enum EnumMood {
    DEPRESSED(-15, EnumMoodGroup.GENERAL),
    SAD(-10, EnumMoodGroup.GENERAL),
    UNHAPPY(-5, EnumMoodGroup.GENERAL),
    PASSIVE(0, EnumMoodGroup.UNASSIGNED),
    FINE(5, EnumMoodGroup.GENERAL),
    HAPPY(10, EnumMoodGroup.GENERAL),
    OVERJOYED(15, EnumMoodGroup.GENERAL),

    BORED_TO_TEARS(-15, EnumMoodGroup.PLAYFUL),
    BORED(-10, EnumMoodGroup.PLAYFUL),
    UNINTERESTED(-5, EnumMoodGroup.PLAYFUL),
    SILLY(5, EnumMoodGroup.PLAYFUL),
    GIGGLY(10, EnumMoodGroup.PLAYFUL),
    ENTERTAINED(15, EnumMoodGroup.PLAYFUL),

    INFURIATED(-5, EnumMoodGroup.SERIOUS),
    ANGRY(-10, EnumMoodGroup.SERIOUS),
    ANNOYED(-15, EnumMoodGroup.SERIOUS),
    INTERESTED(5, EnumMoodGroup.SERIOUS),
    TALKATIVE(10, EnumMoodGroup.SERIOUS),
    PLEASED(15, EnumMoodGroup.SERIOUS);

    public final static int minLevel = -100;
    public final static int maxLevel = 15;
    public final static int levelsPerMood = 5;
    private final int level;
    private final EnumMoodGroup moodGroup;

    EnumMood(int level, EnumMoodGroup moodGroup) {
        this.level = level;
        this.moodGroup = moodGroup;
    }

    public static int getLevel(int mood) {
        return MathHelper.clamp(mood, minLevel, maxLevel);
    }

    public EnumMoodGroup getMoodGroup() {
        return this.moodGroup;
    }

    public int getLevel() {
        return this.level;
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
        switch (interaction) {
            case CHAT:
            case JOKE:
            case TELL_STORY:
            case HUG:
                return level;
            case SHAKE_HAND:
            case FLIRT:
            case KISS:
                return -level;
            default:
                return 0;
        }
    }
}
