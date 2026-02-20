package com.fish.fishlib

import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(FishLib.MODID)
class FishLib(eventBus: IEventBus, containerMod: ModContainer) {
    init {
    }

    companion object {
        const val MODID = "fishlib"
        const val MODNAME = "Fish Lib"

        fun getLocation(path: String): ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(MODID, path)
    }
}