package com.hiusers.mc.xession.reader

import org.bukkit.Bukkit

object PluginReader {

    fun hasPlugin(name: String) = Bukkit.getPluginManager().getPlugin(name) != null

    val hasQuestEngine by lazy {
        hasPlugin("QuestEngine")
    }

    val hasChemdah by lazy {
        hasPlugin("Chemdah")
    }

}