package com.hiusers.mc.xession.core

import com.hiusers.mc.xession.session.questengine.ThemeLoader
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

object LoadableContainer {

    @Awake(LifeCycle.ENABLE)
    fun load() {
        ThemeLoader.load()
    }

    @Awake(LifeCycle.DISABLE)
    fun unload() {
        ThemeLoader.unload()
    }

    fun reset() {
        unload()
        load()
    }

}