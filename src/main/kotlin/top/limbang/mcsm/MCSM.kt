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
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import top.limbang.mcsm.MCSMData.apiKey
import top.limbang.mcsm.MCSMData.apiUrl
import top.limbang.mcsm.entity.Chat
import top.limbang.mcsm.utils.CoroutineUpdateTask
import top.limbang.mcsm.utils.removeColorCodeLog
import top.limbang.mcsm.utils.toRemoveColorCodeMinecraftLog
import java.time.LocalTime

object MCSM : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mcsm",
        name = "mcsm",
        version = "1.0.4",
    ) {
        author("limbang")
    }
) {

    val PERMISSION_ADMIN by lazy {
        PermissionService.INSTANCE.register(permissionId("command.admin"), "管理员权限", parentPermission)
    }
    val PERMISSION_SERVER by lazy {
        PermissionService.INSTANCE.register(
            permissionId("command.server"),
            "CMD/添加/删除/改名/启动/停止/重启/终止服务器权限",
            PERMISSION_ADMIN
        )
    }
    val PERMISSION_START by lazy {
        PermissionService.INSTANCE.register(permissionId("command.start"), "启动服务器权限", PERMISSION_SERVER)
    }

    private val task by lazy { CoroutineUpdateTask() }

    override fun onDisable() {
        MCSMCompositeCommand.unregister()
        task.cancel()
    }

    override fun onEnable() {
        MCSMData.reload()
        MCSMCompositeCommand.register()
        // 初始化权限注册
        PERMISSION_ADMIN
        PERMISSION_SERVER
        PERMISSION_START

        if (apiUrl.isEmpty()) return
        val service = MCSMApi(apiUrl).get()
        globalEventChannel().subscribeGroupMessages {
            // 发送消息到指定服务器
            MCSMData.serverInstances.forEach { entity ->
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
                    service.sendCommandInstance(server.uuid, server.daemonUUid, apiKey, cmd)
                }
            }
            // forge tps
            startsWith("ftps") { message ->
                MCSMData.serverInstances[message]?.let { server ->
                    service.sendCommandInstance(server.uuid, server.daemonUUid, apiKey, "forge tps")
                    // 获取当前时间,忽略毫秒
                    val time = LocalTime.now().withNano(0)
                    var isTps = false
                    do {
                        delay(100)
                        val log = service.getInstanceLog(server.uuid, server.daemonUUid, apiKey)
                        val minecraftLog = log.toRemoveColorCodeMinecraftLog()
                            .filter { it.channels == "minecraft/DedicatedServer" }
                            .last { it.message.indexOf("Overall") != -1 }
                        if (minecraftLog.time >= time) {
                            group.sendMessage(minecraftLog.message)
                            isTps = true
                        }
                    } while (!isTps)
                }
            }
        }

        globalEventChannel().subscribeOnce<BotOnlineEvent> {
            var time = LocalTime.now()
            task.scheduleUpdate(1000) {
                MCSMData.serverInstances.forEach { entry ->
                    // 检查是否有群订阅了此服务器,有就获取日志,没有直接返回
                    var isGetLog = false
                    MCSMData.groupMonitorConfig.forEach {
                        if (it.value.indexOf(entry.key) != -1) isGetLog = true
                    }
                    if (!isGetLog) return@forEach
                    launch {
                        // 查询日志
                        runCatching { service.getInstanceLog(entry.value.uuid, entry.value.daemonUUid, apiKey) }.onSuccess { log ->
                            try {
                                // 正则匹配
                                val matchResult =
                                    """\[(\d+):(\d+):(\d+)].*minecraft/DedicatedServer]:\s<(.*)>.*群(.*)""".toRegex().findAll(log).last().groupValues
                                val newTime = LocalTime.of(matchResult[1].toInt(), matchResult[2].toInt(), matchResult[3].toInt(), time.nano)
                                // 判断消息是否最新
                                if (newTime >= time) {
                                    // 如果群监听了此服务器就发送消息
                                    MCSMData.groupMonitorConfig.forEach { groupMonitorConfig ->
                                        if (groupMonitorConfig.value.indexOf(entry.key) != -1) {
                                            bot.getGroup(groupMonitorConfig.key)
                                                ?.sendMessage("[${entry.key}]${matchResult[4]}:${matchResult[5]}".removeColorCodeLog().trim())
                                            time = LocalTime.now()
                                        }
                                    }
                                }
                            } catch (e: NoSuchElementException) {
                                return@launch
                            }
                        }.onFailure {
                            logger.error(it)
                        }
                    }
                }
            }
        }
    }


}

