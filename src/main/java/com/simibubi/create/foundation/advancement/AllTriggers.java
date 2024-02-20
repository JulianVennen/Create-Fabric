package com.simibubi.create.foundation.advancement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.advancements.CriteriaTriggers;

public class AllTriggers {

	private static final Map<String, CriterionTriggerBase<?>> triggers = new HashMap<>();

	public static SimpleCreateTrigger addSimple(String id) {
		return add(new SimpleCreateTrigger(id));
	}

	private static <T extends CriterionTriggerBase<?>> T add(T instance) {
		triggers.put(instance.getId().toString(), instance);
		return instance;
	}

	public static void register() {
		triggers.forEach(CriteriaTriggers::register);
	}

}
