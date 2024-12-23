package com.hiusers.mc.xession.api.mode

import com.hiusers.mc.xession.reader.ConfigReader
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent

object SessionModeManager {

    private val sessionModeList = mutableListOf<SessionMode>()

    @SubscribeEvent
    fun onQuit(event: PlayerQuitEvent) {
        sessionModeList.removeIf { it.player == event.player }
    }

    fun play(player: Player) {
        if (ConfigReader.supportPacket && ConfigReader.sessionPacket) {
            val sessionMode = SessionMode(player, player.location)
            sessionMode.play()
            sessionModeList.add(sessionMode)
        }
    }

    fun cancel(player: Player) {
        if (ConfigReader.supportPacket && ConfigReader.sessionPacket) {
            sessionModeList.find {
                it.player == player
            }?.exit()
        }
        sessionModeList.removeIf {
            it.player == player
        }
    }

}