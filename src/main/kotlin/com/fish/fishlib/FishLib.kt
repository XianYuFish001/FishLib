package com.fish.fishlib

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(FishLib.MODID)
class FishLib(eventBus: IEventBus, containerMod: ModContainer) {
    companion object {
        const val MODID = "fishlib"
        const val MODNAME = "Fish Lib"
    }
}