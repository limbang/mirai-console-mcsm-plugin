/*
 * Copyright 2022-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm

import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender.Companion.asMemberCommandSender
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import top.limbang.mcsm.MCSMCompositeCommand.api
import top.limbang.mcsm.MCSMCompositeCommand.isNotSetApiKey
import top.limbang.mcsm.MCSMCompositeCommand.renameInstance
import top.limbang.mcsm.MCSMData.apiKey
import top.limbang.mcsm.MCSMData.isEnabledForceStart
import top.limbang.mcsm.MCSMData.isTps
import top.limbang.mcsm.entity.Chat
import top.limbang.mcsm.utils.toRemoveColorCodeMinecraftLog
import top.limbang.mirai.event.RenameEvent
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

object MCSM : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mcsm",
        name = "MCSManager API",
        version = "1.1.0",
    ) {
        author("limbang")
        info("MCSManager api 插件")
        dependsOn("top.limbang.general-plugin-interface", true)
    }
) {
    /** 是否加载通用插件接口 */
    val isLoadGeneralPluginInterface: Boolean = try {
        Class.forName("top.limbang.mirai.GeneralPluginInterface")
        true
    } catch (e: Exception) {
        logger.info("未加载通用插件接口,limbang插件系列改名无法同步.")
        logger.info("前往 https://github.com/limbang/mirai-plugin-general-interface/releases 下载")
        false
    }

    override fun onDisable() {
        MCSMCompositeCommand.unregister()
    }

    override fun onEnable() {
        MCSMData.reload()
        MCSMCompositeCommand.register()

        // 创建事件通道
        val eventChannel = GlobalEventChannel.parentScope(this)

        eventChannel.subscribeGroupMessages {
            // 发送消息到指定服务器
            MCSMData.serverInstances.forEach { entity ->
                if (!MCSMData.isEnabledSendMessage) return@forEach
                startsWith(entity.key) {
                    val server = entity.value
                    val cmd = "tellraw @a ${
                        Json.encodeToString(
                            arrayListOf(
                                Chat("[群]"),
                                Chat("<${sender.nameCardOrNick}>", color = "dark_green"),
                                Chat(it, color = "white")
                            )
                        )
                    }"
                    if (isNotSetApiKey()) return@startsWith
                    api.sendCommandInstance(server.uuid, server.daemonUUid, apiKey, cmd)
                }
            }
            // 对所有服务器发送通知
            startsWith("通知") {
                if (!MCSMData.isEnabledNotice) return@startsWith
                if (sender.asMemberCommandSender().permitteeId.hasPermission(MCSM.parentPermission)) {
                    val cmd = "title @a title ${Json.encodeToString(arrayListOf(Chat(it, color = "red")))}"
                    MCSMData.serverInstances.forEach { entity ->
                        if (isNotSetApiKey()) return@startsWith
                        api.sendCommandInstance(entity.value.uuid, entity.value.daemonUUid, apiKey, cmd)
                    }
                }
            }

            // forge tps
            startsWith("tps") { forgeTps(it) }
            // 启动服务器
            startsWith("启动") { startServer(it) }
        }

        if (!isLoadGeneralPluginInterface) return
        // 监听改名事件
        eventChannel.subscribeAlways<RenameEvent> {
            logger.info("RenameEvent: pluginId = $pluginId oldName = $oldName newName = $newName")
            if (pluginId == MCSM.id) return@subscribeAlways
            renameInstance(oldName, newName, true)
        }
    }

    private suspend fun GroupMessageEvent.forgeTps(name: String) {
        if (!isTps) return
        MCSMData.serverInstances[name]?.let { server ->
            if (isNotSetApiKey()) return
            api.sendCommandInstance(server.uuid, server.daemonUUid, apiKey, "forge tps")
            // 获取当前时间,忽略毫秒
            val time = LocalTime.now().withNano(0)
            var getSuccess = false
            var cumulativeTime = 0
            do {
                delay(1000)
                cumulativeTime++
                if (cumulativeTime >= 10) {
                    group.sendMessage("$name:获取 tps 超时")
                    return
                }
                val log = api.getInstanceLog(server.uuid, server.daemonUUid, apiKey).data!!
                try {
                    val minecraftLog = log.toRemoveColorCodeMinecraftLog()
                        .last { it.message.indexOf("Overall") != -1 }
                    if (minecraftLog.time >= time) {
                        group.sendMessage(minecraftLog.message)
                        getSuccess = true
                    }
                } catch (e: NoSuchElementException) {
                    continue
                }
            } while (!getSuccess)
        }
    }

    private suspend fun GroupMessageEvent.startServer(name: String) {
        if (!isEnabledForceStart) return
        // 黑名单判断
        if (MCSMData.blacklist.any { it.id == sender.id }) return
        MCSMData.serverInstances[name]?.let { server ->
            if (isNotSetApiKey()) return
            runCatching { api.openInstance(server.uuid, server.daemonUUid, apiKey) }.onSuccess {
                group.sendMessage("[$name]启动成功")
            }.onFailure { e ->
                if (e.localizedMessage == "实例未处于关闭状态，无法再进行启动") {
                    group.sendMessage("检测到服务器正在运行中,尝试获取在线人数请稍等...")
                    val instant = api.sendCommandInstance(server.uuid, server.daemonUUid, apiKey, "list").time
                    val time = Instant.ofEpochMilli(instant).atZone(ZoneId.systemDefault()).toLocalTime().withNano(0)
                    delay(1000)
                    val log = api.getInstanceLog(server.uuid, server.daemonUUid, apiKey).data!!.toRemoveColorCodeMinecraftLog()
                    val isFailure = log.filter {
                        // 过滤日志时间比时间戳早的日志
                        it.time >= time && it.time.hour == time.hour && it.time.minute == time.minute
                    }.none {
                        // 过滤不包含 online
                        it.message.contains("online") || it.message.contains("在线")
                    }
                    if (isFailure) {
                        group.sendMessage("获取在线人数失败,开始强行停止服务器...")
                        runCatching { api.killInstance(server.uuid, server.daemonUUid, apiKey) }.onSuccess {
                            group.sendMessage("强行停止服务器成功,开始启动服务器...")
                            startServer(name)
                        }
                        return
                    }
                    group.sendMessage("服务器未卡死,如果是tps低等问题请联系管理员重启.")
                }
            }
        }
    }

}

