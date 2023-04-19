/*
 * Copyright (c) 2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.mirai.command

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import top.limbang.mcsm.mirai.MCSM
import top.limbang.mcsm.mirai.command.MCSMCompositeCommand.isNotGroup
import top.limbang.mcsm.mirai.config.GroupConfig
import top.limbang.mcsm.mirai.config.MCSMData

object GroupConfigCompositeCommand : CompositeCommand(
    owner = MCSM,
    primaryName = "config",
    description = "配置本群的MCSM选项"
) {
    fun checkGroupConfig(groupID: Long) {
        if (MCSMData.groupConfig[groupID] == null) MCSMData.groupConfig[groupID] = GroupConfig()
    }

    @SubCommand
    @Description("设置tps功能启用")
    suspend fun UserCommandSender.setTps(value: Boolean) {
        if (isNotGroup()) return
        MCSMData.groupConfig[subject.id]!!.isTps = value
        sendMessage("tps功能:$value")
    }

    @SubCommand
    @Description("添加黑名单")
    suspend fun UserCommandSender.addBlacklist(member: Member) {
        if (isNotGroup()) return
        if (MCSMData.groupConfig[subject.id]!!.blacklist.add(member)) sendMessage("添加${member.nameCardOrNick}到黑名单")
        else sendMessage("添加黑名单失败")
    }

    @SubCommand
    @Description("移除黑名单")
    suspend fun UserCommandSender.removeBlacklist(member: Member) {
        if (isNotGroup()) return
        if (MCSMData.groupConfig[subject.id]!!.blacklist.remove(member)) sendMessage("将${member.nameCardOrNick}移除黑名单")
        else sendMessage("移除黑名单失败")
    }

    @SubCommand
    @Description("设置发送消息到服务器功能启用")
    suspend fun UserCommandSender.setSendMessage(value: Boolean) {
        if (isNotGroup()) return
        MCSMData.groupConfig[subject.id]!!.isEnabledSendMessage = value
        sendMessage("发送消息到服务器功能:$value")
    }

    @SubCommand
    @Description("设置通知消息功能启用")
    suspend fun UserCommandSender.setNotice(value: Boolean) {
        if (isNotGroup()) return
        MCSMData.groupConfig[subject.id]!!.isEnabledNotice = value
        sendMessage("通知消息功能:$value")
    }

    @SubCommand
    @Description("设置强制启动功能启用")
    suspend fun UserCommandSender.setForceStart(value: Boolean) {
        if (isNotGroup()) return
        MCSMData.groupConfig[subject.id]!!.isEnabledForceStart = value
        sendMessage("强制启动功能:$value")
    }
}
