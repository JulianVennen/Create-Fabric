package com.simibubi.create.compat.emi.recipes.basin;

import com.simibubi.create.content.processing.basin.BasinRecipe;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ShapelessEmiRecipe extends MixingEmiRecipe {

	public ShapelessEmiRecipe(EmiRecipeCategory category, RecipeHolder<BasinRecipe> recipe) {
		super(category, recipe);
		ResourceLocation id = recipe.id();
		this.id = new ResourceLocation ("emi", "create/shapeless/" + id.getNamespace() + "/" + id.getPath());
	}
}
