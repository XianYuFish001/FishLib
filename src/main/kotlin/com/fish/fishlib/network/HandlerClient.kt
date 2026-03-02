package com.fish.fishlib.network

import com.fish.fishlib.network.base.SPacketGeneric
import net.minecraft.client.player.LocalPlayer

fun interface HandlerClient<T : SPacketGeneric> {
    @Suppress("unchecked_cast")
    operator fun invoke(packet: SPacketGeneric, player: LocalPlayer) = (packet as? T)?.handle(player)

    fun T.handle(player: LocalPlayer)
}