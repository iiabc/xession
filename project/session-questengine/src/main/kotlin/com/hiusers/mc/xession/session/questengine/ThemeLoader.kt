package com.hiusers.mc.xession.session.questengine

import com.hiusers.mc.xession.reader.PluginReader.hasQuestEngine
import com.hiusers.questengine.api.registry.ConversationRegistry
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

object ThemeLoader {

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        if (hasQuestEngine) {
            ConversationRegistry.registerProvider(ConversionFont())
        }
    }

}