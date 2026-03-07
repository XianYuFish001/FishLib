package com.fish.fishlib.util.keyBuilder

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.level.material.Fluid
import net.neoforged.neoforge.fluids.FluidType
import net.neoforged.neoforge.registries.DeferredHolder

internal typealias Adder = (Component) -> Any?
internal typealias BiAdder<T> = (T, Component) -> Any?
internal typealias Customizer<T> = (T) -> T

open class UtilKeyBuilder(val modID: String) {
    open fun of(pattern: IKeyPattern) = BuilderGeneric(null).apply {
        this.keyMain = this@UtilKeyBuilder.modID
        this.pattern = pattern
    }

    open fun of(holder: DeferredHolder<*, *>) = BuilderGeneric(null).also {
        it.applyMain(holder)
    }

    open fun dataGen(pattern: IKeyPattern) = BuilderDataGen(null, false).apply {
        ContainerDataGen.checkEnv()
        this.keyMain = this@UtilKeyBuilder.modID
        this.pattern = pattern
    }.snapshot()

    open fun dataGen(holder: DeferredHolder<*, *>) = BuilderDataGen(null, false).also {
        ContainerDataGen.checkEnv()
        it.applyMain(holder)
    }.snapshot()

    private fun BuilderGeneric<*>.applyMain(item: Any) {
        val key = when (item) {
            is Item -> item.asItem().descriptionId
            is FluidType -> item.descriptionId
            is Fluid -> this.applyMain(item.fluidType)
            is DeferredHolder<*, *> -> this.applyMain(item.get())
            else -> throw IllegalArgumentException("Unsupported value type ${item.javaClass.name}")
        }
        if (key is String) this.keyMain = key
    }
}