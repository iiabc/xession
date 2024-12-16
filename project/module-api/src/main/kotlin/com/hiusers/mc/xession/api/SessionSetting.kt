package com.hiusers.mc.xession.api

import com.hiusers.mc.xession.util.FileUtil
import com.hiusers.mc.xession.util.FileUtil.deserialize
import taboolib.module.configuration.Configuration

object SessionSetting {

    var sessionEntity: SessionEntity? = null

    fun load() {
        val file = FileUtil.getFile("session/theme.yml", true)
        val config = Configuration.loadFromFile(file)
        sessionEntity = config.deserialize(SessionEntity::class.java)
    }

    fun unload() {
        sessionEntity = null
    }

}