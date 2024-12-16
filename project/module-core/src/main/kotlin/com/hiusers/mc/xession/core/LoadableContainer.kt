package com.hiusers.mc.xession.core

import com.hiusers.mc.xession.api.SessionSetting
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

object LoadableContainer {

    @Awake(LifeCycle.ENABLE)
    fun load() {
        SessionSetting.load()
    }

    @Awake(LifeCycle.DISABLE)
    fun unload() {
        SessionSetting.unload()
    }

    fun reset() {
        unload()
        load()
    }

}