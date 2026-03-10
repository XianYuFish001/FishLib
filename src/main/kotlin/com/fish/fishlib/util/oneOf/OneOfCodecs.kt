@file:Suppress("unused")

package com.fish.fishlib.util.oneOf

import com.fish.fishlib.util.extension.unit
import com.mojang.serialization.*
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.DecoderException
import net.minecraft.network.codec.StreamCodec
import java.util.stream.Stream

//class CodecOneOf2<A, B>(private val a: Codec<A>, private val b: Codec<B>) : Codec<OneOf2<A, B>> {
//    override fun <T : Any> encode(
//        input: OneOf2<A, B>,
//        ops: DynamicOps<T>,
//        prefix: T
//    ): DataResult<T> = input.flatMap({
//        this.a.encode(it, ops, prefix)
//    }, {
//        this.b.encode(it, ops, prefix)
//    }) : DataResult.success(ops.createString("empty"))
//
//    override fun <T : Any> decode(
//        ops: DynamicOps<T>,
//        input: T
//    ): DataResult<Pair<OneOf2<A, B>, T>> {
//        val resultA = this.a.decode(ops, input).map { it.mapFirst { OneOf2.a<A, B>(it) } }
//        if (resultA.isSuccess) return resultA
//        val resultB = this.b.decode(ops, input).map { it.mapFirst { OneOf2.b<A, B>(it) } }
//        if (resultB.isSuccess) return resultB
//        
//        return when {
//            resultA.hasResultOrPartial() -> resultA
//            resultB.hasResultOrPartial() -> resultB
//            else -> DataResult.error {
//                "Failed to parse OneOf2." +
//                        " A: ${resultA.error().orElseThrow().message()}," +
//                        " B: ${resultB.error().orElseThrow().message()}"
//            }
//        }
//    }
//}

class CodecFieldOneOf2<A : Any, B : Any>(
    private val name: String,
    private val a: Codec<A>,
    private val b: Codec<B>
) : MapCodec<OneOf2<A, B>>() {
    override fun <T : Any> keys(ops: DynamicOps<T>): Stream<T> =
        Stream.of(ops.createString(this.name))

    override fun <T : Any> encode(
        input: OneOf2<A, B>,
        ops: DynamicOps<T>,
        prefix: RecordBuilder<T>
    ) = input.flatMap({
        prefix.add(this.name, this.a.encodeStart(ops, it))
    }, {
        prefix.add(this.name, this.b.encodeStart(ops, it))
    }) ?: prefix

    override fun <T : Any> decode(
        ops: DynamicOps<T>,
        input: MapLike<T>
    ): DataResult<OneOf2<A, B>> {
        val value = input[this.name]
            ?: return DataResult.success(OneOf2.empty<A, B>())

        val resultA = this.a.parse(ops, value)
            .map<OneOf2<A, B>>(OneOf2.Companion::a)
        if (resultA.isSuccess)
            return resultA

        val resultB = this.b.parse(ops, value)
            .map<OneOf2<A, B>>(OneOf2.Companion::b)
        if (resultB.isSuccess)
            return resultB

        return when {
            resultA.hasResultOrPartial() -> resultA
            resultB.hasResultOrPartial() -> resultB
            else -> DataResult.error {
                "Failed to parse OneOf2." +
                        " A: ${resultA.error().orElseThrow().message()}," +
                        " B: ${resultB.error().orElseThrow().message()}"
            }
        }
    }
}

