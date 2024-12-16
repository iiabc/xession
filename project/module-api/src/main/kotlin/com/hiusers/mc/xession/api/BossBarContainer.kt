package com.hiusers.mc.xession.api

import com.hiusers.mc.xession.reader.ConfigReader
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import java.util.*

object BossBarContainer {

    private val map = mutableMapOf<UUID, BossBar>()

    fun add(player: Player) {
        val uuid = player.uniqueId
        if (map.containsKey(uuid)) return

        val bossbar = Bukkit.createBossBar("", BarColor.valueOf(ConfigReader.bossBarColor.uppercase()), BarStyle.SOLID)
        bossbar.addPlayer(player)

        map[uuid] = bossbar
    }

    fun remove(player: Player) {
        val uuid = player.uniqueId
        if (!map.containsKey(uuid)) return

        map[uuid]?.removePlayer(player)
        map.remove(uuid)
    }

    fun get(player: Player, create: Boolean = false): BossBar? {
        val uuid = player.uniqueId
        if (!map.containsKey(uuid) && create) add(player)
        return map[uuid]
    }

    fun clear() {
        map.forEach { (_, bossbar) ->
            bossbar.removeAll()
        }
        map.clear()
    }

}