package com.fish.fishlib.util

import com.google.common.math.LongMath
import java.math.RoundingMode

object UtilMath {
    fun scale(original: Long, scale: Long, throwOnZero: Boolean = false) = original.let {
        when {
            scale > 0 -> LongMath.saturatedMultiply(it, scale)
            scale < 0 -> try {
                LongMath.divide(it, -scale, RoundingMode.UNNECESSARY)
            } catch (_: ArithmeticException) { it }
            throwOnZero -> throw IllegalArgumentException("Scale can't be 0")
            else -> it
        }
    }
}