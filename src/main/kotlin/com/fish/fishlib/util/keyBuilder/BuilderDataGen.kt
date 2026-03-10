package com.fish.fishlib.util.keyBuilder

import net.neoforged.neoforge.data.loading.DatagenModLoader

class BuilderDataGen internal constructor(
    original: BuilderGeneric<BuilderDataGen>?,
    snapshot: Boolean
) : BuilderSnapshotable<BuilderDataGen, Any?>(original, null, snapshot) {
    override fun snapshot(): BuilderDataGen {
        val builder = this.clone()
        builder.snapshot = this
        return builder
    }

    override fun clone() = BuilderDataGen(this, false)

    fun buildInto(text: String): BuilderDataGen {
        ContainerDataGen.accept(this.buildRaw(), text)
        return this
    }

    fun branch(keyBranch: String, text: String) =
        this.snapshot()
            .addStr(keyBranch)
            .buildInto(text)
            .restore()

    inline fun section(section: String, block: (BuilderDataGen) -> Unit) =
        this.snapshot()
            .addStr(section)
            .also(block)
            .restore()
}

object ContainerDataGen {
    private val translators = HashMap<String, (String, String) -> Unit>()
    private val locale: ThreadLocal<String> = ThreadLocal.withInitial { "en_us" }

    fun with(locale: String, adder: (String, String) -> Unit, task: () -> Unit) {
        this.translators[locale] = adder
        this.locale.set(locale)
        task()
        this.translators.remove(locale)
        this.locale.remove()
    }

    internal fun checkEnv() = if (!DatagenModLoader.isRunningDataGen())
        throw IllegalStateException("Cannot call data-only methods outside of the runData phase") else Unit

    internal val accept
        get() = this.translators.getOrDefault(
            this.locale.get()
        ) { _, _ -> }
}