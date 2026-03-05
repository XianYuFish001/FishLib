package com.fish.fishlib.integration.jade

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import kotlin.reflect.KClass

data class InfoBlock<TProvider, TConsumer>(
    val uid: ResourceLocation,
    val clazzProvider: KClass<TProvider>,
    val clazzConsumer: KClass<TConsumer>,
    val clazzBlockEntity: KClass<out BlockEntity>,
    val clazzBlock: KClass<out Block>
) where TProvider : Enum<TProvider>,
        TProvider : IObjectedProvider<BlockAccessor>,
        TConsumer : Enum<TConsumer>,
        TConsumer : IBlockComponentProvider {
    fun registerProvider(registration: IWailaCommonRegistration) = registration.registerBlockDataProvider(
        WrapperObjectedProvider.create(this.uid, this.clazzProvider.java),
        this.clazzBlockEntity.java
    )

    fun registerConsumer(registration: IWailaClientRegistration) {
        for (entry in this.clazzConsumer.java.getEnumConstants()) {
            registration.registerBlockComponent(entry, this.clazzBlock.java)
        }
    }
}