package com.fish.fishlib.util

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.BlockHitResult

object FishStreamCodecs {
    @JvmField
    val blockHitResult: StreamCodec<FriendlyByteBuf, BlockHitResult> = StreamCodec.of(
        FriendlyByteBuf::writeBlockHitResult,
        FriendlyByteBuf::readBlockHitResult
    )
}