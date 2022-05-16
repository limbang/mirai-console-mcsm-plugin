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
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import top.limbang.mcsm.MCSMData.apiKey
import top.limbang.mcsm.MCSMData.apiUrl

object MCSM : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mcsm",
        name = "mcsm",
        version = "1.0.3",
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
                    val cmd =
                        """tellraw @a [{"text":"[群]","color":"dark_red"},{"text":"<${sender.nameCardOrNick}>","color":"dark_green"},{"text":"$it"}]"""
                    service.sendCommandInstance(server.uuid, server.daemonUUid, apiKey, cmd)
                }
            }
            // forge tps
            startsWith("ftps") {
                MCSMData.serverInstances[it]?.let { server ->
                    service.sendCommandInstance(server.uuid, server.daemonUUid, apiKey, "forge tps")
                    delay(1000)
                    val log = service.getInstanceLog(server.uuid, server.daemonUUid, apiKey)
                    val message = """minecraft/DedicatedServer]:\s(Overall.*)""".toRegex()
                        .findAll(log).toList().last().groupValues[1]
                    group.sendMessage(filterColorCode(message))
                }
            }
        }
    }

    /**
     * 过滤颜色代码
     *
     * @param log
     * @return
     */
    fun filterColorCode(log: String): String {
        return """\[[\d;]*m""".toRegex().replace(log, "")
    }
}

