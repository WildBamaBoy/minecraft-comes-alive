package mca.data;

import mca.core.MCA;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import net.minecraft.data.*;
import net.minecraft.data.server.RecipesProvider;
import net.minecraft.data.server.recipe.CookingRecipeJsonFactory;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonFactory;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import java.util.function.Consumer;

public class RecipeProviderMCA extends RecipesProvider {
    public RecipeProviderMCA(DataGenerator generatorIn) {
        super(generatorIn);
    }

    private static Identifier modId(String path) {
        return new Identifier(MCA.MOD_ID, path);
    }

    @Override
    protected void generate(Consumer<RecipeJsonProvider> consumer) {
        ShapelessRecipeJsonFactory.create(ItemsMCA.ROSE_GOLD_INGOT.get(), 9)
                .requires(BlocksMCA.ROSE_GOLD_BLOCK.get())
                .unlockedBy("has_item", conditionsFromTag(ItemsMCA.ROSE_GOLD_INGOT.get()))
                .save(consumer);

        ShapelessRecipeJsonFactory.create(ItemsMCA.ROSE_GOLD_DUST.get(), 1)
                .requires(ItemsMCA.ROSE_GOLD_INGOT.get())
                .unlockedBy("has_item", conditionsFromTag(ItemsMCA.ROSE_GOLD_DUST.get()))
                .save(consumer);

        ShapelessRecipeJsonFactory.create(ItemsMCA.GOLD_DUST.get(), 6)
                .requires(ItemsMCA.ROSE_GOLD_DUST.get())
                .requires(Items.WATER_BUCKET)
                .unlockedBy("has_item", conditionsFromTag(ItemsMCA.GOLD_DUST.get()))
                .save(consumer);

        ShapedRecipeJsonFactory.create(BlocksMCA.ROSE_GOLD_BLOCK.get())
                .define('I', ItemsMCA.ROSE_GOLD_INGOT.get())
                .pattern("III")
                .pattern("III")
                .pattern("III")
                .unlockedBy("has_item", conditionsFromTag(ItemsMCA.ROSE_GOLD_INGOT.get()))
                .save(consumer);

        CookingRecipeJsonFactory.createSmelting(Ingredient.fromTag(BlocksMCA.ROSE_GOLD_ORE.get()), ItemsMCA.ROSE_GOLD_INGOT.get(), 0.7f, 200)
                .unlockedBy("has_item", conditionsFromTag(BlocksMCA.ROSE_GOLD_ORE.get()))
                .save(consumer, modId("rose_gold_ingot_smelting"));

        CookingRecipeJsonFactory.createBlasting(Ingredient.fromTag(BlocksMCA.ROSE_GOLD_ORE.get()), ItemsMCA.ROSE_GOLD_INGOT.get(), 0.7f, 100)
                .unlockedBy("has_item", conditionsFromTag(BlocksMCA.ROSE_GOLD_ORE.get()))
                .save(consumer, modId("rose_gold_ingot_blasting"));
    }
}
