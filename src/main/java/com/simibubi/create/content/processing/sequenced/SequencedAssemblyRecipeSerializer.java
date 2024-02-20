package com.simibubi.create.content.processing.sequenced;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.CodecWrapper;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SequencedAssemblyRecipeSerializer implements RecipeSerializer<SequencedAssemblyRecipe> {
	public static final Codec<SequencedAssemblyRecipe> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
					Ingredient.CODEC.fieldOf("ingredient").forGetter(s -> s.ingredient),
					ProcessingOutput.CODEC.fieldOf("transitionalItem").forGetter(s -> s.transitionalItem),
					Codec.list((Codec<ProcessingRecipe>) BuiltInRegistries.RECIPE_SERIALIZER
									.byNameCodec()
									.dispatch(Recipe::getSerializer, RecipeSerializer::codec)
							)
							.fieldOf("sequence")
							.forGetter(s -> s.sequence.stream().map(x -> (ProcessingRecipe) x.getRecipe()).toList()),
					Codec.list(ProcessingOutput.CODEC).fieldOf("resultPool").orElse(List.of()).forGetter(s -> s.resultPool),
					Codec.INT.fieldOf("loops").forGetter(s -> s.loops)
			)
			.apply(instance, (ingrdient, transitionalItem, sequence, resultPool, loops) -> {
				var recipe = new SequencedAssemblyRecipe(new SequencedAssemblyRecipeSerializer());
				recipe.ingredient = ingrdient;
				recipe.transitionalItem = transitionalItem;
				recipe.sequence = new ArrayList<>();
				recipe.sequence.addAll(sequence.stream().map(SequencedRecipe::new).toList());
				recipe.resultPool.addAll(resultPool);
				recipe.loops = loops;
				return recipe;
			}));

	protected void writeToJson(JsonObject json, SequencedAssemblyRecipe recipe) {
		JsonElement x = CODEC.encodeStart(JsonOps.INSTANCE, recipe).getOrThrow(false, Create.LOGGER::error);

		for (Map.Entry<String, JsonElement> entry : x.getAsJsonObject().entrySet()) {
			json.add(entry.getKey(), entry.getValue());
		}
	}

	/*protected SequencedAssemblyRecipe readFromJson(ResourceLocation recipeId, JsonObject json) {
		SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(recipeId, this);
		recipe.ingredient = Ingredient.fromJson(json.get("ingredient"));
		recipe.transitionalItem = ProcessingOutput.deserialize(GsonHelper.getAsJsonObject(json, "transitionalItem"));
		int i = 0;
		for (JsonElement je : GsonHelper.getAsJsonArray(json, "sequence"))
			recipe.getSequence().add(SequencedRecipe.fromJson(je.getAsJsonObject(), recipe, i++));
		for (JsonElement je : GsonHelper.getAsJsonArray(json, "results"))
			recipe.resultPool.add(ProcessingOutput.deserialize(je));
		if (GsonHelper.isValidNode(json, "loops"))
			recipe.loops = GsonHelper.getAsInt(json, "loops");
		return recipe;
	}*/

	protected void writeToBuffer(FriendlyByteBuf buffer, SequencedAssemblyRecipe recipe) {
		recipe.getIngredient().toNetwork(buffer);
		buffer.writeVarInt(recipe.getSequence().size());
		recipe.getSequence().forEach(sr -> sr.writeToBuffer(buffer));
		buffer.writeVarInt(recipe.resultPool.size());
		recipe.resultPool.forEach(sr -> sr.write(buffer));
		recipe.transitionalItem.write(buffer);
		buffer.writeInt(recipe.loops);
	}

	protected SequencedAssemblyRecipe readFromBuffer(FriendlyByteBuf buffer) {
		SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(this);
		recipe.ingredient = Ingredient.fromNetwork(buffer);
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			recipe.getSequence().add(SequencedRecipe.readFromBuffer(buffer));
		size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			recipe.resultPool.add(ProcessingOutput.read(buffer));
		recipe.transitionalItem = ProcessingOutput.read(buffer);
		recipe.loops = buffer.readInt();
		return recipe;
	}

	public final void write(JsonObject json, SequencedAssemblyRecipe recipe) {
		writeToJson(json, recipe);
	}

	/*@Override
	public final SequencedAssemblyRecipe fromJson(ResourceLocation id, JsonObject json) {
		return readFromJson(id, json);
	}*/

	@Override
	public final void toNetwork(FriendlyByteBuf buffer, SequencedAssemblyRecipe recipe) {
		writeToBuffer(buffer, recipe);
	}

	@Override
	public Codec<SequencedAssemblyRecipe> codec() {
		return new CodecWrapper<>(CODEC);
	}

	@Override
	public final SequencedAssemblyRecipe fromNetwork(FriendlyByteBuf buffer) {
		return readFromBuffer(buffer);
	}
}
