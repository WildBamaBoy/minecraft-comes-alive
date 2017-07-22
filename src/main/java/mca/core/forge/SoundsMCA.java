package mca.core.forge;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class SoundsMCA 
{
	private static final ResourceLocation loc_reaper_scythe_out = new ResourceLocation("mca:reaper.scythe.out");
	private static final ResourceLocation loc_reaper_scythe_swing = new ResourceLocation("mca:reaper.scythe.swing");
	private static final ResourceLocation loc_reaper_idle = new ResourceLocation("mca:reaper.idle");
	private static final ResourceLocation loc_reaper_death = new ResourceLocation("mca:reaper.death");
	private static final ResourceLocation loc_reaper_block = new ResourceLocation("mca:reaper.block");
	private static final ResourceLocation loc_reaper_summon = new ResourceLocation("mca:reaper.summon");
	
	public static final SoundEvent reaper_scythe_out = new SoundEvent(loc_reaper_scythe_out);
	public static final SoundEvent reaper_scythe_swing = new SoundEvent(loc_reaper_scythe_swing);
	public static final SoundEvent reaper_idle = new SoundEvent(loc_reaper_idle);
	public static final SoundEvent reaper_death = new SoundEvent(loc_reaper_death);
	public static final SoundEvent reaper_block = new SoundEvent(loc_reaper_block);
	public static final SoundEvent reaper_summon = new SoundEvent(loc_reaper_summon);
	
	private SoundsMCA(){};
	
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		IForgeRegistry<SoundEvent> registry = event.getRegistry();
		registry.register(reaper_scythe_out);
		registry.register(reaper_scythe_swing);
		registry.register(reaper_idle);
		registry.register(reaper_death);
		registry.register(reaper_block);
		registry.register(reaper_summon);
	}
}
