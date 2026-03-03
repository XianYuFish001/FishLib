package com.fish.fishlib.util.keyBuilder

abstract class BuilderSnapshotable<TBuilder : BuilderGeneric<TBuilder>, TTarget> internal constructor(
    original: BuilderGeneric<TBuilder>?,
    protected val target: TTarget,
    snapshot: Boolean
) : BuilderGeneric<TBuilder>(original) {
    internal var snapshot: TBuilder? = null

    init {
        if (snapshot)
            this.snapshot()
    }

    abstract fun snapshot(): TBuilder

    open fun restore() = this.snapshot ?: this.cast()
}