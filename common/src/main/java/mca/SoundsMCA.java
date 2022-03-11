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

    SoundEvent VILLAGER_BABY_LAUGH = register("villager_baby_laugh");//TODO:

    SoundEvent VILLAGER_MALE_SCREAM = register("villager_male_scream");
    SoundEvent VILLAGER_FEMALE_SCREAM = register("villager_female_scream");

    SoundEvent VILLAGER_MALE_LAUGH = register("villager_male_laugh");
    SoundEvent VILLAGER_FEMALE_LAUGH = register("villager_female_laugh");//TODO:

    SoundEvent VILLAGER_MALE_CRY = register("villager_male_cry");
    SoundEvent VILLAGER_FEMALE_CRY = register("villager_female_cry");//TODO:

    SoundEvent VILLAGER_MALE_ANGRY = register("villager_male_angry");//TODO:
    SoundEvent VILLAGER_FEMALE_ANGRY = register("villager_female_angry");//TODO:

    SoundEvent VILLAGER_MALE_GREET = register("villager_male_greet");
    SoundEvent VILLAGER_FEMALE_GREET = register("villager_female_greet");//TODO:

    SoundEvent VILLAGER_MALE_SURPRISE = register("villager_male_surprise");
    SoundEvent VILLAGER_FEMALE_SURPRISE = register("villager_female_surprise");//TODO:

    static void bootstrap() { }

    static SoundEvent register(String sound) {
        Identifier id = new Identifier(MCA.MOD_ID, sound);
        return Registration.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }
}