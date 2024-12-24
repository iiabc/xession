@file:Suppress("PropertyName", "SpellCheckingInspection")

import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.20" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.22" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.izzel.taboolib")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    // TabooLib 配置
    configure<TabooLibExtension> {
        description {
            name("Xession")
//            name(rootProject.name)
//            prefix(rootProject.name)
            contributors {
                name("HiUsers")
            }
            links {
                name("homepage").url("https://iplugin.hiusers.com/")
            }
            dependencies {
                name("Xerr")
                name("QuestEngine").optional(true)
                name("Chemdah").optional(true)
            }
        }
        env {
            install(Basic, Bukkit, BukkitUtil)
            install(CommandHelper)
            install(BukkitHook)
            install(Kether, JavaScript)
        }
        version {
            taboolib = "6.2.1-f095116"
        }
    }

    // 仓库
    repositories {
        mavenCentral()
        maven("https://repo.hiusers.com/releases")
    }
    // 依赖
    dependencies {
        compileOnly("ink.ptms.core:v11904:11904:mapped")
        compileOnly("ink.ptms.core:v11904:11904:universal")

        compileOnly("com.google.code.gson:gson:2.8.7")

        compileOnly("api:XerrAPI:0.0.1-Alpha.6")
        compileOnly("api:QuestEngineAPI:4.0.5.3")
        compileOnly("ink.ptms.chemdah:api:1.1.5")

        compileOnly(kotlin("stdlib"))
    }

    // 编译配置
    java {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xextended-compiler-checks")
        }
    }
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}