package com.fish.fishlib.config

import com.fish.fishlib.util.extension.appendEnd
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.ModConfigSpec
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

private typealias Consumer<T> = (T) -> Unit

class HelperConfig(private val spec: KProperty0<ModConfigSpec>) {
    internal val configs = HashMap<String, ModConfigSpec.ConfigValue<*>>()

    @Suppress("unchecked_cast")
    operator fun <T : Any> provideDelegate(thisRef: Any?, property: KProperty<*>) =
        DelegateConfig { configs[property.name.lowercase()] as? ModConfigSpec.ConfigValue<T> }

    fun init(containerMod: ModContainer, path: String = "") {
        this.spec.isAccessible = true
        val delegate = this.spec.getDelegate() as? DelegateSpec
            ?: throw IllegalArgumentException("Use HelperConfigKt#spec to declare a delegated spec")
        containerMod.registerConfig(
            delegate.type,
            delegate.spec,
            path.appendEnd("/", false) + delegate.name
        )
    }

    companion object {
        @Suppress("unchecked_cast")
        val <T : Any> KProperty0<T>.configValue: ModConfigSpec.ConfigValue<T>?
            get() = (this.getDelegate() as? DelegateConfig<T>)?.config

        fun <T> ModConfigSpec.ConfigValue<T>.bind(helper: HelperConfig, property: KProperty<T>? = null) {
            val path = property?.name ?: this.path.last()
            helper.configs[path.lowercase()] = this
        }
    }
}

class DelegateConfig<T : Any>(value: () -> ModConfigSpec.ConfigValue<T>?) : ReadWriteProperty<Any?, T> {
    val config by lazy(value)

    override fun getValue(thisRef: Any?, property: KProperty<*>) = this.config?.get()
        ?: throw IllegalStateException("Config ${property.name} is unbound")

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = this.config?.set(value)
        ?: throw IllegalStateException("Config ${property.name} is unbound")
}

class DelegateSpec(
    val name: String, val type: ModConfig.Type, val spec: ModConfigSpec
): ReadOnlyProperty<Any?, ModConfigSpec> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ) = this.spec
}

inline fun spec(type: ModConfig.Type, name: String = "", builder: Consumer<ModConfigSpec.Builder>): DelegateSpec {
    val spec = ModConfigSpec.Builder()
    builder(spec)
    return DelegateSpec(name.ifEmpty(type::extension), type, spec.build())
}

inline fun ModConfigSpec.Builder.section(
    section: String, modifier: Consumer<ModConfigSpec.Builder>
): ModConfigSpec.Builder {
    this.push(section)
    modifier(this)
    this.pop()
    return this
}