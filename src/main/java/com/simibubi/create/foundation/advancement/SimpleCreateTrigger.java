package com.simibubi.create.foundation.advancement;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonObject;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.server.level.ServerPlayer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleCreateTrigger extends CriterionTriggerBase<SimpleCreateTrigger.Instance> {

	public SimpleCreateTrigger(String id) {
		super(id);
	}

	@Override
	public Instance createInstance(JsonObject json, DeserializationContext context) {
		return new Instance();
	}

	public void trigger(ServerPlayer player) {
		super.trigger(player, null);
	}

	public Instance instance() {
		return new Instance();
	}

	public Criterion<Instance> criterion() {
		return new Criterion<>(this, instance());
	}

	public static class Instance extends CriterionTriggerBase.Instance {

		public Instance() {
			super(Optional.empty());
		}

		@Override
		protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
			return true;
		}
	}
}
