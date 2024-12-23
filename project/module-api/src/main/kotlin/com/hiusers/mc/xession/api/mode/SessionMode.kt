package com.hiusers.mc.xession.api.mode

import com.hiusers.mc.xession.nms.PacketMinecraft
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.random.Random

data class SessionMode(
    val player: Player,
    val location: Location
) {

    fun play() {
        val instance = PacketMinecraft.INSTANCE
        instance.changeGameMode(player, 3) // 旁观者
        val entityId = Random.nextInt()
        instance.createEntity(player, entityId, location)
        // 固定视角
        instance.camera(player, entityId)
        // 隐藏实体
        instance.destroyEntity(player, entityId)
    }

    fun exit() {
        val instance = PacketMinecraft.INSTANCE
        instance.changeGameMode(player, player.gameMode.value) // 生存模式
        instance.camera(player, player.entityId)
    }

}