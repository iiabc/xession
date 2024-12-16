package com.hiusers.mc.xession.serializer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import taboolib.common.util.Location

object GsonProvider {

    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Location::class.java, LocationSerializer())
        .registerTypeHierarchyAdapter(PairStringSerializer::class.java, PairStringSerializer())
        .create()

}