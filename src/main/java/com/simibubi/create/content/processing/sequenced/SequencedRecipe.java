package com.simibubi.create.content.processing.sequenced;

import java.util.Arrays;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SequencedRecipe<T extends ProcessingRecipe<?>> {
	private T wrapped;

	public SequencedRecipe(T wrapped) {
		this.wrapped = wrapped;
	}

	public IAssemblyRecipe getAsAssemblyRecipe() {
		return (IAssemblyRecipe) wrapped;
	}

	public ProcessingRecipe<?> getRecipe() {
		return wrapped;
	}

	public JsonObject toJson() {
		@SuppressWarnings("unchecked")
		ProcessingRecipeSerializer<T> serializer = (ProcessingRecipeSerializer<T>) wrapped.getSerializer();
		JsonObject json = new JsonObject();
		json.addProperty("type", RegisteredObjects.getKeyOrThrow(serializer)
			.toString());
		serializer.write(json, wrapped);
		return json;
	}

	public static SequencedRecipe<?> fromJson(JsonObject json, SequencedAssemblyRecipe parent, int index, ResourceLocation parentId) {
		RecipeHolder<?> recipe = RecipeManager.fromJson(new ResourceLocation(parentId.getNamespace(), parentId.getPath() + "_step_" + index), json);
		if (recipe.value() instanceof ProcessingRecipe<?> processingRecipe &&
				recipe.value() instanceof IAssemblyRecipe assemblyRecipe) {
            if (assemblyRecipe.supportsAssembly()) {
				Ingredient transit = Ingredient.of(parent.getTransitionalItem());

				processingRecipe.getIngredients()
					.set(0, index == 0 ? Ingredient.fromValues(ImmutableList.of(transit, parent.getIngredient()).stream().flatMap(i -> Arrays.stream(i.values))) : transit);
                return new SequencedRecipe<>(processingRecipe);
			}
		}
		throw new JsonParseException("Not a supported recipe type");
	}

	public void writeToBuffer(FriendlyByteBuf buffer) {
		@SuppressWarnings("unchecked")
		ProcessingRecipeSerializer<T> serializer = (ProcessingRecipeSerializer<T>) wrapped.getSerializer();
		buffer.writeResourceLocation(RegisteredObjects.getKeyOrThrow(serializer));
		serializer.toNetwork(buffer, wrapped);
	}

	public static SequencedRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
		ResourceLocation serializerLocation = buffer.readResourceLocation();
		RecipeSerializer<?> serializer = BuiltInRegistries.RECIPE_SERIALIZER.get(serializerLocation);
		if (!(serializer instanceof ProcessingRecipeSerializer))
			throw new JsonParseException("Not a supported recipe type");
		@SuppressWarnings("rawtypes")
		ProcessingRecipe recipe = (ProcessingRecipe) serializer.fromNetwork(buffer);
		return new SequencedRecipe<>(recipe);
	}

}
