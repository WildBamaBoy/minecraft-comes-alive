package mca.core.forge;

import mca.core.MCA;
import mca.crafting.recipe.PressingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraftforge.fml.RegistryObject;

public final class RecipesMCA {
    private RecipesMCA() {
    }

    static void register() {
    }

    public static final class Types {
        public static final IRecipeType<PressingRecipe> PRESSING = IRecipeType.register(
                MCA.MOD_ID + ":pressing");

        private Types() {
        }
    }

    public static final class Serializers {
        public static final RegistryObject<IRecipeSerializer<?>> PRESSING = Registration.RECIPE_SERIALIZERS.register(
                "pressing", PressingRecipe.Serializer::new);

        private Serializers() {
        }
    }
}
