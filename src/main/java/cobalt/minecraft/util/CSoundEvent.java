package cobalt.minecraft.util;

import lombok.Getter;
import net.minecraft.util.SoundEvent;

public class CSoundEvent {
    @Getter private final SoundEvent mcSound;
    private CSoundEvent(SoundEvent sound) {
        this.mcSound = sound;
    }

    public static CSoundEvent fromMC(SoundEvent event) {
        return new CSoundEvent(event);
    }
}
