package com.hiusers.mc.xession.api

object Select {

    fun updateSelection(current: Int, newSlot: Int, previousSlot: Int, answerSize: Int): Int {
        return when {
            newSlot > previousSlot -> (current + 1) % answerSize // 向上选择
            newSlot < previousSlot -> (current - 1 + answerSize) % answerSize // 向下选择
            else -> current // 没有变化
        }
    }

}