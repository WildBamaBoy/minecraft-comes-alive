package mca.crafting.recipe;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import mca.core.minecraft.RecipesMCA;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class PressingRecipe extends CuttingRecipe {

    public PressingRecipe(Identifier recipeId, Ingredient ingredient, ItemStack result) {
        super(RecipesMCA.PRESSING, RecipesMCA.PRESSING_SERIALIZER, recipeId, "", ingredient, result);
    }

    @Override
    public boolean matches(Inventory inv, World world) {
        return this.input.test(inv.getStack(0));
    }

    public static class Serializer implements RecipeSerializer<PressingRecipe> {
        @Override
        public PressingRecipe read(Identifier recipeId, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            Identifier itemId = new Identifier(JsonHelper.getString(json, "result"));
            int count = JsonHelper.getInt(json, "count", 1);

            ItemStack result = new ItemStack(Registry.ITEM.get(itemId), count);

            return new PressingRecipe(recipeId, ingredient, result);
        }

        @Nullable
        @Override
        public PressingRecipe read(Identifier recipeId, PacketByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromPacket(buffer);
            ItemStack result = buffer.readItemStack();
            return new PressingRecipe(recipeId, ingredient, result);
        }

        @Override
        public void write(PacketByteBuf buffer, PressingRecipe recipe) {
            recipe.input.write(buffer);
            buffer.writeItemStack(recipe.output);
        }
    }
}
