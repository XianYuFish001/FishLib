package com.fish.fishlib.util.keyBuilder

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

open class BuilderAdder<TBuilder : BuilderAdder<TBuilder>> internal constructor(
    original: BuilderGeneric<TBuilder>?,
    target: Adder,
    snapshot: Boolean
) : BuilderSnapshotable<TBuilder, Adder>(original, target, snapshot) {
    override fun snapshot(): TBuilder {
        val builder = BuilderAdder(this, this.target, false).cast()
        builder.snapshot = this.cast()
        return builder
    }

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