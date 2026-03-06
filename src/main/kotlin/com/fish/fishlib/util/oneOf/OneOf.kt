package com.fish.fishlib.util.oneOf

import com.fish.fishlib.util.extension.unit
import com.fish.fishlib.util.oneOf.OneOf2.A
import com.fish.fishlib.util.oneOf.OneOf2.B
import com.fish.fishlib.util.oneOf.OneOf3.*
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec

private fun <T> identity(): (T) -> T = { it }

interface OneOf {
    fun unwrap(): Any?

    fun isEmpty() = this.unwrap() == null

    fun ifEmpty(action: () -> Unit) = if (isEmpty()) action() else Unit
}

sealed class OneOf2<A, B> : OneOf {
    abstract fun <C, D> map(mapperA: (A) -> C?, mapperB: (B) -> D?): OneOf2<C, D>

    open fun <V> flatMap(mapperA: (A) -> V?, mapperB: (B) -> V?): V? = null

    override fun unwrap() = this.a ?: this.b

    open fun ifA(mapper: (A) -> Unit) = this

    open fun ifB(mapper: (B) -> Unit) = this

    open val a: A? = null

    open val b: B? = null

    fun <V> mapA(mapper: (A) -> V) = this.map(mapper, identity())

    fun <V> mapB(mapper: (B) -> V) = this.map(identity(), mapper)

    companion object {
        fun <A, B> empty() = Empty<A, B>()

        fun <A, B> a(value: A?) = if (value == null) empty() else A<A, B>(value)

        fun <A, B> b(value: B?) = if (value == null) empty() else B<A, B>(value)

        fun <A, B> OneOf2<A, B>.toEither(wrap: Boolean = false): Either<A, B>? = this.flatMap(
            Either<A, B>::left, Either<A, B>::right
        )

        fun <A, B> Either<A, B>.oneOf(): OneOf2<A, B> = this.map(::a, ::b)

        fun <A : Any, B : Any> mapCodec(
            name: String, codecA: Codec<A>, codecB: Codec<B>
        ) = CodecFieldOneOf2(name, codecA, codecB)

        fun <T : ByteBuf, A : Any, B : Any> streamCodec(
            codecA: StreamCodec<in T, A>, codecB: StreamCodec<in T, B>
        ) = StreamCodecOneOf2(codecA, codecB)
    }

    class Empty<A, B> internal constructor() : OneOf2<A, B>() {
        override fun <C, D> map(mapperA: (A) -> C?, mapperB: (B) -> D?) = Empty<C, D>()

        override fun isEmpty() = true
    }

    class A<A, B> internal constructor(private val value: A) : OneOf2<A, B>() {
        override fun <C, D> map(mapperA: (A) -> C?, mapperB: (B) -> D?) = a<C, D>(mapperA(this.value))

        override fun <V> flatMap(mapperA: (A) -> V?, mapperB: (B) -> V?) = mapperA(this.value)

        override fun ifA(mapper: (A) -> Unit) = mapper(this.value).unit(this)

        override val a = this.value
    }

    class B<A, B> internal constructor(private val value: B) : OneOf2<A, B>() {
        override fun <C, D> map(mapperA: (A) -> C?, mapperB: (B) -> D?) = b<C, D>(mapperB(this.value))

        override fun <V> flatMap(mapperA: (A) -> V?, mapperB: (B) -> V?) = mapperB(this.value)

        override fun ifB(mapper: (B) -> Unit) = mapper(this.value).unit(this)

        override val b = this.value
    }
}

sealed class OneOf3<A, B, C> : OneOf {
    abstract fun <D, E, F> map(mapperA: (A) -> D?, mapperB: (B) -> E?, mapperC: (C) -> F?): OneOf3<D, E, F>

    open fun <V> flatMap(mapperA: (A) -> V?, mapperB: (B) -> V?, mapperC: (C) -> V?): V? = null

    override fun unwrap() = this.a ?: this.b ?: this.c

    open fun ifA(mapper: (A) -> Unit) = this

    open fun ifB(mapper: (B) -> Unit) = this

    open fun ifC(mapper: (C) -> Unit) = this

    open val a: A? = null

    open val b: B? = null

    open val c: C? = null

    fun <V> mapA(mapper: (A) -> V) = this.map(
        mapper, identity(), identity()
    )

    fun <V> mapB(mapper: (B) -> V) = this.map(
        identity(), mapper, identity()
    )

    fun <V> mapC(mapper: (C) -> V) = this.map(
        identity(), identity(), mapper
    )

    companion object {
        fun <A, B, C> empty() = Empty<A, B, C>()

        fun <A, B, C> a(value: A?) = if (value == null) empty() else A<A, B, C>(value)

        fun <A, B, C> b(value: B?) = if (value == null) empty() else B<A, B, C>(value)

        fun <A, B, C> c(value: C?) = if (value == null) empty() else C<A, B, C>(value)

        fun <A : Any, B : Any, C : Any> mapCodec(
            name: String, codecA: Codec<A>, codecB: Codec<B>, codecC: Codec<C>
        ) = CodecFieldOneOf3(name, codecA, codecB, codecC)

        fun <T : ByteBuf, A : Any, B : Any, C : Any> streamCodec(
            codecA: StreamCodec<in T, A>, codecB: StreamCodec<in T, B>, codecC: StreamCodec<in T, C>
        ) = StreamCodecOneOf3(codecA, codecB, codecC)
    }

    class Empty<A, B, C> internal constructor() : OneOf3<A, B, C>() {
        override fun <D, E, F> map(
            mapperA: (A) -> D?, mapperB: (B) -> E?, mapperC: (C) -> F?
        ) = Empty<D, E, F>()

        override fun isEmpty() = true
    }

    class A<A, B, C> internal constructor(private val value: A) : OneOf3<A, B, C>() {
        override fun <D, E, F> map(
            mapperA: (A) -> D?, mapperB: (B) -> E?, mapperC: (C) -> F?
        ) = a<D, E, F>(mapperA(this.value))

        override fun <V> flatMap(mapperA: (A) -> V?, mapperB: (B) -> V?, mapperC: (C) -> V?) = mapperA(this.value)

        override fun ifA(mapper: (A) -> Unit) = mapper(this.value).unit(this)

        override val a = this.value
    }

    class B<A, B, C> internal constructor(private val value: B) : OneOf3<A, B, C>() {
        override fun <D, E, F> map(
            mapperA: (A) -> D?, mapperB: (B) -> E?, mapperC: (C) -> F?
        ) = b<D, E, F>(mapperB(this.value))

        override fun <V> flatMap(mapperA: (A) -> V?, mapperB: (B) -> V?, mapperC: (C) -> V?) = mapperB(this.value)

        override fun ifB(mapper: (B) -> Unit) = mapper(this.value).unit(this)

        override val b = this.value
    }

    class C<A, B, C> internal constructor(private val value: C) : OneOf3<A, B, C>() {
        override fun <D, E, F> map(
            mapperA: (A) -> D?, mapperB: (B) -> E?, mapperC: (C) -> F?
        ) = c<D, E, F>(mapperC(this.value))

        override fun <V> flatMap(mapperA: (A) -> V?, mapperB: (B) -> V?, mapperC: (C) -> V?) = mapperC(this.value)

        override fun ifC(mapper: (C) -> Unit) = mapper(this.value).unit(this)

        override val c = this.value
    }
}