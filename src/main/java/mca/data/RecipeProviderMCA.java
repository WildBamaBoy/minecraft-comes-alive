package mca.data;

import mca.core.MCA;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class RecipeProviderMCA extends RecipeProvider {
    public RecipeProviderMCA(DataGenerator generatorIn) {
        super(generatorIn);
    }

    private static ResourceLocation modId(String path) {
        return new ResourceLocation(MCA.MOD_ID, path);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {

        ShapelessRecipeBuilder.shapeless(ItemsMCA.ROSE_GOLD_INGOT.get(), 9)
                .requires(BlocksMCA.ROSE_GOLD_BLOCK.get())
                .unlockedBy("has_item", has(ItemsMCA.ROSE_GOLD_INGOT.get()))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(ItemsMCA.ROSE_GOLD_DUST.get(), 1)
                .requires(ItemsMCA.ROSE_GOLD_INGOT.get())
                .unlockedBy("has_item", has(ItemsMCA.ROSE_GOLD_DUST.get()))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(ItemsMCA.GOLD_DUST.get(), 6)
                .requires(ItemsMCA.ROSE_GOLD_DUST.get())
                .requires(Items.WATER_BUCKET)
                .unlockedBy("has_item", has(ItemsMCA.GOLD_DUST.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(BlocksMCA.ROSE_GOLD_BLOCK.get())
                .define('I', ItemsMCA.ROSE_GOLD_INGOT.get())
                .pattern("III")
                .pattern("III")
                .pattern("III")
                .unlockedBy("has_item", has(ItemsMCA.ROSE_GOLD_INGOT.get()))
                .save(consumer);

        CookingRecipeBuilder.smelting(Ingredient.of(BlocksMCA.ROSE_GOLD_ORE.get()), ItemsMCA.ROSE_GOLD_INGOT.get(), 0.7f, 200)
                .unlockedBy("has_item", has(BlocksMCA.ROSE_GOLD_ORE.get()))
                .save(consumer, modId("rose_gold_ingot_smelting"));
        CookingRecipeBuilder.blasting(Ingredient.of(BlocksMCA.ROSE_GOLD_ORE.get()), ItemsMCA.ROSE_GOLD_INGOT.get(), 0.7f, 100)
                .unlockedBy("has_item", has(BlocksMCA.ROSE_GOLD_ORE.get()))
                .save(consumer, modId("rose_gold_ingot_blasting"));
    }
}
