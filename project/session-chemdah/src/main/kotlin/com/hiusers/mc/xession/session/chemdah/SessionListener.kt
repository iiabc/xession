package com.hiusers.mc.xession.session.chemdah

import com.hiusers.mc.xession.api.Select.updateSelection
import com.hiusers.mc.xession.api.SessionSetting
import com.hiusers.mc.xession.reader.ConfigReader
import com.hiusers.mc.xession.reader.PluginReader.hasChemdah
import com.hiusers.mc.xession.session.chemdah.TalkFont.npcTalkFont
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.core.conversation.Session
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType
import org.bukkit.event.player.PlayerItemHeldEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.PacketSendEvent
import java.util.concurrent.CompletableFuture

object SessionListener {

    /**
     * 监听滚轮
     */
    @SubscribeEvent(EventPriority.HIGHEST, ignoreCancelled = true)
    fun scroll(ev: PlayerItemHeldEvent) {
        if (hasChemdah) {
            val session = ev.player.conversationSession ?: return
            val sessionEntity = SessionSetting.sessionEntity ?: return
            if (session.conversation.theme is ThemeFont) {
                if (session.npcTalking) {
                    // 是否不允许玩家跳过对话演出效果
                    if (session.conversation.hasFlag("NO_SKIP", "FORCE_DISPLAY")) {
                        return
                    }
                    session.npcTalking = false
                }
                val replies = session.playerReplyForDisplay
                if (replies.isNotEmpty()) {
                    val answerSize = replies.size
                    val selected = replies.indexOf(session.playerReplyOnCursor)
                    val newSelected = updateSelection(selected, ev.newSlot, ev.previousSlot, answerSize)

                    if (newSelected != selected) {
                        session.playerReplyOnCursor = replies[newSelected]
                        CompletableFuture<Void>().npcTalkFont(
                            session,
                            session.npcSide,
                            "",
                            session.npcSide.size,
                            stopAnimation = true,
                            sessionEntity = sessionEntity
                        )
                    }
                }
                ev.isCancelled = true
            }
        }
    }

    /**
     * 点击选中
     */
    @SubscribeEvent
    fun clickReply(ev: PlayerAnimationEvent) {
        if (ev.animationType == PlayerAnimationType.ARM_SWING) {
            if (hasChemdah) {
                val session = ev.player.conversationSession ?: return
                val theme = session.conversation.theme
                if (theme is ThemeFont) {
                    ev.isCancelled = true
                    selectReply(session)
                }
            }
        }
    }

    /**
     * 选定应答
     */
    private fun selectReply(session: Session) {
        // 正在发言
        if (session.npcTalking) {
            // 跳过对话演出效果
            if (session.conversation.noFlag("NO_SKIP", "FORCE_DISPLAY")) {
                session.npcTalking = false
            }
        } else {
            val cursor = session.playerReplyOnCursor
            cursor?.check(session)?.thenAccept {
                cursor.select(session)
            }
        }
    }

    @SubscribeEvent
    fun blockServerPrevent(ev: PacketSendEvent) {
        if (ev.packet.name == "ClientboundSystemChatPacket") {
            if (ev.packet.read<Boolean>("overlay") == true) {
                if (hasChemdah && ConfigReader.bossBarPreventActionBar) {
                    val session = ev.player.conversationSession ?: return
                    val theme = session.conversation.theme
                    if (theme is ThemeFont) {
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
            if (hasChemdah && ConfigReader.bossBarPreventActionBar) {
                val session = ev.player.conversationSession ?: return
                val theme = session.conversation.theme
                if (theme is ThemeFont) {
                    ev.isCancelled = true
                }
            }
        }
    }

}