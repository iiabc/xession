dependencies {
    compileOnly(project(":project:module-util"))
    compileOnly(project(":project:module-kether"))
    compileOnly(project(":project:module-reader"))
}

// 子模块
taboolib { subproject = true }