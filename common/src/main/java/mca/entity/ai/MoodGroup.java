package mca.entity.ai;

import java.util.Arrays;
import java.util.List;

import mca.SoundsMCA;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public enum MoodGroup {
    UNASSIGNED(
            new Mood("passive")
    ),
    GENERAL(
            new Mood("depressed", 2, SoundsMCA.VILLAGER_MALE_CRY, SoundsMCA.VILLAGER_FEMALE_CRY, 20, ParticleTypes.SPLASH, Formatting.RED),
            new Mood("sad", 8, SoundsMCA.VILLAGER_MALE_CRY, SoundsMCA.VILLAGER_FEMALE_CRY, 50, ParticleTypes.SPLASH, Formatting.GOLD),
            new Mood("unhappy"),
            new Mood("passive"),
            new Mood("fine"),
            new Mood("happy", 0, null, null, 0, null, Formatting.DARK_GREEN),
            new Mood("overjoyed", 8, SoundsMCA.VILLAGER_MALE_LAUGH, SoundsMCA.VILLAGER_FEMALE_LAUGH, 50, ParticleTypes.HAPPY_VILLAGER, Formatting.GREEN)
    ),
    PLAYFUL(
            new Mood("boredToTears", 0, null, null, 0, null, Formatting.RED),
            new Mood("bored", 0, null, null, 0, null, Formatting.GOLD),
            new Mood("uninterested"),
            new Mood("passive"),
            new Mood("silly"),
            new Mood("giggly", 8, SoundsMCA.VILLAGER_MALE_LAUGH, SoundsMCA.VILLAGER_FEMALE_LAUGH, 50, ParticleTypes.HAPPY_VILLAGER, Formatting.DARK_GREEN),
            new Mood("entertained", 2, SoundsMCA.VILLAGER_MALE_LAUGH, SoundsMCA.VILLAGER_FEMALE_LAUGH, 20, ParticleTypes.HAPPY_VILLAGER, Formatting.GREEN)
    ),
    SERIOUS(
            new Mood("infuriated", 2, SoundsMCA.VILLAGER_MALE_ANGRY, SoundsMCA.VILLAGER_FEMALE_ANGRY, 20, ParticleTypes.ANGRY_VILLAGER, Formatting.RED),
            new Mood("angry", 8, SoundsMCA.VILLAGER_MALE_ANGRY, SoundsMCA.VILLAGER_FEMALE_ANGRY, 50, ParticleTypes.ANGRY_VILLAGER, Formatting.GOLD),
            new Mood("annoyed"),
            new Mood("passive"),
            new Mood("interested"),
            new Mood("talkative", 0, null, null, 0, null, Formatting.DARK_GREEN),
            new Mood("pleased", 0, null, null, 0, null, Formatting.GREEN)
    );

    //-15 to 15 is a range create normal interactions, but mood can go -15 to -100 due to player interactions.
    public final static int normalMinLevel = -15;
    public final static int absoluteMinLevel = -100;
    public final static int maxLevel = 15;

    private final List<Mood> moods;

    MoodGroup(Mood... m) {
        moods = Arrays.asList(m);
    }

    // clamps to valid range
    public static int clampMood(int moodPoints) {
        return MathHelper.clamp(moodPoints, absoluteMinLevel, maxLevel);
    }

    // returns the index of mood based on mood points
    private int getLevel(int moodPoints) {
        return MathHelper.clamp(
                (moodPoints - normalMinLevel) * moods.size() / (maxLevel - normalMinLevel),
                0,
                moods.size() - 1
        );
    }

    public Mood getMood(int moodPoints) {
        int level = getLevel(moodPoints);
        return moods.get(level);
    }
}
