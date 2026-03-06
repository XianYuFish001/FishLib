package com.fish.fishlib.util.extension

import java.util.function.Supplier

fun <T> MutableCollection<T>.addAll(vararg elements: T) = elements.forEach(this::add)

inline fun <T> T?.ifNull(block: () -> T) = this ?: block()

fun <T> T.onlyIf(predicate: (T) -> Boolean) = if (predicate(this)) this else null

@Suppress("unchecked_cast")
fun <T> Any.cast() = this as T

fun Any?.unit() = Unit

fun <T> Any?.unit(unit: T) = unit

operator fun <T> Supplier<T>.invoke() = this.get()

fun <L, R, V> ((L, R) -> V).invokeReversed() = { left: R, right: L -> this(right, left) }

fun String.splitToLastKey(keyToMatches: String, pattern: String): String {
    val parts = this.split(pattern.toRegex())

    val keyIndex = parts.indexOf(keyToMatches)
    if (keyIndex == -1) return this

    val remainingParts = parts.subList(keyIndex, parts.size)
    return remainingParts.joinToString(".")
}

fun String.appendEnd(value: String, acceptsEmpty: Boolean = true): String {
    return if (this.isEmpty() && !acceptsEmpty)
        this
    else if (this.endsWith(value))
        this
    else this + value
}

inline fun Boolean?.ifTrue(block: () -> Unit) = if (this == true) block() else Unit