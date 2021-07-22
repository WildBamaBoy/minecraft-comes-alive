package mca;

import mca.cobalt.registration.Registration;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface SoundsMCA {
    SoundEvent reaper_scythe_out = register("reaper.scythe_out");
    SoundEvent reaper_scythe_swing = register("reaper.scythe_swing");
    SoundEvent reaper_idle = register("reaper.idle");
    SoundEvent reaper_death = register("reaper.death");
    SoundEvent reaper_block = register("reaper.block");
    SoundEvent reaper_summon = register("reaper.summon");

    SoundEvent working_anvil = register("working.anvil");
    SoundEvent working_page = register("working.page");
    SoundEvent working_saw = register("working.saw");
    SoundEvent working_sharpen = register("working.sharpen");

    static void bootstrap() { }

    static SoundEvent register(String sound) {
        Identifier id = new Identifier(MCA.MOD_ID, sound);
        return Registration.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }
}