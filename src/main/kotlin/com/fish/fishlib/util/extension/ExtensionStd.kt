package com.fish.fishlib.util.extension

import java.util.function.Supplier

fun <T> MutableCollection<T>.addAll(vararg elements: T) = elements.forEach(this::add)

fun <T> T.onlyIf(predicate: T.() -> Boolean) = if (predicate(this)) this else null

@Suppress("unchecked_cast")
fun <T> Any.cast() = this as T

operator fun <T> Supplier<T>.invoke() = this.get()

fun <L, R, V> ((L, R) -> V).invokeReversed() = { left: R, right: L -> this(right, left) }

fun String.splitToLastKey(keyToMatches: String, pattern: String): String {
    val parts = this.split(pattern.toRegex())

    val keyIndex = parts.indexOf(keyToMatches)
    if (keyIndex == -1) return this

    val remainingParts = parts.subList(keyIndex, parts.size)
    return remainingParts.joinToString(".")
}