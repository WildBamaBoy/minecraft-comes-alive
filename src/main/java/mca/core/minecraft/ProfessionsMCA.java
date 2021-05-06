package mca.core.minecraft;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Random;

public class ProfessionsMCA {
    public static final VillagerProfession guard = new VillagerProfession("mca:guard", PointOfInterestType.WEAPONSMITH, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_WEAPONSMITH);
    public static final VillagerProfession bandit = new VillagerProfession("mca:bandit", PointOfInterestType.WEAPONSMITH, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_WEAPONSMITH);
    public static final VillagerProfession child = new VillagerProfession("mca:child", PointOfInterestType.WEAPONSMITH, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_WEAPONSMITH);
    public static final VillagerProfession baker = new VillagerProfession("mca:baker", PointOfInterestType.WEAPONSMITH, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_WEAPONSMITH);
    public static final VillagerProfession miner = new VillagerProfession("mca:miner", PointOfInterestType.WEAPONSMITH, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_WEAPONSMITH);

    public static IForgeRegistry<VillagerProfession> registry;

    private static final VillagerProfession[] FORBIDDEN_RANDOM_PROFESSIONS = {
            bandit, child
    };

    public static void registerCareers() {
//        baker_baker.addTrade(1, new BakerTradesLvl1());
//        baker_baker.addTrade(2, new BakerTradesLvl2());
//        baker_baker.addTrade(3, new BakerTradesLvl3());
//
//        miner_miner.addTrade(1, new MinerTradesLvl1());
//        miner_miner.addTrade(2, new MinerTradesLvl2());
//        miner_miner.addTrade(3, new MinerTradesLvl3());
    }

    public static ItemStack getDefaultHeldItem(VillagerProfession profession) {
//        if (profession == ProfessionsMCA.guard)
//            return career == ProfessionsMCA.guard_archer ? ItemStackCache.get(Items.BOW) : ItemStackCache.get(Items.IRON_SWORD);
//        else if (profession == ProfessionsMCA.bandit) return ItemStackCache.get(Items.IRON_SWORD);
        return ItemStack.EMPTY;
    }

    public static VillagerProfession randomProfession() {
//        ResourceLocation resource = null;
//        while (resource == null || resource.getPath().contains("nitwit") || inForbiddenProfessions(registry.getValue(resource))) {
//            int i = new Random().nextInt(registry.getKeys().size() - 1);
//            resource = (ResourceLocation) registry.getKeys().toArray()[i];
//        }
//        return registry.getValue(resource);
        return VillagerProfession.ARMORER;
    }

//    @Mod.EventBusSubscriber(Dist.CLIENT)
//    public static class RegistrationHandler {
//        @SubscribeEvent
//        public static void onEvent(final RegistryEvent.Register<VillagerProfession> event) {
//            registry = event.getRegistry();
//            registry.register(guard);
//            registry.register(bandit);
//            registry.register(child);
//            registry.register(baker);
//            registry.register(miner);
//        }
//    }

//    public static class BakerTradesLvl1 implements EntityVillager.ITradeList {
//
//        @Override
//        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.WHEAT, 6)));
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.MILK_BUCKET, 1), new ItemStack(Items.BREAD, 2)));
//        }
//    }
//
//    public static class BakerTradesLvl2 implements EntityVillager.ITradeList {
//
//        @Override
//        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.EGG, 12)));
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.SUGAR, 2), new ItemStack(Items.CAKE, 1)));
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.STONE_HOE, 1), new ItemStack(Items.WHEAT, 10)));
//        }
//    }
//
//    public static class BakerTradesLvl3 implements EntityVillager.ITradeList {
//
//        @Override
//        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.CAKE, 1), new ItemStack(Items.EMERALD, 5)));
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BREAD, 4)));
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.COOKIE, 6)));
//            recipeList.add(new MerchantRecipe(new ItemStack(Blocks.PUMPKIN, 1), new ItemStack(Items.PUMPKIN_PIE, 1)));
//        }
//    }
//
//    public static class MinerTradesLvl1 implements EntityVillager.ITradeList {
//
//        @Override
//        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
//            recipeList.add(new MerchantRecipe(new ItemStack(Blocks.COBBLESTONE, 8), new ItemStack(Blocks.STONE, 4)));
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 1), new ItemStack(Blocks.TORCH, 8)));
//        }
//    }
//
//    public static class MinerTradesLvl2 implements EntityVillager.ITradeList {
//
//        @Override
//        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.EMERALD, 2)));
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.COAL, 3)));
//        }
//    }
//
//    public static class MinerTradesLvl3 implements EntityVillager.ITradeList {
//
//        @Override
//        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 4), new ItemStack(Items.IRON_PICKAXE, 1)));
//            recipeList.add(new MerchantRecipe(new ItemStack(Blocks.EMERALD_BLOCK, 1), new ItemStack(Items.DIAMOND_PICKAXE, 1)));
//            recipeList.add(new MerchantRecipe(new ItemStack(Items.IRON_INGOT, 8), new ItemStack(Items.DIAMOND, 1)));
//        }
//    }

    private static boolean inForbiddenProfessions(VillagerProfession profIn) {
        for (VillagerProfession profession : FORBIDDEN_RANDOM_PROFESSIONS) {
            if (profession == profIn) {
                return true;
            }
        }
        return false;
    }
}
