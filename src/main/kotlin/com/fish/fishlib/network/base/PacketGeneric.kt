package com.fish.fishlib.network.base

import com.fish.fishlib.network.InitializerPacket
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import kotlin.collections.get

interface PacketGeneric : CustomPacketPayload {
    override fun type() = InitializerPacket.Types[this::class.simpleName]
        ?: throw IllegalStateException("Unknown NetworkPacket: ${this::class.simpleName}")
}
