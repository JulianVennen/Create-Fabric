package com.simibubi.create.foundation.data;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllDamageTypes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;

public class DamageTypeTagGen extends TagsProvider<DamageType> {
	public DamageTypeTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, Registries.DAMAGE_TYPE, lookupProvider);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		// TODO: This is causing datagen to fail, not sure why.
		/*getOrCreateRawBuilder(DamageTypeTags.BYPASSES_ARMOR)
				.addElement(AllDamageTypes.CRUSH.location())
				.addElement(AllDamageTypes.FAN_FIRE.location())
				.addElement(AllDamageTypes.FAN_LAVA.location())
				.addElement(AllDamageTypes.DRILL.location())
				.addElement(AllDamageTypes.SAW.location());
		getOrCreateRawBuilder(DamageTypeTags.IS_FIRE)
				.addElement(AllDamageTypes.FAN_FIRE.location())
				.addElement(AllDamageTypes.FAN_LAVA.location());
		getOrCreateRawBuilder(DamageTypeTags.IS_EXPLOSION)
				.addElement(AllDamageTypes.CUCKOO_SURPRISE.location());*/
	}

	@Override
	public String getName() {
		return "Create's Damage Type Tags";
	}
}
