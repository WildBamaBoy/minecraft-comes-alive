package mca.enums;

import mca.api.API;
import mca.core.MCA;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.math.MathHelper;

public enum Mood {

    DEPRESSED(RangedInteger.of(-100, -15), MoodGroup.GENERAL),
    SAD(RangedInteger.of(-14, -7), MoodGroup.GENERAL),
    UNHAPPY(RangedInteger.of(-6, -1), MoodGroup.GENERAL),
    PASSIVE(RangedInteger.of(0, 0), MoodGroup.UNASSIGNED),
    FINE(RangedInteger.of(1, 6), MoodGroup.GENERAL),
    HAPPY(RangedInteger.of(7, 14), MoodGroup.GENERAL),
    OVERJOYED(RangedInteger.of(15, 15), MoodGroup.GENERAL),

    BORED_TO_TEARS(RangedInteger.of(-100, -15), MoodGroup.PLAYFUL),
    BORED(RangedInteger.of(-14, -7), MoodGroup.PLAYFUL),
    UNINTERESTED(RangedInteger.of(-6, -1), MoodGroup.PLAYFUL),
    SILLY(RangedInteger.of(1, 6), MoodGroup.PLAYFUL),
    GIGGLY(RangedInteger.of(7, 14), MoodGroup.PLAYFUL),
    ENTERTAINED(RangedInteger.of(15, 15), MoodGroup.PLAYFUL),

    INFURIATED(RangedInteger.of(-100, -15), MoodGroup.SERIOUS),
    ANGRY(RangedInteger.of(-14, -7), MoodGroup.SERIOUS),
    ANNOYED(RangedInteger.of(-6, -1), MoodGroup.SERIOUS),
    INTERESTED(RangedInteger.of(1, 6), MoodGroup.SERIOUS),
    TALKATIVE(RangedInteger.of(7, 14), MoodGroup.SERIOUS),
    PLEASED(RangedInteger.of(15, 15), MoodGroup.SERIOUS);

    //-15 to 15 is a range of normal interactions, but mood can go -15 to -100 due to player interactions.
    public final static int normalMinLevel = -15;
    public final static int absoluteMinLevel = -100;
    public final static int maxLevel = 15;
    private final RangedInteger level;
    private final MoodGroup moodGroup;

    Mood(RangedInteger level, MoodGroup moodGroup) {
        this.level = level;
        this.moodGroup = moodGroup;
    }

    public static int getLevel(int mood) {
        return MathHelper.clamp(mood, absoluteMinLevel, maxLevel);
    }

    public MoodGroup getMoodGroup() {
        return this.moodGroup;
    }

    public boolean isInRange(int level) {
        return this.level.getMaxInclusive() >= level && this.level.getMinInclusive() <= level;
    }

    public String getLocalizedName() {
        String name = "mood." + this.name().toLowerCase();
        return MCA.localize(name);
    }

    public int getSuccessModifierForInteraction(Interaction interaction) {
        //no need for custom values
        return getHeartsModifierForInteraction(interaction);
    }

    public int getHeartsModifierForInteraction(Interaction interaction) {
        switch (interaction) {
            case CHAT:
            case JOKE:
            case TELL_STORY:
            case HUG:
                return level.randomValue(API.getRng()) / 20;
            case SHAKE_HAND:
            case FLIRT:
            case KISS:
                return -level.randomValue(API.getRng()) / 20;
            default:
                return 0;
        }
    }
}
