package com.simibubi.create.compat.emi.recipes.basin;

import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PackingEmiRecipe extends BasinEmiRecipe<BasinRecipe> {

	public PackingEmiRecipe(RecipeHolder<BasinRecipe> recipe) {
		super(CreateEmiPlugin.PACKING, recipe, false);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		super.addWidgets(widgets);

		HeatCondition requiredHeat = recipe.value().getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE) {
			CreateEmiAnimations.addBlazeBurner(widgets, widgets.getWidth() / 2 + 3, 55, requiredHeat.visualizeAsBlazeBurner());
		}
		CreateEmiAnimations.addPress(widgets, widgets.getWidth() / 2 + 3, 40, true);
	}
}
