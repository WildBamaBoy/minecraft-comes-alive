package mca.enums;

import mca.core.MCA;

import java.util.*;

public enum Personality {
    //Fallback on error.
    UNASSIGNED(0, MoodGroup.UNASSIGNED),

    //Positive
    ATHLETIC(1, MoodGroup.PLAYFUL),       //Runs 15% faster
    CONFIDENT(2, MoodGroup.SERIOUS),      //Deals more attack damage
    STRONG(3, MoodGroup.SERIOUS),         //Deals way more attack damage
    FRIENDLY(4, MoodGroup.GENERAL),       //Bonus 15% points to all interactions
    TOUGH(5, MoodGroup.GENERAL),          //25% extra defence

    //Neutral
    CURIOUS(21, MoodGroup.SERIOUS),       //Finds more on chores
    PEACEFUL(22, MoodGroup.GENERAL),      //Will not fight when on full health.
    FLIRTY(23, MoodGroup.PLAYFUL),        //Bonus 5 points to chat, flirt and kiss
    WITTY(24, MoodGroup.PLAYFUL),         //Bonus 2 points and 15 chance to jokes.

    //Negative
    SENSITIVE(41, MoodGroup.GENERAL),     //Double heart penalty
    GREEDY(42, MoodGroup.SERIOUS),        //Finds less on chores
    STUBBORN(43, MoodGroup.SERIOUS),      //20% more difficult to speak with.
    ODD(44, MoodGroup.PLAYFUL),           //Flirts, hugs and kisses are more likely to fail
    SLEEPY(45, MoodGroup.GENERAL),        //20% slower
    FRAGILE(46, MoodGroup.GENERAL),       //Less defence
    WEAK(47, MoodGroup.GENERAL);          //Less damage

    //Since we store the personality as id we need frequent id to enum conversions
    private static final Map<Integer, Personality> map = new HashMap<>();

    static {
        for (Personality personality : Personality.values()) {
            map.put(personality.id, personality);
        }
    }

    private final int id;
    private final MoodGroup moodGroup;

    Personality(int id, MoodGroup moodGroup) {
        this.id = id;
        this.moodGroup = moodGroup;
    }

    public static Personality getById(int id) {
        return map.getOrDefault(id, UNASSIGNED);
    }

    public static Personality getRandom() {
        List<Personality> validList = new ArrayList<>();

        for (Personality personality : Personality.values()) {
            if (personality.id != 0) {
                validList.add(personality);
            }
        }

        return validList.get(new Random().nextInt(validList.size()));
    }

    public int getId() {
        return this.id;
    }

    public MoodGroup getMoodGroup() {
        return this.moodGroup;
    }

    public String getLocalizedName() {
        String name = "personality." + this.name().toLowerCase();
        return MCA.localize(name);
    }

    public String getLocalizedDescription() {
        String name = "personalityDescription." + this.name().toLowerCase();
        return MCA.localize(name);
    }

    public int getSuccessModifierForInteraction(Interaction interaction) {
        switch (interaction) {
            case CHAT:
                return this == STUBBORN ? -20 : 0;
            case JOKE:
                return this == WITTY ? 15 : 0;
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

    public int getHeartsModifierForInteraction(Interaction interaction) {
        switch (interaction) {
            case CHAT:
                return this == FRIENDLY ? 1 : this == FLIRTY ? 2 : 0;
            case JOKE:
                return this == FRIENDLY ? 1 : this == WITTY ? 2 : 0;
            case SHAKE_HAND:
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
