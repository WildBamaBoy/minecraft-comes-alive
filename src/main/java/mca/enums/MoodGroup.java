package mca.enums;

public enum MoodGroup {
    UNASSIGNED,
    GENERAL,
    PLAYFUL,
    SERIOUS;

    public Mood getMood(int moodPoints) {
        int level = Mood.getLevel(moodPoints);

        if (level == 0) {
            return Mood.PASSIVE;
        } else {
            for (Mood mood : Mood.values()) {
                if (mood.getMoodGroup() == this && mood.isInRange(level)) {
                    return mood;
                }
            }
        }

        return Mood.PASSIVE;
    }

    public int getSuccessModifierForInteraction(Interaction interaction) {
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
            case KISS:
            case HUG:
                return this == GENERAL ? 0 : this == PLAYFUL ? 3 : this == SERIOUS ? -2 : 0;
            default:
                return 0;
        }
    }

    public int getHeartsModifierForInteraction(Interaction interaction) {
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
            case KISS:
            case HUG:
                return this == GENERAL ? 0 : this == PLAYFUL ? 2 : this == SERIOUS ? -1 : 0;
            default:
                return 0;
        }
    }
}
