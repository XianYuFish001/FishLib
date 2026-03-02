package com.fish.fishlib.network.base

import net.minecraft.network.protocol.PacketFlow
import net.neoforged.neoforge.network.handling.IPayloadContext

interface BPacketGeneric : CPacketGeneric, SPacketGeneric {
    override fun handle(context: IPayloadContext) = when (context.flow()) {
        PacketFlow.CLIENTBOUND -> super<SPacketGeneric>.handle(context)
        PacketFlow.SERVERBOUND -> super<CPacketGeneric>.handle(context)
    }
}
