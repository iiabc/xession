package com.hiusers.mc.xession.session.questengine

data class SessionEntity(
    val content: SessionEntityContent = SessionEntityContent(),
    val answer: SessionEntityAnswer = SessionEntityAnswer(),
    val option: SessionEntityOption = SessionEntityOption(),
)

data class SessionEntityContent(
    val layout: String = "",
    val variable: Map<String, String> = mapOf(),
)

data class SessionEntityAnswer(
    val layout: SessionEntityAnswerLayout = SessionEntityAnswerLayout(),
)

data class SessionEntityAnswerLayout(
    val common: List<String> = listOf(),
    val select: List<String> = listOf(),
    val variable: Map<String, String> = mapOf(),
)

data class SessionEntityOption(
    val animation: SessionEntityOptionAnimation = SessionEntityOptionAnimation()
)

data class SessionEntityOptionAnimation(
    val enable: Boolean = true,
    val speed: Int = 1
)