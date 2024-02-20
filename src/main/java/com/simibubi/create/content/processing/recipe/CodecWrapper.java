package com.simibubi.create.content.processing.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public class CodecWrapper<T> implements Codec<T> {
	private final Codec<T> wrapped;

	public CodecWrapper(Codec<T> wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
		return wrapped.decode(ops, input);
	}

	@Override
	public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
		return wrapped.encode(input, ops, prefix);
	}
}
