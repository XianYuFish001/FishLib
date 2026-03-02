package com.fish.fishlib.mixin

import com.fish.fishlib.mixin.MixinDependencies.InfoDependency
import com.fish.fishlib.util.extension.splitToLastKey
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.LoadingModList
import net.neoforged.fml.loading.moddiscovery.ModInfo
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.slf4j.LoggerFactory
import java.io.IOException

object DiscoverDependencies {
    private val Logger = LoggerFactory.getLogger("FishLib/MixinDependencies")
    private val cacheMod = HashMap<String, Boolean>()
    private val cacheDependency = HashMap<String, Boolean>()
    private lateinit var loader: ClassLoader

    private fun isModLoaded(modID: String): Boolean {
        if (cacheMod.containsKey(modID)) return cacheMod[modID]!!

        val loaded = if (ModList.get() == null) LoadingModList.get().mods
            .map(ModInfo::getModId)
            .any { it == modID }
        else ModList.get().isLoaded(modID)

        cacheMod[modID] = loaded
        return loaded
    }

    fun check(mixinClassName: String): Boolean {
        if (cacheDependency.containsKey(mixinClassName)) return cacheDependency[mixinClassName]!!

        val dependencies = doCheck(mixinClassName)

        if (!dependencies.annotated) {
            cacheDependency[mixinClassName] = true
            return true
        }

        Logger.debug(
            "Found @MixinDependencies" +
                    "{MixinClass[{}], requireMods{}, conflictMods{}}",
            mixinClassName.splitToLastKey("core", "\\."),
            dependencies.requires, dependencies.conflicts
        )

        for (requiredMod in dependencies.requires) {
            if (!isModLoaded(requiredMod)) {
                cacheDependency[mixinClassName] = false
                return false
            }
        }

        for (conflictMod in dependencies.conflicts) {
            if (isModLoaded(conflictMod)) {
                cacheDependency[mixinClassName] = false
                return false
            }
        }

        cacheDependency[mixinClassName] = true
        return true
    }

    private fun doCheck(mixinClassName: String): InfoDependency {
        val dependencies = InfoDependency()
        val classPath = mixinClassName.replace('.', '/') + ".class"

        if (!::loader.isInitialized) loader = Thread.currentThread().getContextClassLoader()

        try {
            loader.getResourceAsStream(classPath).use { streamInput ->
                if (streamInput != null) {
                    val classReader = ClassReader(streamInput)
                    classReader.accept(
                        VisitorClass(dependencies),
                        ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
                    )
                }
            }
        } catch (_: IOException) {
            Logger.warn("Failed to read mixin class: {}", mixinClassName)
        }

        return dependencies
    }

    private class VisitorClass(
        private val dependencies: InfoDependency
    ) : ClassVisitor(Opcodes.ASM9) {
        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            if ("Lcom/fish/fishlib/mixin/MixinDependencies;" != descriptor) return null
            dependencies.annotated = true
            return VisitorAnnotation(dependencies)
        }
    }

    private class VisitorAnnotation(
        private val dependencies: InfoDependency
    ) : AnnotationVisitor(Opcodes.ASM9) {
        override fun visitArray(name: String?): AnnotationVisitor? {
            if ("value" != name && "conflict" != name) return null
            return VisitorValue(name, dependencies)
        }
    }

    private class VisitorValue(
        private val name: String,
        private val dependencies: InfoDependency
    ) : AnnotationVisitor(Opcodes.ASM9) {
        override fun visit(name: String?, value: Any?) {
            if (value !is String) return

            if ("value" == this.name) dependencies.requires.add(value)
            else if ("conflict" == this.name) dependencies.conflicts.add(value)
        }
    }
}