class StreamCodecOneOf2<T : ByteBuf, A : Any, B : Any>(
    private val a: StreamCodec<in T, A>,
    private val b: StreamCodec<in T, B>
) : StreamCodec<T, OneOf2<A, B>> {
    override fun encode(buffer: T, input: OneOf2<A, B>) = input.ifEmpty {
        buffer.writeByte(0)
    }.flatMap({
        buffer.writeByte(1)
        this.a.encode(buffer, it)
    }, {
        buffer.writeByte(2)
        this.b.encode(buffer, it)
    }).unit()

    override fun decode(buffer: T): OneOf2<A, B> = when (buffer.readByte()) {
        0.toByte() -> OneOf2.empty()
        1.toByte() -> OneOf2.a(this.a.decode(buffer))
        2.toByte() -> OneOf2.b(this.b.decode(buffer))
        else -> throw DecoderException(
            "Failed to decode OneOf2 with unknown flag ${
                buffer.getByte(buffer.readerIndex() - 1)
            }"
        )
    }
}

class CodecFieldOneOf3<A : Any, B : Any, C : Any>(
    private val name: String,
    private val a: Codec<A>,
    private val b: Codec<B>,
    private val c: Codec<C>
) : MapCodec<OneOf3<A, B, C>>() {
    override fun <T : Any> keys(ops: DynamicOps<T>): Stream<T> =
        Stream.of(ops.createString(this.name))

    override fun <T : Any> encode(
        input: OneOf3<A, B, C>,
        ops: DynamicOps<T>,
        prefix: RecordBuilder<T>
    ) = input.flatMap({
        prefix.add(this.name, this.a.encodeStart(ops, it))
    }, {
        prefix.add(this.name, this.b.encodeStart(ops, it))
    }, {
        prefix.add(this.name, this.c.encodeStart(ops, it))
    }) ?: prefix

    override fun <T : Any> decode(
        ops: DynamicOps<T>,
        input: MapLike<T>
    ): DataResult<OneOf3<A, B, C>> {
        val value = input[this.name]
            ?: return DataResult.success(OneOf3.empty<A, B, C>())

        val resultA = this.a.parse(ops, value)
            .map<OneOf3<A, B, C>>(OneOf3.Companion::a)
        if (resultA.isSuccess)
            return resultA

        val resultB = this.b.parse(ops, value)
            .map<OneOf3<A, B, C>>(OneOf3.Companion::b)
        if (resultB.isSuccess)
            return resultB

        val resultC = this.c.parse(ops, value)
            .map<OneOf3<A, B, C>>(OneOf3.Companion::c)
        if (resultC.isSuccess)
            return resultC

        return when {
            resultA.hasResultOrPartial() -> resultA
            resultB.hasResultOrPartial() -> resultB
            resultC.hasResultOrPartial() -> resultC
            else -> DataResult.error {
                "Failed to parse OneOf3." +
                        " A: ${resultA.error().orElseThrow().message()}," +
                        " B: ${resultB.error().orElseThrow().message()}," +
                        " C: ${resultC.error().orElseThrow().message()}"
            }
        }
    }
}

class StreamCodecOneOf3<T : ByteBuf, A : Any, B : Any, C : Any>(
    private val a: StreamCodec<in T, A>,
    private val b: StreamCodec<in T, B>,
    private val c: StreamCodec<in T, C>
) : StreamCodec<T, OneOf3<A, B, C>> {
    override fun encode(buffer: T, input: OneOf3<A, B, C>) = input.ifEmpty {
        buffer.writeByte(0)
    }.flatMap({
        buffer.writeByte(1)
        this.a.encode(buffer, it)
    }, {
        buffer.writeByte(2)
        this.b.encode(buffer, it)
    }, {
        buffer.writeByte(3)
        this.c.encode(buffer, it)
    }).unit()

    override fun decode(buffer: T): OneOf3<A, B, C> = when (buffer.readByte()) {
        0.toByte() -> OneOf3.empty()
        1.toByte() -> OneOf3.a(this.a.decode(buffer))
        2.toByte() -> OneOf3.b(this.b.decode(buffer))
        3.toByte() -> OneOf3.c(this.c.decode(buffer))
        else -> throw DecoderException(
            "Failed to decode OneOf3 with unknown flag ${
                buffer.getByte(buffer.readerIndex() - 1)
            }"
        )
    }
}