package mca.core.minecraft;

import mca.util.ItemStackCache;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder("mca")
public class ProfessionsMCA {
    public static final VillagerProfession guard = new VillagerProfession("mca:guard", "mca:textures/skins/", "mca:textures/skins/");
    public static final VillagerProfession bandit = new VillagerProfession("mca:bandit", "mca:textures/skins/", "mca:textures/skins/");
    public static final VillagerProfession child = new VillagerProfession("mca:child", "mca:textures/skins/", "mca:textures/skins/");
    public static final VillagerProfession baker = new VillagerProfession("mca:baker", "mca:textures/skins/", "mca:textures/skins/");
    public static final VillagerProfession miner = new VillagerProfession("mca:miner", "mca:textures/skins/", "mca:textures/skins/");

    public static VillagerCareer guard_warrior;
    public static VillagerCareer guard_archer;
    public static VillagerCareer guard_hero;
    public static VillagerCareer bandit_marauder;
    public static VillagerCareer bandit_outlaw;
    public static VillagerCareer bandit_pillager;
    public static VillagerCareer child_child;
    public static VillagerCareer baker_baker;
    public static VillagerCareer miner_miner;

    public static void registerCareers() {
        guard_warrior = new VillagerCareer(guard, "warrior");
        guard_archer = new VillagerCareer(guard, "archer");
        guard_hero = new VillagerCareer(guard, "hero");
        bandit_marauder = new VillagerCareer(bandit, "marauder");
        bandit_outlaw = new VillagerCareer(bandit, "outlaw");
        bandit_pillager = new VillagerCareer(bandit, "pillager");
        child_child = new VillagerCareer(child, "child");
        baker_baker = new VillagerCareer(baker, "baker");
        miner_miner = new VillagerCareer(miner, "miner");
    }

    public static ItemStack getDefaultHeldItem(VillagerProfession profession, VillagerCareer career) {
        if (profession == ProfessionsMCA.guard) {
            return career == ProfessionsMCA.guard_archer ? ItemStackCache.get(Items.BOW) : ItemStackCache.get(Items.IRON_SWORD);
        } else if (profession == ProfessionsMCA.bandit) {
            return ItemStackCache.get(Items.IRON_SWORD);
        }
        return ItemStack.EMPTY;
    }

    @Mod.EventBusSubscriber(modid = "mca")
    public static class RegistrationHandler {
        /**
         * Register this mod's {@link VillagerProfession}s.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void onEvent(final RegistryEvent.Register<VillagerProfession> event) {
            final IForgeRegistry<VillagerProfession> registry = event.getRegistry();

            registry.register(guard);
            registry.register(bandit);
            registry.register(child);
            registry.register(baker);
            registry.register(miner);
        }
    }
}
