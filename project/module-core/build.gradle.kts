dependencies {
    compileOnly(project(":project:module-api"))
    compileOnly(project(":project:module-util"))
}

// 子模块
taboolib { subproject = true }