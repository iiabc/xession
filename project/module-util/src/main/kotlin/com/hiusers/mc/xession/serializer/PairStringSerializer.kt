package com.hiusers.mc.xession.serializer

import com.google.gson.*
import java.lang.reflect.Type

class PairStringSerializer: JsonSerializer<Pair<String, String>>, JsonDeserializer<Pair<String, String>> {
    override fun serialize(
        src: Pair<String, String>,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty(src.first, src.second)
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Pair<String, String> {
        val jsonObject = json.asJsonObject
        val key = jsonObject.keySet().firstOrNull()
            ?: throw JsonParseException("JSON object is empty, cannot deserialize Pair<String, String>")

        val value = jsonObject.get(key).asString
        return Pair(key, value)
    }
}