package com.fish.fishlib.common

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforgespi.language.ModFileScanData
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory
import java.lang.annotation.ElementType
import java.lang.reflect.Method
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaMethod

@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class InitObject(
    val priority: Int = 100,
    val dist: Array<Dist> = [Dist.CLIENT, Dist.DEDICATED_SERVER]
)

class InitializerObject(eventBus: IEventBus, containerMod: ModContainer) {
    private val logger = LoggerFactory.getLogger("FishLib/Initializer")

    private lateinit var objects: List<ModFileScanData.AnnotationData>

    init {
        this(eventBus, containerMod)
    }

    operator fun invoke(eventBus: IEventBus, containerMod: ModContainer) {
        if (!::objects.isInitialized) {
            objects = (ModList.get()
                .getModFileById(containerMod.modId) ?: return)
                .file
                .scanResult
                .annotations
                .filter { Type.getType(InitObject::class.java) == it.annotationType }
                .sortedBy { it.annotationData.getOrDefault("priority", 100) as Int }
        }
        objects
            .filter {
                AutomaticEventSubscriber.getSides(it.annotationData["dist"])
                    .contains(FMLEnvironment.dist)
            }
            .forEach { this.doInit(eventBus, containerMod, it) }
    }

    private fun doInit(eventBus: IEventBus, containerMod: ModContainer, data: ModFileScanData.AnnotationData) {
        val clazzObject: Class<*>?
        try {
            clazzObject = Class.forName(data.clazz().className)
        } catch (exception: ClassNotFoundException) {
            logger.warn("Failed to load class {}", data.clazz().className, exception)
            return
        }

        when (data.targetType) {
            ElementType.FIELD -> {
                try {
                    val register = clazzObject.getDeclaredField(data.memberName())
                        .also { it.isAccessible = true }
                        .get(null)
                    if (register !is DeferredRegister<*>) return
                    register.register(eventBus)
                } catch (exception: NoSuchFieldException) {
                    logger.error(
                        "Failed to load field register ${data.clazz().className}",
                        exception
                    )
                    throw exception
                } catch (exception: IllegalAccessException) {
                    logger.error(
                        "Failed to load field register ${data.clazz().className}",
                        exception
                    )
                    throw exception
                }
            }

            ElementType.METHOD -> {
                val valueMethod = data.memberName()
                val nameMethod = valueMethod.substring(0, valueMethod.indexOf("("))

                if (
                    this.callFunction(
                        eventBus,
                        containerMod,
                        clazzObject
                    )
                ) return

                this.callMethod(
                    nameMethod,
                    valueMethod,
                    eventBus,
                    containerMod,
                    clazzObject
                )
            }

            else -> Unit
        }
    }

    private fun callFunction(
        eventBus: IEventBus,
        containerMod: ModContainer,
        clazzObject: Class<*>
    ): Boolean {
        val clazzKt = clazzObject.kotlin
        val instance = clazzKt.objectInstance ?: return false

        clazzKt.declaredMemberFunctions.forEach { function ->
            if (!function.hasAnnotation<InitObject>()) return@forEach

            val method = function.javaMethod ?: return@forEach
            method.isAccessible = true

            val params = function.parameters
                .map { it.type.classifier }

            if (instance.javaClass.kotlin !in params) return@forEach

            when {
                IEventBus::class in params && ModContainer::class in params ->
                    method(instance, eventBus, containerMod)

                ModContainer::class in params ->
                    method(instance, containerMod)

                else ->
                    method(instance)
            }

            return true
        }

        return false
    }

    private fun callMethod(
        nameMethod: String,
        valueMethod: String,
        eventBus: IEventBus,
        containerMod: ModContainer,
        clazzObject: Class<*>
    ) {
        val method: Method
        val params: Array<Any?>
        if (valueMethod.contains("IEventBus") && valueMethod.contains("ModContainer")) {
            method = clazzObject.getDeclaredMethod(
                nameMethod,
                IEventBus::class.java,
                ModContainer::class.java
            )
            params = arrayOf(eventBus, containerMod)
        } else if (valueMethod.contains("IEventBus")) {
            method = clazzObject.getDeclaredMethod(
                nameMethod,
                IEventBus::class.java
            )
            params = arrayOf(eventBus)
        } else {
            method = clazzObject.getDeclaredMethod(nameMethod)
            params = arrayOfNulls(0)
        }
        method.isAccessible = true
        method(null, *params)
    }
}
