package mca.core.minecraft;

import mca.core.MCA;
import mca.crafting.recipe.PressingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;

public interface RecipesMCA {
    RecipeType<PressingRecipe> PRESSING = RecipeType.register(MCA.MOD_ID + ":pressing");
    RecipeSerializer<?> PRESSING_SERIALIZER = RecipeSerializer.register(MCA.MOD_ID + ":pressing", new PressingRecipe.Serializer());

    static void bootstrap() { }
}
