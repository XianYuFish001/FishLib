package com.fish.fishlib.network

import com.fish.fishlib.network.base.BPacketGeneric
import com.fish.fishlib.network.base.CPacketGeneric
import com.fish.fishlib.network.base.PacketGeneric
import com.fish.fishlib.network.base.SPacketGeneric
import com.fish.fishlib.FishLib
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadHandler
import net.neoforged.neoforgespi.language.ModFileScanData
import org.objectweb.asm.Type
import kotlin.collections.get
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties

@Suppress("unchecked_cast")
object InitializerPacket {
    internal val Types = HashMap<String, CustomPacketPayload.Type<out PacketGeneric>>()

    private val DataStreamCodec by lazy {
        ModList.get().allScanData
            .asSequence()
            .flatMap(ModFileScanData::getAnnotations)
            .filter { Type.getType(PacketStreamCodec::class.java) == it.annotationType }
            .mapNotNull {
                val nameProperty = it.memberName
                    .substringBefore("$")
                    .substringAfter("get")
                    .let { name ->
                        if (name.uppercase() == name) return@let name
                        name[0].lowercaseChar() + name.substring(1)
                    }

                val clazzCompanion = Class.forName(it.clazz.className)
                val clazzCompanionKt = clazzCompanion.kotlin

                val clazzPacketKt = clazzCompanion.enclosingClass?.kotlin ?: clazzCompanionKt

                val companion = clazzPacketKt
                    .companionObjectInstance
                    ?: clazzPacketKt.objectInstance
                    ?: return@mapNotNull null
                val codec = (
                        (clazzCompanionKt
                            .memberProperties
                            .find { property ->
                                property.name == nameProperty
                                        || property.returnType.classifier == StreamCodec::class
                            } as? KProperty1<Any, StreamCodec<*, *>>)
                            ?.get(companion)
                            ?: clazzCompanion
                                .getDeclaredField(nameProperty)
                                .get(companion)
                        ) as? StreamCodec<RegistryFriendlyByteBuf, out PacketGeneric>
                    ?: return@mapNotNull null
                val typePacket = clazzPacketKt
                    .findAnnotation<FishNetworkPacket>()
                    ?.value
                    ?: return@mapNotNull null
                typePacket to codec
            }.toMap()
    }

    operator fun invoke(containerMod: ModContainer) {
        val eventBus = containerMod.eventBus ?: return
        eventBus.addListener<RegisterPayloadHandlersEvent> {
            this.init(it, containerMod.modId)
        }
    }

    private fun init(event: RegisterPayloadHandlersEvent, modID: String) {
        val registrar = event.registrar(FishLib.MODID)
        val clazzTypePacket = Type.getType(FishNetworkPacket::class.java)
        (ModList.get()
            .getModFileById(modID) ?: return)
            .file
            .scanResult
            .annotations
            .filter { clazzTypePacket == it.annotationType }
            .map { it.annotationData["value"] as String to Class.forName(it.clazz.className).kotlin }
            .forEach { (type, clazz) ->
                this.Types.putIfAbsent(
                    clazz.simpleName ?: return@forEach,
                    CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath(modID, type))
                )
                when {
                    clazz.isSubclassOf(BPacketGeneric::class) -> this.register(
                        clazz as KClass<BPacketGeneric>,
                        type,
                        registrar::playBidirectional,
                        BPacketGeneric::handle
                    )

                    clazz.isSubclassOf(CPacketGeneric::class) -> this.register(
                        clazz as KClass<CPacketGeneric>,
                        type,
                        registrar::playToServer,
                        CPacketGeneric::handle
                    )

                    clazz.isSubclassOf(SPacketGeneric::class) -> this.register(
                        clazz as KClass<SPacketGeneric>,
                        type,
                        registrar::playToClient,
                        SPacketGeneric::handle
                    )
                }
            }
    }

    private fun <TPacket : PacketGeneric> register(
        clazz: KClass<TPacket>,
        typePacket: String,
        register: (
            CustomPacketPayload.Type<TPacket>,
            StreamCodec<RegistryFriendlyByteBuf, TPacket>,
            IPayloadHandler<TPacket>
        ) -> Unit,
        handler: IPayloadHandler<TPacket>
    ) = register(
        this.Types[clazz.simpleName] as? CustomPacketPayload.Type<TPacket>
            ?: throw IllegalStateException("Found EAEPPacket ${clazz.simpleName} with UNKNOWN Type"),
        (DataStreamCodec[typePacket] ?: clazz.staticProperties
            .filterIsInstance<KProperty0<StreamCodec<*, *>>>()
            .ifEmpty { throw IllegalStateException("Found EAEPPacket ${clazz.simpleName} with NO StreamCodec") }[0]
            .get()) as StreamCodec<RegistryFriendlyByteBuf, TPacket>,
        handler
    )
}