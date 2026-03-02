package com.fish.fishlib.mixin

/**
 * awc这depend注解控制怎么这么好用啊😋😋😋
 *
 * awc这asm性能开销怎么这么大啊😭😭😭
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MixinDependencies(vararg val value: String = [], val conflict: Array<String> = []) {
    class InfoDependency {
        @JvmField
        var annotated = false
        @JvmField
        val requires = ArrayList<String>()
        @JvmField
        val conflicts = ArrayList<String>()
    }
}