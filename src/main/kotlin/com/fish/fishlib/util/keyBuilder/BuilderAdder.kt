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
    ): TBuilder {
        this.snapshot()
            .addStr(keyBranch)
            .let(BuilderGeneric<TBuilder>::build)
            .let(customizer)
            .let(this.target)
        return this.restore()
    }

    fun append(text: Component) = this.also {
        it.target(text)
    }.cast()

    fun newLine() = this.append(Component.literal("\n"))

    inline fun section(section: String, block: (TBuilder) -> Unit): TBuilder =
        this.addStr(section)
            .snapshot()
            .also(block)
            .restore()
}