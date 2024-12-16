package com.hiusers.mc.xession.kether

import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.util.VariableReader
import taboolib.common5.Coerce
import taboolib.module.kether.KetherShell
import taboolib.module.kether.ScriptOptions
import taboolib.module.kether.printKetherErrorMessage
import java.util.concurrent.CompletableFuture

object ActionUtil {

    fun Player.runAction(script: String, variable: Map<String, Any> = mutableMapOf()): CompletableFuture<Any> {
        return try {
            KetherShell.eval(
                script,
                ScriptOptions.builder()
                    .sender(adaptCommandSender(this))
                    .vars(variable)
                    .namespace(listOf("xerr", "quest_engine"))
                    .build()
            ).thenApply { it }
        } catch (ex: Throwable) {
            ex.printKetherErrorMessage()
            CompletableFuture.completedFuture(false)
        }
    }

    /**
     * 返回 Boolean 结果
     */
    fun Player.booleanAction(script: String, variable: Map<String, Any> = mutableMapOf()): Boolean {
        if (script.isEmpty()) return true
        return runAction(script, variable).thenApply {
            Coerce.toBoolean(it)
        }.get()
    }

    fun Player.doubleAction(script: String, variable: Map<String, Any> = mutableMapOf()): Double {
        return runAction(script, variable).thenApply {
            Coerce.toDouble(it)
        }.get()
    }

    /**
     * 替换解析内联语句
     */
    fun Player.parseScript(script: String, variable: Map<String, Any> = mapOf()): String {
        return VariableReader().replaceNested(script) {
            val text = try {
                runAction(this, variable).thenApply {
                    it.toString()
                }.get()
            } catch (ex: Exception) {
                ex.localizedMessage
            }
            val e = replace(this, text)
            e
        }
    }

    fun Player.parseScriptList(scriptList: List<String>, variable: Map<String, Any> = mapOf()): List<String> {
        return scriptList.map { parseScript(it, variable) }
    }

    /**
     * 无执行者
     */
    fun runAction(script: String, variable: Map<String, Any> = mutableMapOf()): CompletableFuture<Any> {
        return try {
            KetherShell.eval(
                script,
                ScriptOptions.builder()
                    .vars(variable)
                    .namespace(listOf("xerr", "quest_engine"))
                    .build()
            ).thenApply { it }
        } catch (ex: Throwable) {
            ex.printKetherErrorMessage()
            CompletableFuture.completedFuture(false)
        }
    }

    /**
     * 无执行者解析返回字符串
     */
    fun String.stringAction(variable: Map<String, Any> = mutableMapOf()): String {
        return runAction(this, variable).thenApply {
            it.toString()
        }.get()
    }

}