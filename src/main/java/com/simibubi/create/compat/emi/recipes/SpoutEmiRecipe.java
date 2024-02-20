package com.simibubi.create.compat.emi.recipes;

import java.util.List;

import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.widget.WidgetHolder;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class SpoutEmiRecipe extends CreateEmiRecipe<FillingRecipe> {

	public SpoutEmiRecipe(RecipeHolder<FillingRecipe> recipe) {
		super(CreateEmiPlugin.SPOUT_FILLING, recipe, 134, 74, c -> {});
		input = List.of(
				firstIngredientOrEmpty(recipe.value().getIngredients()),
				firstFluidOrEmpty(recipe.value().getRequiredFluid().getMatchingFluidStacks())
		);
		output = List.of(firstResultOrEmpty(recipe.value().getRollableResults()));
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 40, 57);
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 104, 29);

		addSlot(widgets, input.get(1), 4, 31);
		addSlot(widgets, input.get(0), 4, 50);

		addSlot(widgets, output.get(0), 109, 50).recipeContext(this);

		List<FluidStack> fluids = recipe.value().getRequiredFluid().getMatchingFluidStacks();
		CreateEmiAnimations.addSpout(widgets, widgets.getWidth() / 2 - 13, 22, fluids.isEmpty() ? List.of(FluidStack.EMPTY) : fluids);
	}
}
