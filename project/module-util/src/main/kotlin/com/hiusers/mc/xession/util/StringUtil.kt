package com.hiusers.mc.xession.util

object StringUtil {

    /**
     * 计算字符长度
     */
    fun String.countLength(chinese: Int = 2, other: Int = 1): Int {
        // 计算实际长度：中文字符和中文符号占2个单位长度，其他字符（包括英文、数字、英文符号）占1个单位长度
        var actualLength = 0
        for (char in this) {
            actualLength += if (char.code in 0x4E00..0x9FFF || char.code in 0x3000..0x303F || char.code in 0xFF00..0xFFEF) chinese else other
        }
        return actualLength
    }

    /**
     * 文本填充与截断
     */
    fun fillCutText(text: String, limit: Int, cut: Boolean = true, fill: Boolean = true): String {
        val actualLength = text.countLength()

        return when {
            // 如果实际长度小于限制，填充空格
            fill && actualLength < limit * 2 -> {
                val paddingSpaces = limit * 2 - actualLength
                text.padEnd(text.length + paddingSpaces, ' ')
            }
            // 如果实际长度大于限制，对文本进行截断
            cut && actualLength > limit * 2 -> {
                var currentLength = 0
                val truncatedText = StringBuilder()
                for (char in text) {
                    currentLength += if (char.code in 0x4E00..0x9FFF || char.code in 0x3000..0x303F || char.code in 0xFF00..0xFFEF) 2 else 1
                    if (currentLength > limit * 2) break
                    truncatedText.append(char)
                }
                truncatedText.toString()
            }
            // 如果实际长度正好等于限制，返回原文本
            else -> text
        }
    }

}