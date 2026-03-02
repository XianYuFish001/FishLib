package com.fish.fishlib.integration

import net.neoforged.fml.ModList
import org.objectweb.asm.Type
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

abstract class ManagerIntegration(private val modID: String) {
    val byClazz = HashMap<KClass<*>, Any>()
    val byModID = HashMap<String, Any>()

    inline fun <reified T : Any> get() = this.get(T::class)

    inline operator fun <reified T : Any> invoke() = this.get<T>()

    @Suppress("unchecked_cast")
    fun <T : Any> get(clazz: KClass<T>) = this.byClazz[clazz] as? T

    operator fun <T : Any> invoke(clazz: KClass<T>) = this.get(clazz)

    inline fun <reified T : Any> get(modID: String) = this.byModID[modID] as? T

    inline operator fun <reified T : Any> invoke(modID: String) = this.get<T>(modID)

    fun init() {
        (ModList.get()
            .getModFileById(this.modID) ?: return)
            .file
            .scanResult
            .annotations
            .filter { it.annotationType == Type.getType(BeanIntegration::class.java) }
            .forEach {
                try {
                    val clazzBean = Class.forName(it.clazz.className)
                    val objectBean = clazzBean.kotlin.objectInstance
                        ?: clazzBean.getDeclaredConstructor().newInstance()
                        ?: return@forEach

                    val modID = it.annotationData["modID"] as? String ?: return@forEach
                    if (!ModList.get().isLoaded(modID)) return@forEach

                    var clazzPoint = it.annotationData["clazzPoint"] as? KClass<*> ?: return@forEach
                    if (Nothing::class == clazzPoint)
                        clazzPoint = clazzBean.superclass.kotlin
                    if (Any::class == clazzPoint || Object::class == clazzPoint)
                        return@forEach

                    this.byClazz[clazzPoint] = objectBean
                    this.byModID[modID] = objectBean
                } catch (_: NoSuchMethodException) {
                } catch (_: InvocationTargetException) {
                } catch (_: InstantiationException) {
                }
            }
    }
}

@Target(AnnotationTarget.CLASS)
annotation class BeanIntegration(val modID: String, val clazzPoint: KClass<*> = Nothing::class)