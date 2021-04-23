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
}
