package mca.core.minecraft;

import mca.core.MCA;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraftforge.fml.RegistryObject;

import java.util.*;

public class ProfessionsMCA {
    // as set of invalid professions
    private static final List<VillagerProfession> PROFESSIONS;

    static {
        Set<RegistryObject<VillagerProfession>> forbiddenProfessions = new HashSet<>(Arrays.asList(
                MCA.PROFESSION_CHILD
                //MCA.PROFESSION_BANDIT
        ));

        // add our professions
        PROFESSIONS = new ArrayList<>();
        MCA.getMod().villagerProfessionRegistry.getEntries().forEach((r) -> {
            if (!forbiddenProfessions.contains(r)) {
                PROFESSIONS.add(r.get());
            }
        });

        // add vanilla professions
        PROFESSIONS.addAll(Arrays.asList(
                VillagerProfession.ARMORER,
                VillagerProfession.BUTCHER,
                VillagerProfession.CARTOGRAPHER,
                VillagerProfession.CLERIC,
                VillagerProfession.FARMER,
                VillagerProfession.FISHERMAN,
                VillagerProfession.FLETCHER,
                VillagerProfession.LEATHERWORKER,
                VillagerProfession.LIBRARIAN,
                VillagerProfession.MASON,
                VillagerProfession.NITWIT
        ));
    }

    public static VillagerProfession randomProfession() {
        Random r = new Random();
        return PROFESSIONS.get(r.nextInt(PROFESSIONS.size()));
    }

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
}
