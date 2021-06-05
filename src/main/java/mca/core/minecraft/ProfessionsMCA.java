package mca.core.minecraft;

import com.google.common.collect.ImmutableSet;
import mca.core.MCA;
import mca.core.forge.PointOfInterestTypeMCA;
import mca.core.forge.Registration;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.SoundEvents;
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
                //ProfessionsMCA.JEWELER.get()
        ));
    }

    public static VillagerProfession randomProfession() {
        Random r = new Random();
        return PROFESSIONS.get(r.nextInt(PROFESSIONS.size()));
    }

//TODO BlocksMCA.JEWELER_WORKBENCH
//    public static final RegistryObject<VillagerProfession> JEWELER = Registration.PROFESSIONS.register("jeweler", ()->
//            new VillagerProfession("jeweler", PointOfInterestTypeMCA.JEWELER.get(), ImmutableSet.of(), ImmutableSet.of(BlocksMCA.JEWELER_WORKBENCH.get()), SoundEvents.VILLAGER_WORK_TOOLSMITH));


    public static void register() { }



    /*private static <E> RegistryObject<VillagerProfession> register(String name,  TileEntityTypesMCA jProfessionType, ImmutableSet immutable_1_, ImmutableSet immutable_2_, SoundEvent sound) {
        return Registration.POI_TYPES.register( name, ()->{
            return new VillagerProfession(name, jProfessionType, immutable_1_, immutable_2_, sound); });
    }*/

    /*
    static VillagerProfession register(String name, PointOfInterestType type, @Nullable SoundEvent sound) {
        return register(name, type,  ImmutableSet.of(), ImmutableSet.of(), sound);
    }

    public static ProfessionsMCA createNew(String name, PointOfInterestType poiType, SoundEvent sound) {
        // Creating a new villager profession is private to VillagerProfession. Don't know whose bright idea that
        // was, either Mojang or Forge. Either way, doesn't matter to us, we'll crack it open by reflection.
        try {
            Constructor<VillagerProfession> constructor = VillagerProfession.class.getDeclaredConstructor(String.class, PointOfInterestType.class, ImmutableSet.class, ImmutableSet.class, SoundEvent.class);
            constructor.setAccessible(true);
            return fromMC(constructor.newInstance(name, poiType, ImmutableSet.of(), ImmutableSet.of(), sound));
        } catch (Exception e) {
            Cobalt.getLog().fatal("Unable to create new profession!", e);
            return null;
        }
    }*/
    //public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, MCA.MOD_ID);


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
