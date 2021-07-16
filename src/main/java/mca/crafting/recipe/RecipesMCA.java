package mca.crafting.recipe;

import mca.MCA;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;

public interface RecipesMCA {
    RecipeType<PressingRecipe> PRESSING = RecipeType.register(MCA.MOD_ID + ":pressing");
    RecipeSerializer<?> PRESSING_SERIALIZER = RecipeSerializer.register(MCA.MOD_ID + ":pressing", new PressingRecipe.Serializer());

    static void bootstrap() { }
}
