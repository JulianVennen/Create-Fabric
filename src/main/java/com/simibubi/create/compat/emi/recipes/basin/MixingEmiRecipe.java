package com.simibubi.create.compat.emi.recipes.basin;

import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MixingEmiRecipe<T extends BasinRecipe> extends BasinEmiRecipe<T> {

	public MixingEmiRecipe(EmiRecipeCategory category, RecipeHolder<T> recipe) {
		super(category, recipe, category != CreateEmiPlugin.AUTOMATIC_SHAPELESS);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		super.addWidgets(widgets);

		HeatCondition requiredHeat = recipe.value().getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE) {
			CreateEmiAnimations.addBlazeBurner(widgets, widgets.getWidth() / 2 + 3, 55, requiredHeat.visualizeAsBlazeBurner());
		}
		CreateEmiAnimations.addMixer(widgets, widgets.getWidth() / 2 + 3, 40);
	}
}
