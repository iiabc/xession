package com.hiusers.mc.xession.session.questengine

import com.hiusers.mc.xession.api.mode.SessionModeManager
import com.hiusers.mc.xession.api.reader.SessionSetting
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
import taboolib.common5.util.printed
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.sendActionBar
import java.util.concurrent.CompletableFuture

class ConversionFont : ConversationTheme<String> {

    override val style = "xerr"

    override val animation = true

    override val time = 1L

    override fun renderContent(session: Session): CompletableFuture<List<String>> {
        val player = session.player
        val name = session.name ?: "{name}"
        val conversation = session.conversation
        val list = conversation.asLangContent(player)

        val themeConfig = SessionSetting.sessionEntity ?: return CompletableFuture.completedFuture(emptyList())

        val renderContent = mutableListOf<String>()

        // 替换变量
        val themeVariableMap = themeConfig.content.variable.toMutableMap()
        themeVariableMap.filterValues { it == "{name}" }.forEach { (key, _) ->
            themeVariableMap[key] = name
        }
        val variableMap = themeVariableMap.toMutableMap()

        // 异步处理脚本解析
        player.parseScriptListAsync(list)
        val parseFutures = list.map { script ->
            player.parseScriptAsync(script)
                .thenApply { parsedScript -> parsedScript.replacePlaceholder(player) }
        }

        return CompletableFuture.allOf(*parseFutures.toTypedArray())
            .thenApplyAsync {
                val parsedTexts = parseFutures.map { it.join() }

                // 遍历对话行内容 - 保持原有逻辑不变
                parsedTexts.forEachIndexed { index, text ->
                    text.printed().forEach { printedText ->
                        themeVariableMap.keys.forEachIndexed { j, _ ->
                            val finder = "{text_$j}"
                            themeVariableMap.filterValues { it == finder }.forEach { (key, _) ->
                                variableMap[key] = ""
                            }
                        }

                        themeVariableMap.filterValues { it == "{text_$index}" }.forEach { (key, _) ->
                            variableMap[key] = printedText
                        }

                        // 保持原有LayoutContainer.buildComponentString调用方式
                        var compStr = ""
                        LayoutContainer.buildComponentString(player, themeConfig.content.layout, variableMap)?.let { comp ->
                            compStr += comp
                            conversation.tags.forEach { tag ->
                                LayoutContainer.buildComponentString(player, tag)?.let { tagsComp ->
                                    compStr += tagsComp
                                }
                            }
                            renderContent.add(compStr)
                        }
                    }

                    // 保持原有模板更新逻辑
                    themeVariableMap.filterValues { it == "{text_$index}" }.forEach { (key, _) ->
                        themeVariableMap[key] = text
                    }
                }

                renderContent
            }
    }

    override fun renderAnswer(player: Player, passAnswer: List<AnswerEntity>): CompletableFuture<List<String>> {
        return CompletableFuture.supplyAsync {
            val themeConfig = SessionSetting.sessionEntity ?: return@supplyAsync emptyList()
            val renderAnswer = mutableListOf<String>()

            val answerLayout = themeConfig.answer.layout
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

    override fun renderContentAnimation(session: Session): CompletableFuture<List<String>> {
        return renderContent(session)
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
