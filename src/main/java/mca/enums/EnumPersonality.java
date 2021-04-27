package mca.enums;

import mca.core.MCA;

import java.util.*;

public enum EnumPersonality {
    //Fallback on error.
    UNASSIGNED(0, EnumMoodGroup.UNASSIGNED),

    //Positive
    ATHLETIC(1, EnumMoodGroup.PLAYFUL),       //Runs 15% faster
    CONFIDENT(2, EnumMoodGroup.SERIOUS),      //Deals more attack damage
    STRONG(3, EnumMoodGroup.SERIOUS),         //Deals way more attack damage
    FRIENDLY(4, EnumMoodGroup.GENERAL),       //Bonus 15% points to all interactions
    TOUGH(5, EnumMoodGroup.GENERAL),          //25% extra defence

    //Neutral
    CURIOUS(21, EnumMoodGroup.SERIOUS),       //Finds more on chores
    PEACEFUL(22, EnumMoodGroup.GENERAL),      //Will not fight when on full health.
    FLIRTY(23, EnumMoodGroup.PLAYFUL),        //Bonus 5 points to chat, flirt and kiss
    WITTY(24, EnumMoodGroup.PLAYFUL),         //Bonus 2 points and 15 chance to jokes.

    //Negative
    SENSITIVE(41, EnumMoodGroup.GENERAL),     //Double heart penalty
    GREEDY(42, EnumMoodGroup.SERIOUS),        //Finds less on chores
    STUBBORN(43, EnumMoodGroup.SERIOUS),      //20% more difficult to speak with.
    ODD(44, EnumMoodGroup.PLAYFUL),           //Flirts, hugs and kisses are more likely to fail
    SLEEPY(45, EnumMoodGroup.GENERAL),        //20% slower
    FRAGILE(46, EnumMoodGroup.GENERAL),       //Less defence
    WEAK(47, EnumMoodGroup.GENERAL);          //Less damage

    private final int id;
    private final EnumMoodGroup moodGroup;

    //Since we store the personality as id we need frequent id to enum conversions
    private static final Map<Integer, EnumPersonality> map = new HashMap<>();

    static {
        for (EnumPersonality personality : EnumPersonality.values()) {
            map.put(personality.id, personality);
        }
    }

    EnumPersonality(int id, EnumMoodGroup moodGroup) {
        this.id = id;
        this.moodGroup = moodGroup;
    }

    public int getId() {
        return this.id;
    }

    public EnumMoodGroup getMoodGroup() {
        return this.moodGroup;
    }

    public static EnumPersonality getById(int id) {
        return map.getOrDefault(id, UNASSIGNED);
    }

    public static EnumPersonality getRandom() {
        List<EnumPersonality> validList = new ArrayList<>();

        for (EnumPersonality personality : EnumPersonality.values()) {
            if (personality.id != 0) {
                validList.add(personality);
            }
        }

        return validList.get(new Random().nextInt(validList.size()));
    }

    public String getLocalizedName() {
        String name = "personality." + this.name().toLowerCase();
        return MCA.localize(name);
    }

    public String getLocalizedDescription() {
        String name = "personalityDescription." + this.name().toLowerCase();
        return MCA.localize(name);
    }

    public int getSuccessModifierForInteraction(EnumInteraction interaction) {
        switch (interaction) {
            case CHAT:
                return this == STUBBORN ? -20 : 0;
            case JOKE:
                return this == WITTY ? 15 : 0;
            case SHAKE_HAND:
                return 0;
            case TELL_STORY:
                return this == CURIOUS ? 20 : this == STUBBORN ? -20 : 0;
            case FLIRT:
                return this == ODD ? -10 : 0;
            case HUG:
                return this == ODD ? -15 : 0;
            case KISS:
                return this == ODD ? -5 : 0;
            default:
                return 0;
        }
    }

    public int getHeartsModifierForInteraction(EnumInteraction interaction) {
        switch (interaction) {
            case CHAT:
                return this == FRIENDLY ? 1 : this == FLIRTY ? 2 : 0;
            case JOKE:
                return this == FRIENDLY ? 1 : this == WITTY ? 2 : 0;
            case SHAKE_HAND:
                return this == FRIENDLY ? 1 : 0;
            case TELL_STORY:
                return this == FRIENDLY ? 1 : 0;
            case FLIRT:
                return this == FRIENDLY ? 1 : this == FLIRTY ? 3 : 0;
            case HUG:
                return this == FRIENDLY ? 1 : this == ODD ? 2 : 0;
            case KISS:
                return this == FLIRTY ? 1 : 0;
            default:
                return 0;
        }
    }
}
