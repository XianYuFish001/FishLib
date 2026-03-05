package com.fish.fishlib.integration.jade

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.Accessor
import snownee.jade.api.IServerDataProvider

@JvmRecord
data class WrapperObjectedProvider<TAccessor : Accessor<*>>(
    val uid: ResourceLocation,
    val providers: Provider<TAccessor>
) : IServerDataProvider<TAccessor> {
    override fun appendServerData(
        serverData: CompoundTag, accessor: TAccessor
    ) = this.providers.forEach { provider ->
        val data = CompoundTag()
        provider.second(data, accessor)
        serverData.put(provider.first, data)
    }

    override fun getUid() = this.uid

    companion object {
        fun <TAccessor : Accessor<*>, TProvider> create(
            uid: ResourceLocation,
            clazzProvider: Class<TProvider>
        ) where TProvider : Enum<TProvider>,
                TProvider : IObjectedProvider<TAccessor> =
            WrapperObjectedProvider(uid, IObjectedProvider.getProviders(clazzProvider))
    }
}
