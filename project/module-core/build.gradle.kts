dependencies {
    compileOnly(project(":project:module-util"))
    compileOnly(project(":project:session-questengine"))
}

// 子模块
taboolib { subproject = true }