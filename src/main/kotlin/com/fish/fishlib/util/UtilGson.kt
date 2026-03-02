package com.fish.fishlib.util

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.mojang.datafixers.util.Pair
import com.mojang.logging.LogUtils
import com.mojang.serialization.JsonOps
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import java.io.IOException

object UtilGson {
    private val Logger = LogUtils.getLogger()
    private val Gson = GsonBuilder().create()

    @JvmField
    val adapterComponent = object : TypeAdapter<Component?>() {
        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Component?) {
            if (value?.string?.isBlank() ?: true) {
                writer.nullValue()
                return
            }

            val encoded = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, value)
                .resultOrPartial { Logger.error("Failed to encode Components: {}", it) }
            if (encoded.isEmpty) {
                writer.nullValue()
                return
            }

            writer.jsonValue(Gson.toJson(encoded.get()))
        }

        @Throws(IOException::class)
        override fun read(reader: JsonReader): Component = if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            Component.empty()
        } else ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
            .resultOrPartial { Logger.error("Failed to decode Components: {}", it) }
            .orElse(Component.empty())
    }
}
