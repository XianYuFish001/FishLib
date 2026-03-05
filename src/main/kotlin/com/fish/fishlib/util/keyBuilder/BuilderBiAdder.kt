package com.fish.fishlib.util.keyBuilder

import net.minecraft.network.chat.MutableComponent

abstract class BuilderBiAdder<TBuilder : BuilderBiAdder<TBuilder, TKey>, TKey> internal constructor(
    original: BuilderGeneric<TBuilder>?,
    target: BiAdder<TKey>,
    snapshot: Boolean
) : BuilderSnapshotable<TBuilder, BiAdder<TKey>>(original, target, snapshot) {
    override fun snapshot(): TBuilder {
        val builder = this.clone()
        builder.snapshot = this.cast()
        return builder
    }

//    override fun clone() = BuilderBiAdder(this, this.target, false).cast()

    fun buildInto(
        keyBranch: TKey,
        customizer: Customizer<MutableComponent> = { it },
        plain: Boolean = false
    ): TBuilder {
        this.snapshot()
            .addStr(!plain, keyBranch.toString())
            .let(BuilderGeneric<TBuilder>::build)
            .let(customizer)
            .let { this.target(keyBranch, it) }
        return this.restore()
    }
}