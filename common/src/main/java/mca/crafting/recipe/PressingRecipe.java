package mca.crafting.recipe;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class PressingRecipe extends CuttingRecipe {

    public PressingRecipe(Identifier id, Ingredient ingredient, ItemStack result) {
        super(RecipesMCA.PRESSING, RecipesMCA.PRESSING_SERIALIZER, id, "", ingredient, result);
    }

    @Override
    public boolean matches(Inventory inv, World world) {
        return this.input.test(inv.getStack(0));
    }

    public static class Serializer extends SpecialRecipeSerializer<PressingRecipe> {
        public Serializer() {
            super(id -> new PressingRecipe(id, Ingredient.EMPTY, ItemStack.EMPTY));
        }

        @Override
        public PressingRecipe read(Identifier id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            Identifier itemId = new Identifier(JsonHelper.getString(json, "result"));
            int count = JsonHelper.getInt(json, "count", 1);

            ItemStack result = new ItemStack(Registry.ITEM.get(itemId), count);

            return new PressingRecipe(id, ingredient, result);
        }

        @Nullable
        @Override
        public PressingRecipe read(Identifier id, PacketByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromPacket(buffer);
            ItemStack result = buffer.readItemStack();
            return new PressingRecipe(id, ingredient, result);
        }

        @Override
        public void write(PacketByteBuf buffer, PressingRecipe recipe) {
            recipe.input.write(buffer);
            buffer.writeItemStack(recipe.output);
        }
    }
}
