package com.fish.fishlib.util.keyBuilder

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.level.ItemLike

open class BuilderGeneric<TBuilder : BuilderGeneric<TBuilder>> internal constructor(
    original: BuilderGeneric<TBuilder>?
) {
    internal var pattern = "%s%s".toKeyPattern()
    internal var keyMain = ""
    internal var keyAdditional = ""
    internal var args: Array<Any>? = null

    init {
        this.copyFrom(original)
    }

    protected fun copyFrom(original: BuilderGeneric<TBuilder>?) {
        if (original == null) return
        this.pattern = original.pattern
        this.keyMain = original.keyMain
        this.keyAdditional = original.keyAdditional
        this.args = original.args
    }

    @Suppress("unchecked_cast")
    internal fun cast() = this as TBuilder

    fun pattern(pattern: IKeyPattern): TBuilder {
        this.pattern = pattern
        return this.cast()
    }

    fun item(item: ItemLike): TBuilder {
        this.keyMain = item.asItem().descriptionId
        return this.cast()
    }

    fun addStr(keyAdditional: String?): TBuilder {
        if (keyAdditional.isNullOrBlank()) return this.cast()
        if (!this.keyAdditional.endsWith("."))
            this.keyAdditional += "."
        this.keyAdditional += keyAdditional
        return this.cast()
    }

    @JvmOverloads
    fun addStr(condition: Boolean, keyA: String?, keyB: String? = "") =
        this.addStr(if (condition) keyA else keyB)

    fun args(vararg args: Any?): TBuilder {
        this.args = args
            .map {
                return@map if (it == null) ""
                else if (it is Component
                    || TranslatableContents.isAllowedPrimitiveArgument(it)) it
                else it.toString()
            }.toTypedArray()
        return this.cast()
    }

    fun buildRaw() = this.pattern(this.keyMain, this.keyAdditional)

    fun build(): MutableComponent {
        val keyRaw = this.buildRaw()

        return this.args
            ?.let { Component.translatable(keyRaw, *it) }
            ?: Component.translatable(keyRaw)
    }
}