package mca.entity.ai;

import java.util.Locale;
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
    private final String building;

    Mood(String name, int soundInterval, SoundEvent soundMale, SoundEvent soundFemale, int particleInterval, DefaultParticleType particle, Formatting color, String building) {
        this.name = name;
        this.soundInterval = soundInterval;
        this.soundMale = soundMale;
        this.soundFemale = soundFemale;
        this.particleInterval = particleInterval;
        this.particle = particle;
        this.color = color;
        this.building = building;
    }

    public Text getText() {
        return new TranslatableText("mood." + name.toLowerCase(Locale.ENGLISH));
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

    public String getBuilding() {
        return building;
    }
}
