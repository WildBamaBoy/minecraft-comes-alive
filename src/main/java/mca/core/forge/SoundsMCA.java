package mca.core.forge;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class SoundsMCA 
{
	private static final ResourceLocation loc_reaper_scythe_out = new ResourceLocation("mca", "reaper-scythe-out");
	private static final ResourceLocation loc_reaper_scythe_swing = new ResourceLocation("mca", "reaper-scythe-out");
	private static final ResourceLocation loc_reaper_idle = new ResourceLocation("mca", "reaper-scythe-out");
	private static final ResourceLocation loc_reaper_death = new ResourceLocation("mca", "reaper-scythe-out");
	private static final ResourceLocation loc_reaper_block = new ResourceLocation("mca", "reaper-scythe-out");
	private static final ResourceLocation loc_reaper_summon = new ResourceLocation("mca", "reaper-scythe-out");
	
	public static final SoundEvent reaper_scythe_out = new SoundEvent(loc_reaper_scythe_out);
	public static final SoundEvent reaper_scythe_swing = new SoundEvent(loc_reaper_scythe_swing);
	public static final SoundEvent reaper_idle = new SoundEvent(loc_reaper_idle);
	public static final SoundEvent reaper_death = new SoundEvent(loc_reaper_death);
	public static final SoundEvent reaper_block = new SoundEvent(loc_reaper_block);
	public static final SoundEvent reaper_summon = new SoundEvent(loc_reaper_summon);
	
	private SoundsMCA(){};
	
	public static void registerSounds()
	{
		GameRegistry.register(reaper_scythe_out, loc_reaper_scythe_out);
		GameRegistry.register(reaper_scythe_swing, loc_reaper_scythe_swing);
		GameRegistry.register(reaper_idle, loc_reaper_idle);
		GameRegistry.register(reaper_death, loc_reaper_death);
		GameRegistry.register(reaper_block, loc_reaper_block);
		GameRegistry.register(reaper_summon, loc_reaper_summon);
	}
}
