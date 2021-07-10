package mca.core.forge;

import mca.core.MCA;
import mca.crafting.recipe.PressingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraftforge.fml.RegistryObject;

public final class RecipesMCA {
    private RecipesMCA() {
    }

    static void register() {
    }

    public static final class Types {
        public static final RecipeType<PressingRecipe> PRESSING = RecipeType.register(
                MCA.MOD_ID + ":pressing");

        private Types() {
        }
    }

    public static final class Serializers {
        public static final RegistryObject<RecipeSerializer<?>> PRESSING = Registration.RECIPE_SERIALIZERS.register(
                "pressing", PressingRecipe.Serializer::new);

        private Serializers() {
        }
    }
}
