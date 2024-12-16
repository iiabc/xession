package com.hiusers.mc.xession.util

import java.text.DecimalFormat

object MathUtil {

    /**
     * 将 Double 转换为两位小数
     *
     * @return 格式化后的 Double
     */
    fun Double.toTwoDecimalPlaces(): Double {
        val decimalFormat = DecimalFormat("#.##")  // 格式化为最多保留两位小数
        return decimalFormat.format(this).toDouble()  // 格式化并转回 Double
    }

}