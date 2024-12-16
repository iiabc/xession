package com.hiusers.mc.xession.core

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang

@CommandHeader("xession", permission = "xession.command")
object Command {

    @CommandBody(permission = "xession.command.admin.reload")
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            LoadableContainer.reset()
            sender.sendLang("command_reload")
        }
    }

}