package com.hiusers.mc.xession.reader

import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration
import taboolib.module.nms.MinecraftVersion

object ConfigReader {

    @Config("config.yml", autoReload = true)
    lateinit var config: Configuration
        private set

    @ConfigNode("prevent.actionbar")
    var preventActionBar: Boolean = true

    @ConfigNode("session.packet")
    var sessionPacket: Boolean = false

    // 1.19+ 才能支持数据包模式
    val supportPacket = MinecraftVersion.versionId >= 11900

}