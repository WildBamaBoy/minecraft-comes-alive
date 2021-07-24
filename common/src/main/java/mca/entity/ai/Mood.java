package mca.entity.ai;

import mca.resources.API;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.IntRange;
import net.minecraft.util.math.MathHelper;

public enum Mood {
    DEPRESSED(-100, -15, MoodGroup.GENERAL),
    SAD(-14, -7, MoodGroup.GENERAL),
    UNHAPPY(-6, -1, MoodGroup.GENERAL),
    PASSIVE(0, 0, MoodGroup.UNASSIGNED),
    FINE(1, 6, MoodGroup.GENERAL),
    HAPPY(7, 14, MoodGroup.GENERAL),
    OVERJOYED(15, 15, MoodGroup.GENERAL),

    BORED_TO_TEARS(-100, -15, MoodGroup.PLAYFUL),
    BORED(-14, -7, MoodGroup.PLAYFUL),
    UNINTERESTED(-6, -1, MoodGroup.PLAYFUL),
    SILLY(1, 6, MoodGroup.PLAYFUL),
    GIGGLY(7, 14, MoodGroup.PLAYFUL),
    ENTERTAINED(15, 15, MoodGroup.PLAYFUL),

    INFURIATED(-100, -15, MoodGroup.SERIOUS),
    ANGRY(-14, -7, MoodGroup.SERIOUS),
    ANNOYED(-6, -1, MoodGroup.SERIOUS),
    INTERESTED(1, 6, MoodGroup.SERIOUS),
    TALKATIVE(7, 14, MoodGroup.SERIOUS),
    PLEASED(15, 15, MoodGroup.SERIOUS);

    //-15 to 15 is a range create normal interactions, but mood can go -15 to -100 due to player interactions.
    public final static int normalMinLevel = -15;
    public final static int absoluteMinLevel = -100;
    public final static int maxLevel = 15;

    private final IntRange level;
    private final MoodGroup moodGroup;

    Mood(int min, int max, MoodGroup moodGroup) {
        this.level = IntRange.between(min, max);
        this.moodGroup = moodGroup;
    }

    public static int getLevel(int mood) {
        return MathHelper.clamp(mood, absoluteMinLevel, maxLevel);
    }

    public MoodGroup getMoodGroup() {
        return this.moodGroup;
    }

    public int getMiddleLevel() {
        return (int)MathHelper.lerp(0.5F, level.getMin(), level.getMax());
    }

    public boolean isInRange(int level) {
        return this.level.getMax() >= level && this.level.getMin() <= level;
    }

    public Text getName() {
        return new TranslatableText("mood." + name().toLowerCase());
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
                return level.choose(API.getRng()) / 20;
            case SHAKE_HAND:
            case FLIRT:
            case KISS:
                return -level.choose(API.getRng()) / 20;
            default:
                return 0;
        }
    }
}
