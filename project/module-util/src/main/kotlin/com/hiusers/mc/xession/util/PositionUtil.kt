package com.hiusers.mc.xession.util

import org.bukkit.entity.Entity
import taboolib.module.nms.MinecraftVersion.major

object PositionUtil {

    /**
     * @return 旋转结果
     */
    fun Float.rotate(): Float {
        return this * 256.0F / 360.0F
    }

    /**
     * Entity 实体
     * @param entity 其它实体
     * @return 两实体间距离
     */
    fun Entity?.distancePos(entity: Entity): Double {
        this?: return 0.0
        if (major <= 5) {
            return location.distance(entity.location)
        }
        return boundingBox.max.distance(entity.boundingBox.max)
    }

}