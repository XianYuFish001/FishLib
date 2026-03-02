package com.fish.fishlib.network.base

import com.fish.fishlib.network.HandlerClient
import net.minecraft.client.player.LocalPlayer
import net.neoforged.neoforge.network.handling.IPayloadContext

interface SPacketGeneric : PacketGeneric {
    val handlerClient: HandlerClient<out SPacketGeneric>

    fun handle(context: IPayloadContext) {
        if (context.player() is LocalPlayer)
            this.handlerClient(this, context.player() as LocalPlayer)
    }
}
