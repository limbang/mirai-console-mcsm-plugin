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
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import top.limbang.mcsm.MCSMCompositeCommand.renameInstance
import top.limbang.mcsm.MCSMData.apiKey
import top.limbang.mcsm.MCSMData.apiUrl
import top.limbang.mcsm.MCSMData.isPluginLinkage
import top.limbang.mcsm.MCSMData.isTps
import top.limbang.mcsm.entity.Chat
import top.limbang.mcsm.utils.toRemoveColorCodeMinecraftLog
import top.limbang.mirai.event.RenameEvent
import java.time.LocalTime

object MCSM : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mcsm",
        name = "MCSManager API",
        version = "1.0.7",
    ) {
        author("limbang")
        info("MCSManager api 插件")
        dependsOn("top.limbang.general-plugin-interface")
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

    override fun onDisable() {
        MCSMCompositeCommand.unregister()
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
            // 对所有服务器发送通知
            startsWith("通知") {
                if (sender.asMemberCommandSender().permitteeId.hasPermission(PERMISSION_SERVER)) {
                    val cmd = "title @a title ${Json.encodeToString(arrayListOf(Chat(it, color = "red")))}"
                    MCSMData.serverInstances.forEach { entity ->
                        service.sendCommandInstance(entity.value.uuid, entity.value.daemonUUid, apiKey, cmd)
                    }
                }
            }
            // forge tps
            startsWith("tps") { message ->
                if (!isTps) return@startsWith
                MCSMData.serverInstances[message]?.let { server ->
                    service.sendCommandInstance(server.uuid, server.daemonUUid, apiKey, "forge tps")
                    // 获取当前时间,忽略毫秒
                    val time = LocalTime.now().withNano(0)
                    var isTps = false
                    do {
                        delay(100)
                        val log = service.getInstanceLog(server.uuid, server.daemonUUid, apiKey)
                        try {
                            val minecraftLog = log.toRemoveColorCodeMinecraftLog()
                                .filter { it.channels == "minecraft/DedicatedServer" }
                                .last { it.message.indexOf("Overall") != -1 }
                            if (minecraftLog.time >= time) {
                                group.sendMessage(minecraftLog.message)
                                isTps = true
                            }
                        } catch (e: NoSuchElementException) {
                            continue
                        }
                    } while (!isTps)
                }
            }
        }
        // 监听改名事件
        globalEventChannel().subscribeAlways<RenameEvent> {
            logger.info("RenameEvent: pluginId = $pluginId oldName = $oldName newName = $newName")
            if (!isPluginLinkage) return@subscribeAlways
            if (pluginId == MCSM.id) return@subscribeAlways
            renameInstance(oldName, newName,true)
        }

    }


}

