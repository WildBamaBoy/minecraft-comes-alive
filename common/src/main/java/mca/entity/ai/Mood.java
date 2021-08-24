package mca.entity.ai;

import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class Mood {
    private final String name;

    private final int soundInterval;
    private final SoundEvent soundMale;
    private final SoundEvent soundFemale;
    private final int particleInterval;
    private final DefaultParticleType particle;
    private final Formatting color;

    Mood(String name) {
        this(name, 0, null, null);
    }

    Mood(String name, int soundInterval, SoundEvent soundMale, SoundEvent soundFemale) {
        this(name, soundInterval, soundMale, soundFemale, 0, null, Formatting.WHITE);
    }

    Mood(String name, int soundInterval, SoundEvent soundMale, SoundEvent soundFemale, int particleInterval, DefaultParticleType particle, Formatting color) {
        this.name = name;
        this.soundInterval = soundInterval;
        this.soundMale = soundMale;
        this.soundFemale = soundFemale;
        this.particleInterval = particleInterval;
        this.particle = particle;
        this.color = color;
    }

    public Text getText() {
        return new TranslatableText("mood." + name.toLowerCase());
    }

    public String getName() {
        return name;
    }

    public int getSoundInterval() {
        return soundInterval;
    }

    public SoundEvent getSoundMale() {
        return soundMale;
    }

    public SoundEvent getSoundFemale() {
        return soundFemale;
    }

    public int getParticleInterval() {
        return particleInterval;
    }

    public DefaultParticleType getParticle() {
        return particle;
    }

    public Formatting getColor() {
        return color;
    }
}
