package com.hiusers.mc.xession.session.questengine

import com.hiusers.mc.xession.api.BossBarContainer
import com.hiusers.mc.xession.api.SessionSetting
import com.hiusers.mc.xession.kether.ActionUtil.parseScript
import com.hiusers.mc.xession.kether.ActionUtil.runAction
import com.hiusers.questengine.api.conversation.ActionManager.action
import com.hiusers.questengine.api.conversation.Session
import com.hiusers.questengine.api.conversation.SessionManager.updateSession
import com.hiusers.questengine.api.conversation.entity.AnswerEntity
import com.hiusers.questengine.api.conversation.theme.ConversationTheme
import com.hiusers.xerr.api.builder.ComponentBuilder.buildRaw
import com.hiusers.xerr.api.container.LayoutContainer
import com.hiusers.xerr.api.container.SessionConfig
import org.bukkit.entity.Player
import taboolib.common5.util.printed
import taboolib.module.chat.ComponentText
import taboolib.module.nms.setRawTitle
import taboolib.platform.compat.replacePlaceholder

class ConversionFont : ConversationTheme {

    override val style = "xerr"

    override val animation = true

    override val time = 1L

    override fun renderContent(session: Session): List<Any> {
        val themeConfig = SessionSetting.sessionEntity ?: return emptyList()

        val renderContent = mutableListOf<ComponentText>()

        val player = session.player
        val name = session.name ?: "{name}"

        val conversation = session.conversation

        // 提取对话内容
        val list = conversation.content

        // 替换变量
        val themeVariableMap = themeConfig.content.variable.toMutableMap()
        themeVariableMap.filterValues { it == "{name}" }.forEach { (key, _) ->
            themeVariableMap[key] = name
        }
        val variableMap = themeVariableMap.toMutableMap()

        // 遍历对话行内容
        list.forEachIndexed { index, s ->
            val text = player.parseScript(s).replacePlaceholder(player)

            themeVariableMap.keys.forEachIndexed { j, _ ->
                val finder = "{text_$j}"
                themeVariableMap.filterValues { it == finder }.forEach { (key, _) ->
                    variableMap[key] = ""
                }
            }

            // 打印
            text.printed().forEach {
                themeVariableMap.filterValues { v -> v == "{text_$index}" }.forEach { (key, _) ->
                    variableMap[key] = it
                }
                LayoutContainer.buildComponentText(player, themeConfig.content.layout, variableMap)?.let { comp ->
                    conversation.tags.forEach {
                        LayoutContainer.buildComponentText(player, it)?.let { tagsComp ->
                            comp.append(tagsComp)
                        }
                    }
                    renderContent.add(comp)
                }
            }
            // 更新模板，此行是最后，进行完整填充，为下一行对话做准备
            // 填充
            themeVariableMap.filterValues { it == "{text_$index}" }.forEach { (key, _) ->
                themeVariableMap[key] = text
            }
        }

        return renderContent

    }

    override fun renderAnswer(player: Player, passAnswer: List<AnswerEntity>): List<Any> {
        val themeConfig = SessionConfig.getConfig() ?: return emptyList()
        val renderAnswer = mutableListOf<ComponentText>()

        val answerLayout = themeConfig.answer.layout
        val commonLayout = answerLayout.common
        val selectLayout = answerLayout.select

        for (answerIndex in passAnswer.indices) {
            // 渲染应答
            val componentText = ComponentText.empty()

            // 处理每个应答的渲染
            passAnswer.forEachIndexed { index, answerEntity ->
                // 当前行的文本
                val text = player.parseScript(answerEntity.text).replacePlaceholder(player)

                // 处理变量映射替换
                val variableMap = answerLayout.variable.toMutableMap().apply {
                    filterValues { it == "{answer}" }.forEach { (key, _) ->
                        this[key] = text
                    }
                }

                // 渲染公共部分
                if (index < commonLayout.size) {
                    LayoutContainer.buildComponentText(player, commonLayout[index], variableMap)?.let { comp ->
                        componentText.append(comp)
                    }

                    // 如果当前行是选中的，渲染选中部分
                    if (index == answerIndex && index < selectLayout.size) {
                        LayoutContainer.buildComponentText(player, selectLayout[index])?.let { comp ->
                            componentText.append(comp)
                        }
                    }
                }
            }

            // 将渲染结果添加到渲染列表
            renderAnswer.add(componentText)
        }

        return renderAnswer
    }


    override fun renderContentAnimation(session: Session): List<Any> {
        return renderContent(session)
    }

    override fun selectAnswerAction(session: Session) {
        // 处于选择模式
        if (session.selecting) {
            val player = session.player
            // 从通过条件的应答配置列表提取
            val answer = session.passAnswer.getOrNull(session.selected)
            if (answer != null) {
                val answerCase = answer.`when`
                // 逻辑动作
                if (answerCase.isNotEmpty()) {
                    // 暂停，不退出会话，因为可能要跳转会话
                    if (answerCase.action(player)) session.pause()
                } else {
                    // 无逻辑动作就直接执行
                    val answerSend = answer.open
                    if (answerSend.isNotEmpty()) {
                        // 跳转会话
                        player.updateSession(answerSend)
                    } else {
                        // 退出会话
                        session.exit()
                    }
                    val answerAction = answer.action
                    if (answerAction.isNotEmpty()) {
                        player.runAction(answerAction)
                    }
                }
            } else {
                session.exit()
            }
        }
    }

    override fun sendContent(player: Player, content: Any) {
        val text = (content as ComponentText).buildRaw()
        val bossbar = BossBarContainer.get(player) ?: return
        bossbar.setRawTitle(text)
    }

    override fun answer(session: Session, renderContent: List<Any>, renderAnswer: List<Any>) {
        if (renderContent.isEmpty()) {
            session.exit()
            return
        }
        session.selecting = true
        val selected = session.selected
        val player = session.player
        // 发送最后一行对话内容和全部应答
        val text = ComponentText.empty().append(renderContent[renderContent.size - 1] as ComponentText)
        if (renderAnswer.isNotEmpty()) {
            if (renderAnswer.size > selected) {
                text.append(renderAnswer[selected] as ComponentText)
            }
        }
        val raw = text.buildRaw()
        val bossbar = BossBarContainer.get(player) ?: return
        bossbar.setRawTitle(raw)
    }

    override fun preExitAction(session: Session) {
        BossBarContainer.remove(session.player)
    }

    override fun preSendAction(session: Session) {
        BossBarContainer.add(session.player)
    }

}