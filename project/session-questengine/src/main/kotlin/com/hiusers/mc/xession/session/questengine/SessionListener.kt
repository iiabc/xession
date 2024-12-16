package com.hiusers.mc.xession.session.questengine

import com.hiusers.mc.xession.api.Select.updateSelection
import com.hiusers.mc.xession.reader.PluginReader.hasQuestEngine
import com.hiusers.questengine.api.conversation.SessionManager.getSession
import com.hiusers.questengine.api.conversation.reader.ConversationReader
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType
import org.bukkit.event.player.PlayerItemHeldEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object SessionListener {

    /**
     * 监听滚轮
     */
    @SubscribeEvent(EventPriority.HIGHEST, ignoreCancelled = true)
    fun scroll(ev: PlayerItemHeldEvent) {
        val p = ev.player
        if (hasQuestEngine) {
            val session = p.getSession() ?: return
            if (session.theme?.style?.lowercase() != "xerr") return
            val answerSize = session.passAnswer.size
            val selected = session.selected
            val newSelected = updateSelection(selected, ev.newSlot, ev.previousSlot, answerSize)
            if (newSelected != selected) {
                session.selected = newSelected
                session.answer()
            }
            ev.isCancelled = true
        }
    }

    /**
     * 点击选中
     */
    @SubscribeEvent
    fun clickReply(ev: PlayerAnimationEvent) {
        if (ev.animationType == PlayerAnimationType.ARM_SWING) {
            if (hasQuestEngine) {
                val p = ev.player
                if (ConversationReader.themeChatClick) {
                    val session = p.getSession() ?: return
                    if (session.theme?.style?.lowercase() != "xerr") return
                    session.selectAnswerAction()
                }
            }
        }
    }

}