package com.simibubi.create.foundation.blockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.fabricators_of_create.porting_lib.blocks.api.addons.CustomDataPacketHandlingBlockEntity;
import io.github.fabricators_of_create.porting_lib.blocks.api.addons.CustomUpdateTagHandlingBlockEntity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SyncedBlockEntity extends BlockEntity implements CustomDataPacketHandlingBlockEntity, CustomUpdateTagHandlingBlockEntity {

	public SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return writeClient(new CompoundTag());
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		readClient(tag);
	}

	@Override
	public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
		CompoundTag tag = packet.getTag();
		readClient(tag == null ? new CompoundTag() : tag);
	}

	// Special handling for client update packets
	public void readClient(CompoundTag tag) {
		load(tag);
	}

	// Special handling for client update packets
	public CompoundTag writeClient(CompoundTag tag) {
		saveAdditional(tag);
		return tag;
	}

	public void sendData() {
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(getBlockPos());
	}

	public void notifyUpdate() {
		setChanged();
		sendData();
	}

//	public PacketDistributor.PacketTarget packetTarget() {
//		return PacketDistributor.TRACKING_CHUNK.with(this::containedChunk);
//	}

	public LevelChunk containedChunk() {
		return level.getChunkAt(worldPosition);
	}

	@Override
	public void deserializeNBT(BlockState state, CompoundTag nbt) {
		this.load(nbt);
	}

	@SuppressWarnings("deprecation")
	public HolderGetter<Block> blockHolderGetter() {
		return (HolderGetter<Block>) (level != null ? level.holderLookup(Registries.BLOCK)
			: BuiltInRegistries.BLOCK.asLookup());
	}

}
