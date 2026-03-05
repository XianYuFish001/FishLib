package com.fish.fishlib.util.keyBuilder

import com.fish.fishlib.util.extension.cast
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

internal interface HolderTarget<T> {
    val value: T
}

@Suppress("unchecked_cast")
val BuilderAdder<*>.collection
    get() = (this as? HolderTarget<*>)?.value as? MutableCollection<Component>

val BuilderAdder<*>.containerComponent
    get() = (this as? HolderTarget<*>)?.value as? MutableComponent

@Suppress("unchecked_cast")
val <V> BuilderBiAdder<*, V>.map
    get() = (this as? HolderTarget<*>)?.value as? MutableMap<V, Component>

fun BuilderGeneric<*>.bindAdder(adder: Adder): BuilderAdder<out BuilderAdder<*>> {
    class Simple(snapshot: Boolean, original: Simple? = null) : BuilderAdder<Simple>(
        original ?: this.cast<BuilderGeneric<Simple>>(),
        adder,
        snapshot
    ) {
        override fun clone() = Simple(false, this)
    }
    return Simple(true)
}

fun <V> BuilderGeneric<*>.bindBiAdder(biAdder: BiAdder<V>): BuilderBiAdder<out BuilderBiAdder<*, V>, V> {
    class Simple(snapshot: Boolean, original: Simple? = null) : BuilderBiAdder<Simple, V>(
        original ?: this.cast<BuilderGeneric<Simple>>(),
        biAdder,
        snapshot
    ) {
        override fun clone() = Simple(false, this)
    }
    return Simple(true)
}

fun BuilderGeneric<*>.newContainerComponent(): BuilderAdder<out BuilderAdder<*>> {
    val target = Component.empty()
    class Simple(snapshot: Boolean, original: Simple? = null) : BuilderAdder<Simple>(
        original ?: this.cast<BuilderGeneric<Simple>>(),
        target::append,
        snapshot
    ), HolderTarget<MutableComponent> {
        override val value = target

        override fun clone() = Simple(false, this)
    }
    return Simple(true)
}

fun BuilderGeneric<*>.newArrayList(): BuilderAdder<out BuilderAdder<*>> {
    val target = ArrayList<Component>()
    class Simple(snapshot: Boolean, original: Simple? = null) : BuilderAdder<Simple>(
        original ?: this.cast<BuilderGeneric<Simple>>(),
        target::add,
        snapshot
    ), HolderTarget<MutableCollection<Component>> {
        override val value = target

        override fun clone() = Simple(false, this)
    }
    return Simple(true)
}

fun <V> BuilderGeneric<*>.newHashMap(): BuilderBiAdder<out BuilderBiAdder<*, V>, V> {
    val target = HashMap<V, Component>()
    class Simple(snapshot: Boolean, original: Simple? = null) : BuilderBiAdder<Simple, V>(
        original ?: this.cast<BuilderGeneric<Simple>>(),
        target::put,
        snapshot
    ), HolderTarget<MutableMap<V, Component>> {
        override val value = target

        override fun clone() = Simple(false, this)
    }
    return Simple(true)
}