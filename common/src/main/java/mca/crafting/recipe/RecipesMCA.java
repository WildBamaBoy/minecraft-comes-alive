package mca.crafting.recipe;

import mca.MCA;
import mca.cobalt.registration.Registration;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface RecipesMCA {
    RecipeType<PressingRecipe> PRESSING = type("pressing");
    RecipeSerializer<?> PRESSING_SERIALIZER = serializer("pressing", new PressingRecipe.Serializer());

    static void bootstrap() { }

    static <T extends Recipe<?>> RecipeType<T> type(String name) {
        Identifier id = new Identifier(MCA.MOD_ID, name);
        return Registration.register(Registry.RECIPE_TYPE, id, new RecipeType<T>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    static <T extends RecipeSerializer<?>> T serializer(String name, T obj) {
        return Registration.register(Registry.RECIPE_SERIALIZER, new Identifier(MCA.MOD_ID, name), obj);
    }
}
