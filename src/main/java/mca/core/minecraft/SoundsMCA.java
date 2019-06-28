package mca.core.minecraft;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class SoundsMCA {
    private static final ResourceLocation loc_reaper_scythe_out = new ResourceLocation("mca:reaper.scythe.out");
    public static final SoundEvent reaper_scythe_out = new SoundEvent(loc_reaper_scythe_out);
    private static final ResourceLocation loc_reaper_scythe_swing = new ResourceLocation("mca:reaper.scythe.swing");
    public static final SoundEvent reaper_scythe_swing = new SoundEvent(loc_reaper_scythe_swing);
    private static final ResourceLocation loc_reaper_idle = new ResourceLocation("mca:reaper.idle");
    public static final SoundEvent reaper_idle = new SoundEvent(loc_reaper_idle);
    private static final ResourceLocation loc_reaper_death = new ResourceLocation("mca:reaper.death");
    public static final SoundEvent reaper_death = new SoundEvent(loc_reaper_death);
    private static final ResourceLocation loc_reaper_block = new ResourceLocation("mca:reaper.block");
    public static final SoundEvent reaper_block = new SoundEvent(loc_reaper_block);
    private static final ResourceLocation loc_reaper_summon = new ResourceLocation("mca:reaper.summon");
    public static final SoundEvent reaper_summon = new SoundEvent(loc_reaper_summon);

    public static void register(RegistryEvent.Register<SoundEvent> event) {
        IForgeRegistry<SoundEvent> registry = event.getRegistry();
        reaper_scythe_out.setRegistryName(loc_reaper_scythe_out);
        reaper_scythe_swing.setRegistryName(loc_reaper_scythe_swing);
        reaper_idle.setRegistryName(loc_reaper_idle);
        reaper_death.setRegistryName(loc_reaper_death);
        reaper_block.setRegistryName(loc_reaper_block);
        reaper_summon.setRegistryName(loc_reaper_summon);

        registry.register(reaper_scythe_out);
        registry.register(reaper_scythe_swing);
        registry.register(reaper_idle);
        registry.register(reaper_death);
        registry.register(reaper_block);
        registry.register(reaper_summon);
    }
}
