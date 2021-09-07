package mca.entity.ai;

import java.util.Arrays;
import java.util.List;

import mca.SoundsMCA;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public enum MoodGroup {
    UNASSIGNED(
            new MoodBuilder("passive").build()
    ),
    GENERAL(
            new MoodBuilder("depressed")
                    .sounds(2, SoundsMCA.VILLAGER_MALE_CRY, SoundsMCA.VILLAGER_FEMALE_CRY)
                    .particles(20, ParticleTypes.SPLASH)
                    .building("inn")
                    .color(Formatting.RED).build(),
            new MoodBuilder("sad")
                    .sounds(8, SoundsMCA.VILLAGER_MALE_CRY, SoundsMCA.VILLAGER_FEMALE_CRY)
                    .particles(50, ParticleTypes.SPLASH)
                    .building("inn")
                    .color(Formatting.GOLD).build(),
            new MoodBuilder("unhappy").build(),
            new MoodBuilder("passive").build(),
            new MoodBuilder("fine").build(),
            new MoodBuilder("happy")
                    .color(Formatting.DARK_GREEN).build(),
            new MoodBuilder("overjoyed")
                    .sounds(8, SoundsMCA.VILLAGER_MALE_LAUGH, SoundsMCA.VILLAGER_FEMALE_LAUGH)
                    .particles(50, ParticleTypes.HAPPY_VILLAGER)
                    .color(Formatting.GREEN).build()
    ),
    PLAYFUL(
            new MoodBuilder("bored_to_tears")
                    .color(Formatting.RED).build(),
            new MoodBuilder("bored")
                    .color(Formatting.GOLD).build(),
            new MoodBuilder("uninterested").build(),
            new MoodBuilder("passive").build(),
            new MoodBuilder("silly").build(),
            new MoodBuilder("giggly")
                    .sounds(8, SoundsMCA.VILLAGER_MALE_LAUGH, SoundsMCA.VILLAGER_FEMALE_LAUGH)
                    .particles(50, ParticleTypes.HAPPY_VILLAGER)
                    .building("inn")
                    .color(Formatting.DARK_GREEN).build(),
            new MoodBuilder("entertained")
                    .sounds(2, SoundsMCA.VILLAGER_MALE_LAUGH, SoundsMCA.VILLAGER_FEMALE_LAUGH)
                    .particles(20, ParticleTypes.HAPPY_VILLAGER)
                    .building("inn")
                    .color(Formatting.GREEN).build()
    ),
    SERIOUS(
            new MoodBuilder("infuriated")
                    .sounds(2, SoundsMCA.VILLAGER_MALE_ANGRY, SoundsMCA.VILLAGER_FEMALE_ANGRY)
                    .particles(20, ParticleTypes.ANGRY_VILLAGER)
                    .color(Formatting.RED).build(),
            new MoodBuilder("angry")
                    .sounds(8, SoundsMCA.VILLAGER_MALE_ANGRY, SoundsMCA.VILLAGER_FEMALE_ANGRY)
                    .particles(50, ParticleTypes.ANGRY_VILLAGER)
                    .color(Formatting.GOLD).build(),
            new MoodBuilder("annoyed").build(),
            new MoodBuilder("passive").build(),
            new MoodBuilder("interested").build(),
            new MoodBuilder("talkative")
                    .building("inn")
                    .color(Formatting.DARK_GREEN).build(),
            new MoodBuilder("pleased")
                    .color(Formatting.GREEN).build()
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
