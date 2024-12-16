package com.hiusers.mc.xession.util

import com.google.gson.Gson
import com.hiusers.mc.xession.serializer.GsonProvider
import taboolib.common.io.runningResources
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.configuration.Configuration
import taboolib.platform.util.bukkitPlugin
import java.io.File
import java.io.IOException

object FileUtil {

    fun File.getFileList(extension: String = ".yml"): List<File> {
        return mutableListOf<File>().let { files ->
            if (isDirectory) {
                listFiles()?.forEach { files.addAll(it.getFileList(extension)) }
            } else if (name.endsWith(extension, true)) {
                files.add(this)
            }
            return@let files
        }
    }

    fun getFile(child: String, create: Boolean = true, vararg yml: String = arrayOf("example")): File {
        val file = File(bukkitPlugin.dataFolder, child)
        if (!file.exists() && create) {
            yml.forEach {
                releaseResourceFile("$child/$it.yml")
            }
        }
        return file
    }

    fun getFile(path: String, create: Boolean): File {
        val file = File(bukkitPlugin.dataFolder, path)
        if (!file.exists() && create) {
            releaseResourceFile(path)
        }
        return file
    }

    /**
     * 从 YAML 文件反序列化到 Kotlin 对象
     */
    fun <T> Configuration.deserialize(classifier: Class<T>, gson: Gson = GsonProvider.gson): T {
        val json = gson.toJson(this.toMap())
        return gson.fromJson(json, classifier)
    }

    /**
     * 从 YAML 文件的指定Key的Section反序列化到 Kotlin 对象
     */
    fun <T> Configuration.deserialize(key: String, classifier: Class<T>, gson: Gson = GsonProvider.gson): T {
        val section = this.getConfigurationSection(key)
        val json = gson.toJson(section?.toMap())
        return gson.fromJson(json, classifier)
    }

    /**
     * 释放当前插件内特定目录下的所有资源文件
     *
     * @param prefix 资源文件目录前缀
     * @param replace 是否覆盖文件
     * @param target 目标文件目录
     */
    fun releaseFolder(prefix: String, replace: Boolean = false, prefixTarget: String) {
        // 遍历资源列表，假设 runningResources 是一个存储资源路径和字节数据的映射
        runningResources.forEach { (path, bytes) ->
            // 如果路径以指定前缀开头
            if (path.startsWith(prefix)) {
                // 构建目标文件路径
                val targetFile = File(getDataFolder(), prefixTarget + path.substring(prefix.length))

                // 如果文件已经存在并且 replace 为 false，则跳过
                if (targetFile.exists() && !replace) {
                    return@forEach
                }

                // 创建目标目录，如果目标目录不存在
                targetFile.parentFile?.mkdirs()

                // 尝试写入文件
                try {
                    // 创建文件并写入字节
                    targetFile.writeBytes(bytes)
                } catch (e: IOException) {
                    error("Failed to write resource file: $path")
                }
            }
        }
    }

}
