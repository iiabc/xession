package com.hiusers.mc.xession.nms

import net.minecraft.network.protocol.game.PacketPlayOutCamera
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity
import net.minecraft.world.entity.EntityTypes
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.reflex.Reflex.Companion.unsafeInstance
import taboolib.module.nms.sendPacket
import java.util.*

class PacketMinecraftImpl : PacketMinecraft() {

    override fun createEntity(player: Player, entityId: Int, location: Location) {
        val pitch = (location.pitch * 256.0f / 360.0f).toInt().toByte()
        val yaw = (location.yaw * 256.0f / 360.0f).toInt().toByte()
        player.sendPacket(
            PacketPlayOutSpawnEntity::class.java.unsafeInstance().also {
                it.setProperty("id", entityId)
                it.setProperty("uuid", UUID.randomUUID())
                it.setProperty("type", EntityTypes.VILLAGER)
                it.setProperty("x", location.x)
                it.setProperty("y", location.y)
                it.setProperty("z", location.z)
                it.setProperty("xRot", pitch)
                it.setProperty("yRot", yaw)
                // 1.19才有
                it.setProperty("yHeadRot", yaw)
            }
        )
    }

    override fun destroyEntity(player: Player, entityId: Int) {
        player.sendPacket(
            PacketPlayOutEntityDestroy(entityId)
        )
    }

    override fun camera(player: Player, entityId: Int) {
        player.sendPacket(
            PacketPlayOutCamera::class.java.unsafeInstance().also {
                it.setProperty("cameraId", entityId)
            }
        )
    }

    override fun changeGameMode(player: Player, mode: Int) {
        player.sendPacket(
            PacketPlayOutGameStateChange::class.java.unsafeInstance().also {
                it.setProperty("event", PacketPlayOutGameStateChange.CHANGE_GAME_MODE)
                it.setProperty("param", mode.toFloat())
            }
        )
    }

}