package com.hiusers.mc.xession.session.chemdah

import com.hiusers.mc.xession.api.mode.SessionModeManager
import com.hiusers.mc.xession.api.reader.SessionSetting
import com.hiusers.mc.xession.session.chemdah.TalkFont.npcTalkFont
import com.hiusers.xerr.api.container.BossbarLayoutContainer
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.conversation.theme.Theme
import ink.ptms.chemdah.core.conversation.theme.ThemeSettings
import taboolib.common.platform.function.submit
import taboolib.common5.util.printed
import taboolib.module.chat.colored
import java.util.concurrent.CompletableFuture

class ThemeFont : Theme<ThemeSettings>() {

    override fun createConfig(): ThemeSettings {
        val config = ConversationManager.conf
        // 不使用配置
        return object : ThemeSettings(
            config.getConfigurationSection("theme-xerr") ?: config.getConfigurationSection("theme-chat")!!
        ) {
        }
    }

    override fun onDisplay(session: Session, message: List<String>, canReply: Boolean): CompletableFuture<Void> {
        val sessionEntity = SessionSetting.sessionEntity ?: return CompletableFuture.completedFuture(null)
        val future = CompletableFuture<Void>()
        // 延迟
        var d = 0L
        var d2 = 0L
        // 取消
        var isCancelled = false
        // 标记为 NPC 正在发言
        session.npcTalking = true
        message.colored().map {
            // 是否播放
            if (sessionEntity.option.animation.enable) {
                it.printed()
            } else {
                listOf(it)
            }
        }.forEachIndexed { index, messageText ->
            messageText.forEachIndexed { printIndex, printMessage ->
                // 是否最终消息 —— 停止动画
                val stopAnimation = printIndex + 1 == messageText.size
                // 延迟发送
                submit(delay = sessionEntity.option.animation.speed * d++) {
                    // 会话合法
                    if (session.isValid) {
                        // 如果 NPC 正在发言，则向玩家发送
                        if (session.npcTalking) {
                            future.npcTalkFont(
                                session,
                                message,
                                printMessage,
                                index,
                                stopAnimation,
                                sessionEntity
                            )
                        }
                        // 跳过对话
                        else if (!isCancelled) {
                            isCancelled = true
                            // 打印完整的消息
                            future.npcTalkFont(
                                session,
                                session.npcSide,
                                printMessage = "",
                                lineIndex = session.npcSide.size,
                                stopAnimation = true,
                                sessionEntity
                            )
                            future.complete(null)
                        }
                    }
                    // 对话中断
                    else if (!isCancelled) {
                        isCancelled = true
                        BossbarLayoutContainer.removeLayout(session.player, "xession")
                        future.complete(null)
                    }
                }
            }
            d2 += d
        }
        // 消息发送结束后解除标签
        future.thenAccept {
            session.npcTalking = false
        }
        if (d == 0L) {
            future.complete(null)
        }
        return future
    }

    override fun onBegin(session: Session): CompletableFuture<Void> {
        val player = session.player
        player.inventory.heldItemSlot = ConversationManager.conf.getInt("theme-xerr.init-slot", 4)
        SessionModeManager.play(player)
        return super.onBegin(session)
    }

    override fun onClose(session: Session): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        future.thenApply {
            val player = session.player
            BossbarLayoutContainer.removeLayout(player, "xession")
            SessionModeManager.cancel(player)
        }
        future.complete(null)
        return future
    }

}