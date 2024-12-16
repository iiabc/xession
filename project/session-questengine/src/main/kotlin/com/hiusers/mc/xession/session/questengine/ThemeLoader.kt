package com.hiusers.mc.xession.session.questengine

import com.hiusers.mc.xession.reader.PluginReader.hasQuestEngine
import com.hiusers.mc.xession.util.FileUtil
import com.hiusers.mc.xession.util.FileUtil.deserialize
import com.hiusers.questengine.api.registry.ConversationRegistry
import taboolib.module.configuration.Configuration

object ThemeLoader {

    var sessionEntity: SessionEntity? = null

    fun load() {
        if (hasQuestEngine) {
            val file = FileUtil.getFile("session/theme.yml", true)
            val config = Configuration.loadFromFile(file)
            sessionEntity = config.deserialize(SessionEntity::class.java)
            ConversationRegistry.registerProvider(ConversionFont())
        }
    }

    fun unload() {
        sessionEntity = null
    }

}