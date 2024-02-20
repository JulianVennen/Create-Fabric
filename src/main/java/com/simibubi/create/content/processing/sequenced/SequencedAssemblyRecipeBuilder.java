package com.simibubi.create.content.processing.sequenced;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeFactory;

import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.Nullable;

public class SequencedAssemblyRecipeBuilder {

	private SequencedAssemblyRecipe recipe;
	protected List<ConditionJsonProvider> recipeConditions;

	public SequencedAssemblyRecipeBuilder() {
		recipeConditions = new ArrayList<>();
		this.recipe = new SequencedAssemblyRecipe(AllRecipeTypes.SEQUENCED_ASSEMBLY.getSerializer());
	}

	public <T extends ProcessingRecipe<?>> SequencedAssemblyRecipeBuilder addStep(ProcessingRecipeFactory<T> factory,
		UnaryOperator<ProcessingRecipeBuilder<T>> builder) {
		ProcessingRecipeBuilder<T> recipeBuilder = new ProcessingRecipeBuilder<>(factory);
		Item placeHolder = recipe.getTransitionalItem()
			.getItem();
		recipe.getSequence().add(new SequencedRecipe<>(builder.apply(recipeBuilder
						.require(placeHolder)
						.output(placeHolder))
						.build()
		));
		return this;
	}

	public SequencedAssemblyRecipeBuilder require(ItemLike ingredient) {
		return require(Ingredient.of(ingredient));
	}

	public SequencedAssemblyRecipeBuilder require(TagKey<Item> tag) {
		return require(Ingredient.of(tag));
	}

	public SequencedAssemblyRecipeBuilder require(Ingredient ingredient) {
		recipe.ingredient = ingredient;
		return this;
	}

	public SequencedAssemblyRecipeBuilder transitionTo(ItemLike item) {
		recipe.transitionalItem = new ProcessingOutput(new ItemStack(item), 1);
		return this;
	}

	public SequencedAssemblyRecipeBuilder loops(int loops) {
		recipe.loops = loops;
		return this;
	}

	public SequencedAssemblyRecipeBuilder addOutput(ItemLike item, float weight) {
		return addOutput(new ItemStack(item), weight);
	}

	public SequencedAssemblyRecipeBuilder addOutput(ItemStack item, float weight) {
		recipe.resultPool.add(new ProcessingOutput(item, weight));
		return this;
	}

	public SequencedAssemblyRecipe build() {
		return recipe;
	}

	public void build(RecipeOutput consumer, ResourceLocation id) {
		consumer.accept(new DataGenResult(new RecipeHolder<>(id, build()), recipeConditions));
	}

	public static class DataGenResult implements FinishedRecipe {

		private SequencedAssemblyRecipe recipe;
		private List<ConditionJsonProvider> recipeConditions;
		private ResourceLocation id;
		private SequencedAssemblyRecipeSerializer serializer;

		public DataGenResult(RecipeHolder<SequencedAssemblyRecipe> recipe, List<ConditionJsonProvider> recipeConditions) {
			this.recipeConditions = recipeConditions;
			this.recipe = recipe.value();
			this.id = new ResourceLocation(recipe.id().getNamespace(),
					AllRecipeTypes.SEQUENCED_ASSEMBLY.getId().getPath() + "/" + recipe.id().getPath());
			this.serializer = (SequencedAssemblyRecipeSerializer) recipe.value().getSerializer();
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			serializer.write(json, recipe);
			if (recipeConditions.isEmpty())
				return;

			JsonArray conds = new JsonArray();
			recipeConditions.forEach(c -> conds.add(c.toJson()));
			json.add(ResourceConditions.CONDITIONS_KEY, conds);
		}

		@Override
		public ResourceLocation id() {
			return id;
		}

		@Override
		public RecipeSerializer<?> type() {
			return serializer;
		}

		@Nullable
		@Override
		public AdvancementHolder advancement() {
			return null;
		}
	}

}
