package com.teammetallurgy.atum.api.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public abstract class RotationRecipe<C extends Container> extends AbstractAtumRecipe<C> {
    protected final int rotations;
    protected final int inputCount;

    public RotationRecipe(RecipeType<?> recipeType, ResourceLocation id, Ingredient input, @Nonnull ItemStack output, int rotations) {
        this(recipeType, id, input, output, rotations, 0);
    }

    protected RotationRecipe(RecipeType<?> recipeType, ResourceLocation id, Ingredient input, ItemStack output, int rotations, int inputCount) {
        super(recipeType, id, input, output);
        this.rotations = rotations;
        this.inputCount = inputCount;
    }

    public int getInputCount(ItemStack ingredientStack) {
        return this.inputCount > 0 ? this.inputCount : ingredientStack.getCount();
    }

    public int getRotations() {
        return this.rotations;
    }

    public static class Serializer<C extends RotationRecipe<? extends Container>> implements RecipeSerializer<C> {
        private final CountedFactory<C> factory;
        private final boolean inputCanHaveCount;

        public Serializer(Serializer.IFactory<C> factory, boolean inputCanHaveCount) {
            this((id, input, output, rotations, inputCount) -> factory.create(id, input, output, rotations), inputCanHaveCount);
        }

        public Serializer(CountedFactory<C> factory) {
            this(factory, true);
        }

        private Serializer(CountedFactory<C> factory, boolean inputCanHaveCount) {
            this.factory = factory;
            this.inputCanHaveCount = inputCanHaveCount;
        }

        @Override
        @Nonnull
        public C fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
            JsonObject inputObject = GsonHelper.getAsJsonObject(json, "ingredient");
            Ingredient input = this.inputCanHaveCount && inputObject.has("item") && !inputObject.has("type")
                    ? Ingredient.of(CraftingHelper.getItemStack(inputObject, true).copyWithCount(1))
                    : Ingredient.fromJson(inputObject);
            int inputCount = this.inputCanHaveCount ? GsonHelper.getAsInt(inputObject, "count", 1) : 1;
            if (inputCount < 1) {
                throw new JsonSyntaxException("Recipe ingredient count must be at least one");
            }

            if (!json.has("result")) {
                throw new JsonSyntaxException("Missing result, expected to find a string or object");
            }
            ItemStack output;
            if (json.get("result").isJsonObject()) {
                output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            } else {
                String result = GsonHelper.getAsString(json, "result");
                ResourceLocation resultID = new ResourceLocation(result);
                output = new ItemStack(ForgeRegistries.ITEMS.getValue(resultID));
            }
            int rotations = GsonHelper.getAsInt(json, "rotations", 0);
            return this.factory.create(id, input, output, rotations, inputCount);
        }

        @Override
        public C fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack stack = buffer.readItem();
            int rotations = buffer.readInt();
            return this.factory.create(id, ingredient, stack, rotations, buffer.readVarInt());
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buffer, C recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeInt(recipe.rotations);
            buffer.writeVarInt(recipe.inputCount);
        }

        public interface IFactory<T extends RotationRecipe<? extends Container>> {
            T create(ResourceLocation id, Ingredient input, @Nonnull ItemStack output, int rotations);
        }

        public interface CountedFactory<T extends RotationRecipe<? extends Container>> {
            T create(ResourceLocation id, Ingredient input, @Nonnull ItemStack output, int rotations, int inputCount);
        }
    }
}
