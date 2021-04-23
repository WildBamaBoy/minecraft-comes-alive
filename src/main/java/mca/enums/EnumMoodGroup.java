package mca.enums;

public enum EnumMoodGroup {
    UNASSIGNED,
    GENERAL,
    PLAYFUL,
    SERIOUS;

    public EnumMood getMood(int moodPoints) {
        int level = EnumMood.getLevel(moodPoints);

        if (level == 0) {
            return EnumMood.PASSIVE;
        } else {
            for (EnumMood mood : EnumMood.values()) {
                if (mood.getMoodGroup() == this && mood.getLevel() == level) {
                    return mood;
                }
            }
        }

        return EnumMood.PASSIVE;
    }

    public int getSuccessModifierForInteraction(EnumInteraction interaction) {
        switch (interaction) {
            case CHAT:
                return this == GENERAL ? 5 : this == PLAYFUL ? 0 : this == SERIOUS ? 2 : 0;
            case JOKE:
                return this == GENERAL ? 0 : this == PLAYFUL ? 5 : this == SERIOUS ? -3 : 0;
            case SHAKE_HAND:
                return this == GENERAL ? 0 : this == PLAYFUL ? 0 : this == SERIOUS ? 5 : 0;
            case TELL_STORY:
                return this == GENERAL ? 3 : this == PLAYFUL ? 0 : this == SERIOUS ? 3 : 0;
            case FLIRT:
                return this == GENERAL ? 0 : this == PLAYFUL ? 3 : this == SERIOUS ? -2 : 0;
            case HUG:
                return this == GENERAL ? 0 : this == PLAYFUL ? 3 : this == SERIOUS ? -2 : 0;
            case KISS:
                return this == GENERAL ? 0 : this == PLAYFUL ? 3 : this == SERIOUS ? -2 : 0;
            default:
                return 0;
        }
    }

    public int getHeartsModifierForInteraction(EnumInteraction interaction) {
        switch (interaction) {
            case CHAT:
                return this == GENERAL ? 3 : this == PLAYFUL ? 0 : this == SERIOUS ? 1 : 0;
            case JOKE:
                return this == GENERAL ? 0 : this == PLAYFUL ? 3 : this == SERIOUS ? -2 : 0;
            case SHAKE_HAND:
                return this == GENERAL ? 0 : this == PLAYFUL ? 0 : this == SERIOUS ? 3 : 0;
            case TELL_STORY:
                return this == GENERAL ? 2 : this == PLAYFUL ? 0 : this == SERIOUS ? 2 : 0;
            case FLIRT:
                return this == GENERAL ? 0 : this == PLAYFUL ? 2 : this == SERIOUS ? -1 : 0;
            case HUG:
                return this == GENERAL ? 0 : this == PLAYFUL ? 2 : this == SERIOUS ? -1 : 0;
            case KISS:
                return this == GENERAL ? 0 : this == PLAYFUL ? 2 : this == SERIOUS ? -1 : 0;
            default:
                return 0;
        }
    }
}
