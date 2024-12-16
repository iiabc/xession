package com.hiusers.mc.xession.system

/**
 * 加载接口
 */
interface Loadable : Comparable<Loadable> {

    // 加载优先级，返回一个整数，值越小，优先级越高
    val priority: Int

    fun load()

    fun unload()

    // 用于按优先级排序，优先级低的先加载
    override fun compareTo(other: Loadable): Int {
        return this.priority - other.priority
    }

}