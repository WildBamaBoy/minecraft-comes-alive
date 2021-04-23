package mca.enums;

import mca.core.MCA;

@SuppressWarnings({"DuplicateBranchesInSwitch", "DuplicateExpressions"})
public enum EnumMood {
    DEPRESSED(-3, EnumMoodGroup.GENERAL),
    SAD(-2, EnumMoodGroup.GENERAL),
    UNHAPPY(-1, EnumMoodGroup.GENERAL),
    PASSIVE(0, EnumMoodGroup.UNASSIGNED),
    FINE(1, EnumMoodGroup.GENERAL),
    HAPPY(2, EnumMoodGroup.GENERAL),
    OVERJOYED(3, EnumMoodGroup.GENERAL),

    BORED_TO_TEARS(-3, EnumMoodGroup.PLAYFUL),
    BORED(-2, EnumMoodGroup.PLAYFUL),
    UNINTERESTED(-1, EnumMoodGroup.PLAYFUL),
    SILLY(1, EnumMoodGroup.PLAYFUL),
    GIGGLY(2, EnumMoodGroup.PLAYFUL),
    ENTERTAINED(3, EnumMoodGroup.PLAYFUL),

    INFURIATED(-3, EnumMoodGroup.SERIOUS),
    ANGRY(-2, EnumMoodGroup.SERIOUS),
    ANNOYED(-1, EnumMoodGroup.SERIOUS),
    INTERESTED(1, EnumMoodGroup.SERIOUS),
    TALKATIVE(2, EnumMoodGroup.SERIOUS),
    PLEASED(3, EnumMoodGroup.SERIOUS);

    private final int level;
    private final EnumMoodGroup moodGroup;

    public final static int minLevel = -3;
    public final static int maxLevel = 3;
    public final static int levelsPerMood = 3;

    EnumMood(int level, EnumMoodGroup moodGroup) {
        this.level = level;
        this.moodGroup = moodGroup;
    }

    public EnumMoodGroup getMoodGroup() {
        return this.moodGroup;
    }

    public int getLevel() {
        return this.level;
    }

    public static int getLevel(int mood) {
        return Math.min(maxLevel, Math.max(minLevel, Math.round((float) mood / levelsPerMood)));
    }

    public String getLocalizedName() {
        String name = "mood." + this.name().toLowerCase();
        return MCA.getLocalizer().localize(name);
    }

    public int getSuccessModifierForInteraction(EnumInteraction interaction) {
        int base = 0;

        switch (interaction) {
            case CHAT:
                base = moodGroup == EnumMoodGroup.GENERAL ? 5 : moodGroup == EnumMoodGroup.PLAYFUL ? 0 : moodGroup == EnumMoodGroup.SERIOUS ? 2 : 0;
                break;
            case JOKE:
                base = moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 5 : moodGroup == EnumMoodGroup.SERIOUS ? -3 : 0;
                break;
            case SHAKE_HAND:
                base = moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 0 : moodGroup == EnumMoodGroup.SERIOUS ? 5 : 0;
                break;
            case TELL_STORY:
                base = moodGroup == EnumMoodGroup.GENERAL ? 3 : moodGroup == EnumMoodGroup.PLAYFUL ? 0 : moodGroup == EnumMoodGroup.SERIOUS ? 3 : 0;
                break;
            case FLIRT:
                base = moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 3 : moodGroup == EnumMoodGroup.SERIOUS ? -2 : 0;
                break;
            case HUG:
                base = moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 3 : moodGroup == EnumMoodGroup.SERIOUS ? -2 : 0;
                break;
            case KISS:
                base = moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 3 : moodGroup == EnumMoodGroup.SERIOUS ? -2 : 0;
                break;
            default:
                break;
        }

        return base * level;
    }

    public int getHeartsModifierForInteraction(EnumInteraction interaction) {
        int base = 0;

        switch (interaction) {
            case CHAT:
                base = moodGroup == EnumMoodGroup.GENERAL ? 3 : moodGroup == EnumMoodGroup.PLAYFUL ? 0 : moodGroup == EnumMoodGroup.SERIOUS ? 1 : 0;
                break;
            case JOKE:
                base = moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 3 : moodGroup == EnumMoodGroup.SERIOUS ? -2 : 0;
                break;
            case SHAKE_HAND:
                base = moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 0 : moodGroup == EnumMoodGroup.SERIOUS ? 3 : 0;
                break;
            case TELL_STORY:
                base = moodGroup == EnumMoodGroup.GENERAL ? 2 : moodGroup == EnumMoodGroup.PLAYFUL ? 0 : moodGroup == EnumMoodGroup.SERIOUS ? 2 : 0;
                break;
            case FLIRT:
                base = moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 2 : moodGroup == EnumMoodGroup.SERIOUS ? -1 : 0;
                break;
            case HUG:
                base = moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 2 : moodGroup == EnumMoodGroup.SERIOUS ? -1 : 0;
                break;
            case KISS:
                base = moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 2 : moodGroup == EnumMoodGroup.SERIOUS ? -1 : 0;
                break;
            default:
                break;
        }

        return base * level;
    }
}
