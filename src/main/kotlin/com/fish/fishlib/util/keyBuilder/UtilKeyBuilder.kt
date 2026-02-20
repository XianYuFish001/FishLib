package com.fish.fishlib.util.keyBuilder

import com.fish.fishlib.util.extension.cast
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.level.material.Fluid
import net.neoforged.neoforge.fluids.FluidType
import net.neoforged.neoforge.registries.DeferredHolder
import java.util.ArrayList
import java.util.HashMap

internal typealias Adder = (Component) -> Any?
internal typealias BiAdder<T> = (T, Component) -> Any?
internal typealias Customizer<T> = (T) -> T

abstract class UtilKeyBuilder(val modID: String) {
    fun of(pattern: IKeyPattern): BuilderGeneric<*> {
        val builder = BuilderGeneric(null)
        builder.keyMain = this.modID
        builder.pattern = pattern
        return builder
    }

    fun of(holder: DeferredHolder<*, *>): BuilderGeneric<*> {
        val builder = BuilderGeneric(null)
        this.applyMain(builder, holder)
        return builder
    }

    fun dataGen(pattern: IKeyPattern): BuilderDataGen {
        ContainerDataGen.checkEnv()
        val builder = BuilderDataGen(null, false)
        builder.keyMain = this.modID
        builder.pattern = pattern
        return builder
    }

    fun dataGen(holder: DeferredHolder<*, *>): BuilderDataGen {
        ContainerDataGen.checkEnv()
        val builder = BuilderDataGen(null, false)
        this.applyMain(builder, holder)
        return builder
    }

    private fun applyMain(builder: BuilderGeneric<*>, item: Any): String {
        builder.keyMain = when (item) {
            is Item -> item.asItem().descriptionId
            is FluidType -> item.descriptionId
            is Fluid -> applyMain(builder, item.fluidType)
            is DeferredHolder<*, *> -> applyMain(builder, item.get())
            else -> throw IllegalArgumentException("Unsupported value type ${item.javaClass.name}")
        }
        return builder.keyMain
    }
}

fun BuilderGeneric<*>.bindAdder(adder: Adder): BuilderAdder<*> {
    class Simple : BuilderAdder<Simple>(
        this.cast<BuilderGeneric<Simple>>(),
        adder,
        true
    )
    return Simple()
}

fun <V> BuilderGeneric<*>.bindBiAdder(biAdder: BiAdder<V>): BuilderBiAdder<*, V> {
    class Simple : BuilderBiAdder<Simple, V>(
        this.cast<BuilderGeneric<Simple>>(),
        biAdder,
        true
    )
    return Simple()
}

fun BuilderGeneric<*>.bindCollection(target: MutableCollection<Component>): BuilderAdder<*> {
    class Simple : BuilderAdder<Simple>(
        this.cast<BuilderGeneric<Simple>>(),
        target::add,
        true
    ), HolderTarget<MutableCollection<Component>> {
        override val value = target
    }
    return Simple()
}

fun <V> BuilderGeneric<*>.bindMap(target: MutableMap<V, Component>): BuilderBiAdder<*, V> {
    class Simple : BuilderBiAdder<Simple, V>(
        this.cast<BuilderGeneric<Simple>>(),
        target::put,
        true
    ), HolderTarget<MutableMap<V, Component>> {
        override val value = target
    }
    return Simple()
}

fun BuilderGeneric<*>.newArrayList() = this.bindCollection(ArrayList())

fun <T> BuilderGeneric<*>.newHashMap() = this.bindMap(HashMap<T, Component>())

internal interface HolderTarget<T> {
    val value: T
}

@Suppress("unchecked_cast")
fun BuilderAdder<*>.getCollection() = (this as? HolderTarget<*>)?.value as? MutableCollection<Component>

@Suppress("unchecked_cast")
fun <T> BuilderBiAdder<*, T>.getMap() = (this as? HolderTarget<*>)?.value as? MutableMap<Component, T>