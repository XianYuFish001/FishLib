package com.fish.fishlib.datagen

import com.fish.fishlib.util.extension.unit
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

abstract class ProviderDatapack<T>(
    private val modID: String,
    internal val keyRegistry: ResourceKey<out Registry<T>>
) {
    protected fun key(key: String): ResourceKey<T> = ResourceKey.create(
        this.keyRegistry,
        ResourceLocation.fromNamespaceAndPath(
            this.modID,
            key
        )
    )

    operator fun invoke(builder: RegistrySetBuilder) =
        builder.add(this.keyRegistry) { it.bootstrap() }.unit()

    abstract fun BootstrapContext<T>.bootstrap()
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProviderObject