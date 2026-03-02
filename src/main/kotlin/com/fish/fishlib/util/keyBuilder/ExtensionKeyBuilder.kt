package com.fish.fishlib.util.keyBuilder

import com.fish.fishlib.util.extension.cast
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.ArrayList
import java.util.HashMap

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
    class Simple : BuilderAdder<Simple>(
        this.cast<BuilderGeneric<Simple>>(),
        adder,
        true
    )
    return Simple()
}

fun <V> BuilderGeneric<*>.bindBiAdder(biAdder: BiAdder<V>): BuilderBiAdder<out BuilderBiAdder<*, V>, V> {
    class Simple : BuilderBiAdder<Simple, V>(
        this.cast<BuilderGeneric<Simple>>(),
        biAdder,
        true
    )
    return Simple()
}

fun BuilderGeneric<*>.newContainerComponent(): BuilderAdder<out BuilderAdder<*>> {
    val target = Component.empty()
    class Simple : BuilderAdder<Simple>(
        this.cast<BuilderGeneric<Simple>>(),
        target::append,
        true
    ), HolderTarget<MutableComponent> {
        override val value = target
    }
    return Simple()
}

fun BuilderGeneric<*>.newArrayList(): BuilderAdder<out BuilderAdder<*>> {
    val target = ArrayList<Component>()
    class Simple : BuilderAdder<Simple>(
        this.cast<BuilderGeneric<Simple>>(),
        target::add,
        true
    ), HolderTarget<MutableCollection<Component>> {
        override val value = target
    }
    return Simple()
}

fun <V> BuilderGeneric<*>.newHashMap(): BuilderBiAdder<out BuilderBiAdder<*, V>, V> {
    val target = HashMap<V, Component>()
    class Simple : BuilderBiAdder<Simple, V>(
        this.cast<BuilderGeneric<Simple>>(),
        target::put,
        true
    ), HolderTarget<MutableMap<V, Component>> {
        override val value = target
    }
    return Simple()
}