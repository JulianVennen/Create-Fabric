package com.simibubi.create.content.processing.recipe;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeFactory;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ProcessingRecipeSerializer<T extends ProcessingRecipe<?>> implements RecipeSerializer<T> {

	private final ProcessingRecipeFactory<T> factory;
	private final Codec<T> codec;

	public ProcessingRecipeSerializer(ProcessingRecipeFactory<T> factory) {
		this.factory = factory;
		this.codec = RecordCodecBuilder.create((instance) -> instance.group(
						Codec.INT.optionalFieldOf("processingTime").forGetter(s -> {
							if (s.processingDuration == 0)
								return Optional.empty();
							return Optional.of(s.processingDuration);
						}),
						Codec.STRING.optionalFieldOf("heatRequirement").forGetter(s -> {
                            if (s.requiredHeat == HeatCondition.NONE)
								return Optional.empty();
                            return Optional.of(s.requiredHeat.serialize());
                        }),
						Codec.list(Ingredient.CODEC).fieldOf("ingredients").forGetter(T::getIngredients),
						Codec.list(ProcessingOutput.CODEC).fieldOf("results").forGetter(s -> s.results),
						Codec.list(FluidIngredient.CODEC()).optionalFieldOf("fluidIngredients").forGetter(s -> {
							if (s.fluidIngredients.isEmpty())
								return Optional.empty();
							return Optional.of(s.fluidIngredients);
						}),
						Codec.list(FluidStack.CODEC).optionalFieldOf("fluidResults").forGetter(s -> {
							if (s.fluidResults.isEmpty())
								return Optional.empty();
							return Optional.of(s.fluidResults);
						})
				)
				.apply(instance, (processingDuration, requiredHeat, ingredients, results, fluidIngredients, fluidResults) -> {
					NonNullList<Ingredient> itemIngredients = NonNullList.create();
					itemIngredients.addAll(ingredients);

					NonNullList<FluidIngredient> fluidIngredientsList = NonNullList.create();
					fluidIngredients.ifPresent(fluidIngredientsList::addAll);

					NonNullList<FluidStack> fluidOutputs = NonNullList.create();
					fluidResults.ifPresent(fluidOutputs::addAll);

					NonNullList<ProcessingOutput> itemOutputs = NonNullList.create();
					itemOutputs.addAll(results);

					return new ProcessingRecipeBuilder<>(factory)
							.withItemIngredients(itemIngredients)
							.withItemOutputs(itemOutputs)
							.withFluidIngredients(fluidIngredientsList)
							.withFluidOutputs(fluidOutputs)
							.duration(processingDuration.orElse(0))
							.requiresHeat(requiredHeat.map(HeatCondition::deserialize).orElse(HeatCondition.NONE))
							.build();
				}));
	}


	@Override
	public Codec<T> codec() {
		return new CodecWrapper<>(codec);
	}

	@Override
	public T fromNetwork(FriendlyByteBuf buffer) {
		return readFromBuffer(buffer);
	}

	@Override
	public final void toNetwork(FriendlyByteBuf buffer, T recipe) {
		writeToBuffer(buffer, recipe);
	}

	protected void writeToBuffer(FriendlyByteBuf buffer, T recipe) {
		NonNullList<Ingredient> ingredients = recipe.ingredients;
		NonNullList<FluidIngredient> fluidIngredients = recipe.fluidIngredients;
		NonNullList<ProcessingOutput> outputs = recipe.results;
		NonNullList<FluidStack> fluidOutputs = recipe.fluidResults;

		buffer.writeVarInt(ingredients.size());
		ingredients.forEach(i -> i.toNetwork(buffer));
		buffer.writeVarInt(fluidIngredients.size());
		fluidIngredients.forEach(i -> i.write(buffer));

		buffer.writeVarInt(outputs.size());
		outputs.forEach(o -> o.write(buffer));
		buffer.writeVarInt(fluidOutputs.size());
		fluidOutputs.forEach(o -> o.writeToPacket(buffer));

		buffer.writeVarInt(recipe.getProcessingDuration());
		buffer.writeVarInt(recipe.getRequiredHeat().ordinal());

		recipe.writeAdditional(buffer);
	}

	protected T readFromBuffer(FriendlyByteBuf buffer) {
		NonNullList<Ingredient> ingredients = NonNullList.create();
		NonNullList<FluidIngredient> fluidIngredients = NonNullList.create();
		NonNullList<ProcessingOutput> results = NonNullList.create();
		NonNullList<FluidStack> fluidResults = NonNullList.create();

		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			ingredients.add(Ingredient.fromNetwork(buffer));

		size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			fluidIngredients.add(FluidIngredient.read(buffer));

		size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			results.add(ProcessingOutput.read(buffer));

		size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			fluidResults.add(FluidStack.readFromPacket(buffer));

		T recipe = new ProcessingRecipeBuilder<>(factory).withItemIngredients(ingredients)
				.withItemOutputs(results)
				.withFluidIngredients(fluidIngredients)
				.withFluidOutputs(fluidResults)
				.duration(buffer.readVarInt())
				.requiresHeat(HeatCondition.values()[buffer.readVarInt()])
				.build();
		recipe.readAdditional(buffer);
		return recipe;
	}

	public final void write(JsonObject json, T recipe) {
		JsonElement x = codec.encodeStart(JsonOps.INSTANCE, recipe).getOrThrow(false, Create.LOGGER::error);

		for (Map.Entry<String, JsonElement> entry : x.getAsJsonObject().entrySet()) {
			json.add(entry.getKey(), entry.getValue());
		}

		recipe.writeAdditional(json);
	}

	public ProcessingRecipeFactory<T> getFactory() {
		return factory;
	}

}
