package com.hiusers.mc.xession.kether

import org.bukkit.entity.Player
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.script

object ActionFrame {

    fun ScriptFrame.player() = script().sender?.castSafely<Player>() ?: error("unknown player")

}