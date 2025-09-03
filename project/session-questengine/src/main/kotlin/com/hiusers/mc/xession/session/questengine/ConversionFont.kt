package com.hiusers.mc.xession.session.questengine

import com.hiusers.mc.xession.api.mode.SessionModeManager
import com.hiusers.mc.xession.api.reader.SessionSetting.sessionEntity
import com.hiusers.mc.xession.reader.ConfigReader
import com.hiusers.questengine.api.config.conversation.AnswerEntity
import com.hiusers.questengine.api.conversation.Session
import com.hiusers.questengine.api.conversation.theme.ConversationTheme
import com.hiusers.questengine.kether.ActionUtil.parseScriptAsync
import com.hiusers.questengine.kether.ActionUtil.parseScriptListAsync
import com.hiusers.xerr.api.builder.ComponentBuilder.buildRaw
import com.hiusers.xerr.api.container.BossbarLayoutContainer
import com.hiusers.xerr.api.container.LayoutContainer
import org.bukkit.entity.Player
import taboolib.common5.clong
import taboolib.common5.util.printed
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.sendActionBar
import java.util.concurrent.CompletableFuture

class ConversionFont : ConversationTheme<String> {

    override val style = "xerr"

    override val animation = sessionEntity.option.animation.enable

    override val time = (sessionEntity.option.animation.speed).clong

    override fun renderContent(session: Session): CompletableFuture<List<String>> {
        val player = session.player
        val name = session.name ?: "{name}"
        val conversation = session.conversation
        val list = conversation.asLangContent(player)

        // 替换变量
        val themeVariableMap = sessionEntity.content.variable.toMutableMap()
        themeVariableMap.filterValues { it == "{name}" }.forEach { (key, _) ->
            themeVariableMap[key] = name
        }

        val parseFutures = list.map { script ->
            player.parseScriptAsync(script)
                .thenApply { parsedScript -> parsedScript.replacePlaceholder(player) }
        }

        return CompletableFuture.allOf(*parseFutures.toTypedArray())
            .thenApplyAsync {
                val parsedTexts = parseFutures.map { it.join() }
                val variableMap = themeVariableMap.toMutableMap()

                // 处理静态内容渲染
                parsedTexts.forEachIndexed { index, text ->
                    themeVariableMap.filterValues { it == "{text_$index}" }.forEach { (key, _) ->
                        variableMap[key] = text
                    }
                }

                var compStr = ""
                LayoutContainer.buildComponentString(player, sessionEntity.content.layout, variableMap)?.let { comp ->
                    compStr += comp
                    conversation.tags.forEach { tag ->
                        LayoutContainer.buildComponentString(player, tag)?.let { tagsComp ->
                            compStr += tagsComp
                        }
                    }
                }

                mutableListOf(compStr)
            }
    }

    override fun renderContentAnimation(session: Session): CompletableFuture<List<String>> {
        val player = session.player
        val name = session.name ?: "{name}"
        val conversation = session.conversation
        val list = conversation.asLangContent(player)

        // 替换变量
        val themeVariableMap = sessionEntity.content.variable.toMutableMap()
        themeVariableMap.filterValues { it == "{name}" }.forEach { (key, _) ->
            themeVariableMap[key] = name
        }

        return player.parseScriptListAsync(list)
            .thenApply { parsedList -> parsedList.replacePlaceholder(player) }
            .thenApplyAsync { parsedTexts ->
                val allFrames = mutableListOf<String>()

                // 遍历对话行内容生成所有动画帧
                parsedTexts.forEachIndexed { index, text ->
                    text.printed(sessionEntity.option.separator).forEach { printedText ->
                        val variableMap = themeVariableMap.toMutableMap()

                        // 清空其他文本变量
                        themeVariableMap.keys.forEachIndexed { j, _ ->
                            val finder = "{text_$j}"
                            themeVariableMap.filterValues { it == finder }.forEach { (key, _) ->
                                variableMap[key] = ""
                            }
                        }

                        // 设置当前动画文本
                        themeVariableMap.filterValues { it == "{text_$index}" }.forEach { (key, _) ->
                            variableMap[key] = printedText
                        }

                        // 生成当前帧内容
                        var compStr = ""
                        LayoutContainer.buildComponentString(player, sessionEntity.content.layout, variableMap)
                            ?.let { comp ->
                                compStr += comp
                                conversation.tags.forEach { tag ->
                                    LayoutContainer.buildComponentString(player, tag)?.let { tagsComp ->
                                        compStr += tagsComp
                                    }
                                }
                                allFrames.add(compStr)
                            }
                    }

                    // 更新模板变量为完整文本
                    themeVariableMap.filterValues { it == "{text_$index}" }.forEach { (key, _) ->
                        themeVariableMap[key] = text
                    }
                }
                allFrames
            }
    }

    override fun renderAnswer(player: Player, passAnswer: List<AnswerEntity>): CompletableFuture<List<String>> {
        return CompletableFuture.supplyAsync {
            val renderAnswer = mutableListOf<String>()

            val answerLayout = sessionEntity.answer.layout
            val commonLayout = answerLayout.common
            val selectLayout = answerLayout.select

            // 应答渲染逻辑
            for (answerIndex in passAnswer.indices) {
                var componentString = ""

                passAnswer.forEachIndexed { index, answerEntity ->
                    // 异步解析
                    val text = player.parseScriptAsync(answerEntity.asLangAnswerText(player))
                        .thenApply { it.replacePlaceholder(player) }
                        .join()

                    val variableMap = answerLayout.variable.toMutableMap().apply {
                        filterValues { it == "{answer}" }.forEach { (key, _) ->
                            this[key] = text
                        }
                    }

                    if (index < commonLayout.size) {
                        LayoutContainer.buildComponentString(player, commonLayout[index], variableMap)?.let { comp ->
                            componentString += comp
                        }

                        if (index == answerIndex && index < selectLayout.size) {
                            LayoutContainer.buildComponentString(player, selectLayout[index])?.let { comp ->
                                componentString += comp
                            }
                        }
                    }
                }

                renderAnswer.add(componentString)
            }

            renderAnswer
        }
    }

    override fun sendContent(player: Player, content: String) {
        val text = content.buildRaw()
        BossbarLayoutContainer.appendLayoutRaw(player, "xession", text)
    }

    override fun answer(session: Session, renderContent: List<String>, renderAnswer: List<String>): Boolean {
        if (renderContent.isEmpty()) {
            session.exit()
            return true
        }
        session.selecting = true
        val selected = session.selected
        val player = session.player

        // 保持原有内容拼接逻辑
        var text = renderContent[renderContent.size - 1]
        if (renderAnswer.isNotEmpty()) {
            if (renderAnswer.size > selected) {
                text += renderAnswer[selected]
            }
        }
        val raw = text.buildRaw()
        BossbarLayoutContainer.appendLayoutRaw(player, "xession", raw)
        return false
    }

    override fun preExitAction(session: Session) {
        BossbarLayoutContainer.removeLayout(session.player, "xession")
        SessionModeManager.cancel(session.player)
    }

    override fun preSendAction(session: Session) {
        val player = session.player
        if (ConfigReader.preventActionBar) {
            player.sendActionBar("")
        }
        SessionModeManager.play(player)
    }
}
