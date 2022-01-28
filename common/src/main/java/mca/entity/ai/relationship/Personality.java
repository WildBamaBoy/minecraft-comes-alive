package mca.entity.ai.relationship;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.*;

import mca.entity.ai.MoodGroup;

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
    FLIRTY(23, MoodGroup.PLAYFUL),        //likes to chat, flirt and kiss
    WITTY(24, MoodGroup.PLAYFUL),         //likes jokes.

    //Negative
    SENSITIVE(41, MoodGroup.GENERAL),     //Double heart penalty
    GREEDY(42, MoodGroup.SERIOUS),        //Finds less on chores
    STUBBORN(43, MoodGroup.SERIOUS),      //more difficult to speak with.
    ODD(44, MoodGroup.PLAYFUL),           //some interactions are more difficult
    SLEEPY(45, MoodGroup.GENERAL),        //20% slower
    FRAGILE(46, MoodGroup.GENERAL),       //Less defence
    WEAK(47, MoodGroup.GENERAL);          //Less damage

    private final int id;
    private final MoodGroup moodGroup;

    Personality(int id, MoodGroup moodGroup) {
        this.id = id;
        this.moodGroup = moodGroup;
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

    public float getDamageModifier() {
        if (this == Personality.WEAK) {
            return 0.75F;
        }
        if (this == Personality.CONFIDENT) {
            return 1.25F;
        }
        if (this == Personality.STRONG) {
            return 1.5F;
        }
        return 1;
    }

    public float getWeaknessModifier() {
        if (this == Personality.TOUGH) {
            return 0.5F;
        }
        if (this == Personality.FRAGILE) {
            return 1.25F;
        }
        return 1;
    }

    public float getSpeedModifier() {
        if (this == Personality.ATHLETIC) {
            return 1.15F;
        }
        if (this == Personality.SLEEPY) {
            return 0.8F;
        }
        return 1;
    }

    public Text getName() {
        return new TranslatableText("personality." + name().toLowerCase(Locale.ENGLISH));
    }

    public Text getDescription() {
        return new TranslatableText("personalityDescription." + name().toLowerCase(Locale.ENGLISH));
    }
}
