package mca.core.minecraft;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.LinkedList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "mca")
public final class SoundsMCA {
    static final List<SoundEvent> soundList = new LinkedList<>();

    public static final SoundEvent reaper_scythe_out = newSound("reaper.scythe_out");
    public static final SoundEvent reaper_scythe_swing = newSound("reaper.scythe_swing");
    public static final SoundEvent reaper_idle = newSound("reaper.idle");
    public static final SoundEvent reaper_death = newSound("reaper.death");
    public static final SoundEvent reaper_block = newSound("reaper.block");
    public static final SoundEvent reaper_summon = newSound("reaper.summon");

    public static final SoundEvent working_anvil = newSound("working.anvil");
    public static final SoundEvent working_page = newSound("working.page");
    public static final SoundEvent working_saw = newSound("working.saw");
    public static final SoundEvent working_sharpen = newSound("working.sharpen");

    //simplifies sound creation
    private static SoundEvent newSound(String sound) {
        ResourceLocation loc = new ResourceLocation("mca", sound);
        SoundEvent event = new SoundEvent(loc).setRegistryName(loc);
        soundList.add(event);
        return event;
    }

    public static void register() {
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<SoundEvent> event) {
        IForgeRegistry<SoundEvent> registry = event.getRegistry();

        for (SoundEvent e : soundList) {
            registry.register(e);
        }
    }
}