package com.fish.fishlib.util.extension

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import java.util.Optional

fun <TBuffer : ByteBuf, TValve : Any> StreamCodec<TBuffer, TValve>.predicated(
    predicate: (TValve) -> Boolean, unit: () -> TValve
): StreamCodec<TBuffer, TValve> = StreamCodec.of<TBuffer, TValve>({ buffer, value ->
    val tested = predicate(value)
    buffer.writeBoolean(tested)

    if (tested) return@of
    encode(buffer, value)
}, { buffer ->
    if (buffer.readBoolean()) return@of unit()
    else return@of decode(buffer)
})

// 这么写有点丑陋(
//        return new OptionalFieldCodec<>("predicated", codec, false)
//                .xmap(value -> value.orElse(unit.get()),
//                        value -> predicate.test(value)  Optional.empty() : Optional.of(value))
//                .codec();
fun <TValue : Any> Codec<TValue>.predicated(
    predicate: (TValue) -> Boolean, unit: () -> TValue
) = object : Codec<TValue> {
    override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<TValue, T>> {
        val predicated = ops.getStringValue(input)
            .map { "predicated_empty" == it }
        if (predicated.isSuccess && predicated.getOrThrow())
            return DataResult.success(Pair<TValue, T>(unit(), input))
        return this@predicated.decode(ops, input)
    }

    override fun <T> encode(input: TValue, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        if (predicate(input))
            return DataResult.success(ops.createString("predicated_empty"))
        return this@predicated.encode(input, ops, prefix)
    }

    override fun toString() = "CodecPredicated[${this@predicated}]"
}

fun <Host, Value : Any> ((Host) -> Value?).optional(): (Host) -> Optional<Value> = {
    Optional.ofNullable(this(it))
}

fun <T : Any> T?.toOptional(): Optional<T> = Optional.ofNullable(this)

fun <T> Optional<T?>.orNull() = orElse(null)