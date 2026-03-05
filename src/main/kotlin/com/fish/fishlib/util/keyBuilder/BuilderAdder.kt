package com.fish.fishlib.util.keyBuilder

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

abstract class BuilderAdder<TBuilder : BuilderAdder<TBuilder>> internal constructor(
    original: BuilderGeneric<TBuilder>?,
    target: Adder,
    snapshot: Boolean
) : BuilderSnapshotable<TBuilder, Adder>(original, target, snapshot) {
    override fun snapshot(): TBuilder {
        val builder = this.clone()
        builder.snapshot = this.cast()
        return builder
    }

//    override fun clone() = BuilderAdder(this, this.target, false).cast()

    @JvmOverloads
    fun buildInto(
        keyBranch: String = "",
        customizer: Customizer<MutableComponent> = { it }
    ) = this.section(keyBranch) { it
        .let(BuilderGeneric<TBuilder>::build)
        .let(customizer)
        .let(this.target)
    }

    fun append(text: Component) = this.also {
        it.target(text)
    }.cast()

    fun newLine() = this.append(Component.literal("\n"))

    inline fun section(section: String, block: (TBuilder) -> Unit) =
        this.snapshot()
            .addStr(section)
            .also(block)
            .restore()
}