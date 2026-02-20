package com.fish.fishlib.util.keyBuilder

import net.neoforged.neoforge.data.loading.DatagenModLoader
import java.util.HashMap

class BuilderDataGen internal constructor(
    original: BuilderGeneric<BuilderDataGen>?,
    snapshot: Boolean
) : BuilderSnapshotable<BuilderDataGen, Any?>(original, null, snapshot) {
    override fun snapshot(): BuilderDataGen {
        val builder = BuilderDataGen(this, false)
        builder.snapshot = this
        return builder
    }

    fun buildInto(text: String): BuilderDataGen {
        ContainerDataGen.accept(this.buildRaw(), text)
        return this
    }

    fun branch(keyBranch: String, text: String) =
        this.snapshot()
            .addStr(keyBranch)
            .buildInto(text)
            .restore()
}

object ContainerDataGen {
    private val translators = HashMap<String, (String, String) -> Unit>()
    private val locale: ThreadLocal<String> = ThreadLocal.withInitial { "en_us" }

    @JvmStatic
    fun bind(locale: String, adder: (String, String) -> Unit) {
        this.translators[locale] = adder
        this.locale.set(locale)
    }

    @JvmStatic
    fun destroy(locale: String) {
        this.translators.remove(locale)
        this.locale.remove()
    }

    internal fun checkEnv() = if (!DatagenModLoader.isRunningDataGen())
        throw IllegalStateException("Cannot use data-only methods outside of the runData phase") else Unit

    internal val accept
        get() = this.translators.getOrDefault(
            this.locale.get()
        ) { _, _ -> }
}