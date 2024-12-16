package com.hiusers.mc.xession.session.chemdah

import com.hiusers.mc.xession.api.BossBarContainer
import com.hiusers.mc.xession.api.SessionEntity
import com.hiusers.mc.xession.reader.PluginReader.hasChemdah
import com.hiusers.xerr.api.builder.ComponentBuilder.buildRaw
import com.hiusers.xerr.api.container.LayoutContainer
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Session
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.nms.setRawTitle
import java.util.concurrent.CompletableFuture

object TalkFont {

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        // 注册会话风格
        if (hasChemdah) {
            ThemeFont().register("xerr")
        }
    }

    private fun closeDistance(): Double {
        return ConversationManager.conf.getDouble("theme-xerr.close-distance", 1.0)
    }

    /**
     * 发送对话
     *
     * @param session 会话对象
     * @param messages 所有信息
     * @param printMessage 正在打印的信息
     * @param lineIndex 打印信息在所有信息中的索引
     * @param stopAnimation 本行信息的打印是否结束
     */
    fun CompletableFuture<Void>.npcTalkFont(
        session: Session,
        messages: List<String>,
        printMessage: String,
        lineIndex: Int,
        stopAnimation: Boolean,
        sessionEntity: SessionEntity
    ) {

        if (session.distance > closeDistance()) {
            session.close()
            complete(null)
            return
        }

        val answerLayout = sessionEntity.answer.layout
        val commonLayout = answerLayout.common
        val selectLayout = answerLayout.select

        val themeVariableMap = sessionEntity.content.variable.toMutableMap()
        themeVariableMap.filterValues { it == "{name}" }.forEach { (key, _) ->
            themeVariableMap[key] = session.title
        }
        val variableMap = themeVariableMap.toMutableMap()

        // lineIndex 之后的文本为空
        themeVariableMap.keys.forEachIndexed { j, _ ->
            val finder = "{text_$j}"
            themeVariableMap.filterValues { it == finder }.forEach { (key, _) ->
                variableMap[key] = ""
            }
        }

        // 获取有效回复
        session.conversation.playerSide.checkReply(session).thenApply { replies ->

            // 添加可用回复
            session.playerReplyForDisplay.clear()
            session.playerReplyForDisplay.addAll(replies)
            if (replies.isNotEmpty() && session.playerReplyOnCursor == null) {
                session.playerReplyOnCursor = replies.first()
            }

            // 最终效果
            val animationStopped = lineIndex + 1 >= messages.size && stopAnimation
            var component = ""
            try {

                themeVariableMap.filterValues { v -> v == "{text_$lineIndex}" }.forEach { (key, _) ->
                    variableMap[key] = printMessage
                }
                // lineIndex 之前的文本填充内容
                for (i in 0 until lineIndex) {
                    themeVariableMap.filterValues { v -> v == "{text_$i}" }.forEach { (key, _) ->
                        variableMap[key] = messages[i]
                    }
                }

                LayoutContainer.buildComponentString(session.player, sessionEntity.content.layout, variableMap)
                    ?.let { comp ->
                        component += comp
                    }

                // 播放结束后显示应答
                if (animationStopped) {
                    replies.forEachIndexed { replyIndex, reply ->
                        // 处理变量映射替换
                        val answerVariable = answerLayout.variable.toMutableMap().apply {
                            filterValues { it == "{answer}" }.forEach { (key, _) ->
                                this[key] = reply.text
                            }
                        }
                        LayoutContainer.buildComponentString(session.player, commonLayout[replyIndex], answerVariable)
                            ?.let { comp ->
                                component += comp
                            }

                        // 如果当前行是选中的，渲染选中部分
                        if (session.playerReplyOnCursor?.equals(reply) == true) {
                            LayoutContainer.buildComponentText(session.player, selectLayout[replyIndex], answerVariable)
                                ?.let { comp ->
                                    component += comp
                                }
                        }
                    }
                }

            } catch (e: Throwable) {
                e.printStackTrace()
            }

            BossBarContainer.get(session.player)?.setRawTitle(component.buildRaw())

            // 打印完成则结束演示
            if (animationStopped) {
                complete(null)
            }
        }
    }

}