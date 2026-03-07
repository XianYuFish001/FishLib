package com.fish.fishlib.datagen

import net.minecraft.core.RegistrySetBuilder
import net.neoforged.fml.ModList
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import net.neoforged.neoforge.data.event.GatherDataEvent
import org.objectweb.asm.Type

fun GatherDataEvent.datapack() = RegistrySetBuilder().also { builder ->
    (ModList.get().getModFileById(this.modContainer.modId)
        ?: throw NullPointerException("Mod file with id ${this.modContainer.modId} not found"))
        .file
        .scanResult
        .annotations
        .asSequence()
        .filter { it.annotationType == Type.getType(ProviderObject::class.java) }
        .mapNotNull { Class.forName(it.clazz.className).kotlin.objectInstance }
        .filterIsInstance<ProviderDatapack<*>>()
        .forEach { it(builder) }
}.let {
    DatapackBuiltinEntriesProvider(
        this.generator.packOutput,
        this.lookupProvider,
        it,
        setOf(this.modContainer.modId)
    )
}