dependencies {
    compileOnly(project(":project:module-util"))
    compileOnly(project(":project:module-reader"))
    compileOnly(project(":project:module-nms"))
}

// 子模块
taboolib { subproject = true }