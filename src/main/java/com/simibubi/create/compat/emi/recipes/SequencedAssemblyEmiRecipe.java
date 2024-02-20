package com.simibubi.create.compat.emi.recipes;

import java.util.List;

import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.compat.emi.EmiSequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.crafting.RecipeHolder;

public class SequencedAssemblyEmiRecipe extends CreateEmiRecipe<SequencedAssemblyRecipe> {
	public static final String[] ROMAN = {
		"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX",
		"X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX",
		"XX", "XXI", "XXII", "XXIII", "XXIV", "XXV", "XXVI", "XXVII", "XXVIII", "XXIX", "-" };
	private final int margin = 3;
	private int width;

	public SequencedAssemblyEmiRecipe(RecipeHolder<SequencedAssemblyRecipe> recipe) {
		super(CreateEmiPlugin.SEQUENCED_ASSEMBLY, recipe, 180, 120);
		for (SequencedRecipe<?> r : recipe.value().getSequence()) {
			width += getSubCategory(r).getWidth() + margin;
		}
		width -= margin;
	}

	@Override
	public int getDisplayWidth() {
		return Math.max(150, width);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		int xOff = recipe.value().getOutputChance() == 1 ? 0 : -7;
		int mid = widgets.getWidth() / 2;

		addTexture(widgets, AllGuiTextures.JEI_LONG_ARROW, mid - 38 + xOff, 94);

		widgets.addDrawable(mid - 38 + xOff, 94, AllGuiTextures.JEI_LONG_ARROW.width, 20, (matrices, mx, my, delta) -> {})
			.tooltip((mouseX, mouseY) -> List.of(ClientTooltipComponent.create(
				Lang.translateDirect("recipe.assembly.repeat", recipe.value().getLoops()).getVisualOrderText())));

		if (recipe.value().getOutputChance() != 1) {
			float chance = recipe.value().getOutputChance();
			addTexture(widgets, AllGuiTextures.JEI_CHANCE_SLOT, mid + 60 + xOff, 90)
				.tooltip((mouseX, mouseY) -> List.of(
					ClientTooltipComponent.create(Lang.translateDirect("recipe.assembly.junk").getVisualOrderText()),
					ClientTooltipComponent.create(Components.translatable("tooltip.emi.chance.produce", chance > 0.99 ? "<1" : 100 - (int) (chance * 100))
						.withStyle(ChatFormatting.GOLD).getVisualOrderText())
				));
		}

		addSlot(widgets, EmiIngredient.of(recipe.value().getIngredient()), mid - 64 + xOff, 90);

		addSlot(widgets, output.get(0), mid + 41 + xOff, 90).recipeContext(this);

		int sx = width / -2 + mid;
		int x = sx;
		int index = 0;
		for (SequencedRecipe<?> recipe : recipe.value().getSequence()) {
			EmiSequencedAssemblySubCategory category = getSubCategory(recipe);
			category.addWidgets(widgets, x, 0, recipe, index++);
			x += category.getWidth() + margin;
		}

		widgets.addDrawable(0, 0, 0, 0, (graphics, mouseX, mouseY, delta) -> {
			PoseStack matrices = graphics.pose();
			Minecraft client = Minecraft.getInstance();
			matrices.pushPose();
			matrices.translate(0, 15, 0);
			if (recipe.value().getOutputChance() != 1) {
				graphics.drawString(client.font, "?", mid + 69 + xOff - client.font.width("?") / 2, 80, 0xefefef, true);
			}

			if (recipe.value().getLoops() > 1) {
				matrices.pushPose();
				matrices.translate(15, 9, 0);
				AllIcons.I_SEQ_REPEAT.render(graphics, mid - 40 + xOff, 75);
				graphics.drawString(client.font, "x" + recipe.value().getLoops(), mid - 24 + xOff, 80, 0x888888, true);
				matrices.popPose();
			}
			matrices.popPose();

			int cx = sx;
			for (int i = 0; i < recipe.value().getSequence().size(); i++) {
				String text = ROMAN[Math.min(i, ROMAN.length)];
				int w = getSubCategory(recipe.value().getSequence().get(i)).getWidth();
				int off = w / 2 - client.font.width(text) / 2;
				graphics.drawString(client.font, text, cx + off, 2, 0x888888, true);
				cx += w + margin;
			}
		});
	}

	public static EmiSequencedAssemblySubCategory getSubCategory(SequencedRecipe<?> recipe) {
		return recipe.getAsAssemblyRecipe()
				.getJEISubCategory()
				.emi()
				.get()
				.get();
	}
}
