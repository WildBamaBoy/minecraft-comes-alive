package mca.entity.ai;

import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Formatting;

public class MoodBuilder {
    private final String name;

    private int soundInterval = 0;
    private SoundEvent soundMale;
    private SoundEvent soundFemale;
    private int particleInterval = 0;
    private DefaultParticleType particle;
    private Formatting color = Formatting.WHITE;
    private String building;

    public MoodBuilder(String name) {
        this.name = name;
    }

    public MoodBuilder sounds(int soundInterval, SoundEvent soundMale, SoundEvent soundFemale) {
        this.soundInterval = soundInterval;
        this.soundMale = soundMale;
        this.soundFemale = soundFemale;
        return this;
    }

    public MoodBuilder particles(int particleInterval, DefaultParticleType particle) {
        this.particleInterval = particleInterval;
        this.particle = particle;
        return this;
    }

    public MoodBuilder color(Formatting color) {
        this.color = color;
        return this;
    }

    public MoodBuilder building(String building) {
        this.building = building;
        return this;
    }

    public Mood build() {
        return new Mood(name, soundInterval, soundMale, soundFemale, particleInterval, particle, color, building);
    }
}
