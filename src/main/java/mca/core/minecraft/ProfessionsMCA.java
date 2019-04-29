package mca.core.minecraft;

import mca.util.ItemStackCache;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Random;

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

        baker_baker.addTrade(1, new BakerTradesLvl1());
        baker_baker.addTrade(2, new BakerTradesLvl2());
        baker_baker.addTrade(3, new BakerTradesLvl3());

        miner_miner.addTrade(1, new MinerTradesLvl1());
        miner_miner.addTrade(2, new MinerTradesLvl2());
        miner_miner.addTrade(3, new MinerTradesLvl3());
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

    public static class BakerTradesLvl1 implements EntityVillager.ITradeList {
        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.WHEAT, 6)));
            recipeList.add(new MerchantRecipe(new ItemStack(Items.MILK_BUCKET, 1), new ItemStack(Items.BREAD, 2)));
        }
    }

    public static class BakerTradesLvl2 implements EntityVillager.ITradeList {
        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.EGG, 12)));
            recipeList.add(new MerchantRecipe(new ItemStack(Items.SUGAR, 2), new ItemStack(Items.CAKE, 1)));
            recipeList.add(new MerchantRecipe(new ItemStack(Items.STONE_HOE, 1), new ItemStack(Items.WHEAT, 10)));
        }
    }

    public static class BakerTradesLvl3 implements EntityVillager.ITradeList {
        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            recipeList.add(new MerchantRecipe(new ItemStack(Items.CAKE, 1), new ItemStack(Items.EMERALD, 5)));
            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BREAD, 4)));
            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.COOKIE, 6)));
            recipeList.add(new MerchantRecipe(new ItemStack(Blocks.PUMPKIN, 1), new ItemStack(Items.PUMPKIN_PIE, 1)));
        }
    }

    public static class MinerTradesLvl1 implements EntityVillager.ITradeList {
        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            recipeList.add(new MerchantRecipe(new ItemStack(Blocks.COBBLESTONE, 8), new ItemStack(Blocks.STONE, 4)));
            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 1), new ItemStack(Blocks.TORCH, 8)));
        }
    }

    public static class MinerTradesLvl2 implements EntityVillager.ITradeList {
        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            recipeList.add(new MerchantRecipe(new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.EMERALD, 2)));
            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.COAL, 3)));
        }
    }

    public static class MinerTradesLvl3 implements EntityVillager.ITradeList {
        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 4), new ItemStack(Items.IRON_PICKAXE, 1)));
            recipeList.add(new MerchantRecipe(new ItemStack(Blocks.EMERALD_BLOCK, 1), new ItemStack(Items.DIAMOND_PICKAXE, 1)));
            recipeList.add(new MerchantRecipe(new ItemStack(Items.IRON_INGOT, 8), new ItemStack(Items.DIAMOND, 1)));
        }
    }
}
