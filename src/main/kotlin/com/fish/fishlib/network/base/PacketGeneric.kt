package com.fish.fishlib.network.base

import com.fish.fishlib.network.InitializerPacket
import com.fish.fishlib.util.extension.tryCast
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.network.PacketDistributor

interface PacketGeneric : CustomPacketPayload {
    override fun type() = InitializerPacket.Types[this::class.simpleName]
        ?: throw IllegalStateException("Unknown NetworkPacket: ${this::class.simpleName}")

    companion object {
        fun CustomPacketPayload.sendToServer() =
            PacketDistributor.sendToServer(this)

        fun CustomPacketPayload.sendToPlayer(player: Player) = player.tryCast<ServerPlayer>()?.let {
            PacketDistributor.sendToPlayer(it, this)
        }
    }
}
