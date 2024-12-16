package com.hiusers.mc.xession.reader

import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration

object ConfigReader {

    @Config("config.yml", autoReload = true)
    lateinit var config: Configuration
        private set

    @ConfigNode("bossbar.color")
    var bossBarColor: String = "YELLOW"

}