package com.hiusers.mc.xession.serializer

import com.google.gson.*
import taboolib.common.util.Location
import java.lang.reflect.Type

class LocationSerializer : JsonSerializer<Location>, JsonDeserializer<Location> {
    override fun serialize(src: Location, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonObject().apply {
            addProperty("world", src.world)
            addProperty("x", src.x)
            addProperty("y", src.y)
            addProperty("z", src.z)
            addProperty("yaw", src.yaw)
            addProperty("pitch", src.pitch)
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Location {
        val obj = json.asJsonObject
        val world = obj.get("world").asString
        val x = obj.get("x").asDouble
        val y = obj.get("y").asDouble
        val z = obj.get("z").asDouble
        val yaw = obj.get("yaw").asFloat
        val pitch = obj.get("pitch").asFloat
        return Location(world, x, y, z, yaw, pitch)
    }
}
