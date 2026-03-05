package com.fish.fishlib.network

import com.fish.fishlib.network.base.SPacketGeneric
import net.minecraft.world.entity.player.Player

fun interface HandlerClient<T : SPacketGeneric> {
    @Suppress("unchecked_cast")
    operator fun invoke(packet: SPacketGeneric, player: Player) = (packet as? T)?.handle(player)

    fun T.handle(player: Player)
}