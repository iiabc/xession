package com.hiusers.mc.xession.nms

import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.module.nms.nmsProxy

abstract class PacketMinecraft {

    /**
     * 生成实体
     */
    abstract fun createEntity(player: Player, entityId: Int, location: Location)

    /**
     * 销毁实体
     */
    abstract fun destroyEntity(player: Player, entityId: Int)

    abstract fun camera(player: Player, entityId: Int)

    /**
     * 切换游戏模式
     * @param mode 0: 生存模式 1: 创造模式 2: 冒险模式 3: 旁观者
     */
    abstract fun changeGameMode(player: Player, mode: Int)

    companion object {

        val INSTANCE by lazy {
            nmsProxy<PacketMinecraft>()
        }
    }


}