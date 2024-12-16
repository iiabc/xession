package com.hiusers.mc.xession.util

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.player
import taboolib.module.lang.sendLang

object BukkitSimple {

    fun ProxyCommandSender.runWithPlayer(context: CommandContext<ProxyCommandSender>, action: Player.() -> Unit) {
        val playerName = context.player("player")
        val player = playerName.castSafely<Player>()
        player?.action() ?: sendLang("player_not_online", playerName)
    }


    fun runWithPlayer(context: CommandContext<ProxyCommandSender>, action: Player.() -> List<String>?): List<String> {
        val playerName = context.player("player")
        val player = playerName.castSafely<Player>()
        return player?.action() ?: listOf()
    }

}