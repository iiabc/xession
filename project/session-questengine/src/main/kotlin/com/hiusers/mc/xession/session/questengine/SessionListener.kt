package com.hiusers.mc.xession.session.questengine

import com.hiusers.mc.xession.api.Select.updateSelection
import com.hiusers.mc.xession.reader.ConfigReader
import com.hiusers.mc.xession.reader.PluginReader.hasQuestEngine
import com.hiusers.questengine.api.conversation.SessionManager.getSession
import com.hiusers.questengine.api.conversation.reader.ConversationReader
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType
import org.bukkit.event.player.PlayerItemHeldEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.PacketSendEvent

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
            if (!session.selecting) return
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

    /**
     * 屏蔽操作栏
     */
    @SubscribeEvent
    fun blockServerPrevent(ev: PacketSendEvent) {
        if (ev.packet.nameInSpigot == "ClientboundSystemChatPacket") {
            if (ev.packet.read<Boolean>("overlay") == true) {
                if (hasQuestEngine && ConfigReader.preventActionBar) {
                    if (isSession(ev.player)) {
                        val source = ev.packet.source.toString()
                        if (source.contains("""content={"text":""}""") || source.contains("content=empty")) {
                            return
                        }
                        ev.isCancelled = true
                    }
                }
            }
        }
    }

    /**
     * 屏蔽系统级别的操作栏
     */
    @SubscribeEvent
    fun preventSystem(ev: PacketSendEvent) {
        if (ev.packet.nameInSpigot == "ClientboundSetActionBarTextPacket") {
            if (hasQuestEngine && ConfigReader.preventActionBar) {
                if (isSession(ev.player)) {
                    ev.isCancelled = true
                }
            }
        }
    }

    @SubscribeEvent
    fun cameraClick(ev: PacketReceiveEvent) {
        if (ev.packet.nameInSpigot == "PacketPlayInUseEntity") {
            // 避免在相机状态下点击发生错误
            if (ConfigReader.supportPacket && ConfigReader.sessionPacket) {
                if (hasQuestEngine) {
                    if (isSession(ev.player)) {
                        ev.isCancelled = true
                    }
                }
            }
        }
    }

    private fun isSession(player: Player): Boolean {
        val session = player.getSession() ?: return false
        return session.theme?.style?.lowercase() == "xerr"
    }

}