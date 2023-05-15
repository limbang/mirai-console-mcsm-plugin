/*
 * Copyright (c) 2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.mirai.command

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.utils.MiraiLogger
import top.limbang.mcsm.RetrofitClient
import top.limbang.mcsm.entity.Chat
import top.limbang.mcsm.entity.MinecraftLog
import top.limbang.mcsm.mirai.MCSM
import top.limbang.mcsm.mirai.command.MCSMCompositeCommand.apiMap
import top.limbang.mcsm.mirai.config.MCSMData.groupConfig
import top.limbang.mcsm.mirai.config.MCSMData.groupInstances
import top.limbang.mcsm.utils.*
import java.net.URL
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlin.coroutines.CoroutineContext

object MCSMListener : SimpleListenerHost() {

    private val mcloApi = RetrofitClient(apiUrl = "https://api.mclo.gs/1/").getMcloApi()

    @PublishedApi
    internal val logger: MiraiLogger = MiraiLogger.Factory.create(this::class.java)
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.warning(exception.message)
    }

    /**
     * 对所有MC服务器发送通知
     */
    @EventHandler
    fun GroupMessageEvent.notice() {
        if (toCommandSender().hasPermission(MCSM.parentPermission).not()) return
        val config = groupConfig[group.id] ?: return
        if (!config.isEnabledNotice) return
        val content = message.contentToString()
        val match = """^通知\s?(.*)""".toRegex().find(content) ?: return
        val (message) = match.destructured
        val cmd = "title @a title ${Json.encodeToString(arrayListOf(Chat(text = message, color = "red")))}"
        groupInstances[group.id]!!.forEach { instance ->
            launch {
                apiMap[instance.apiKey]!!.sendCommandInstance(instance.uuid, instance.daemonUUID, instance.apiKey, cmd)
            }
        }
    }

    /**
     * 发送消息到MC服务器
     *
     */
    @EventHandler
    fun GroupMessageEvent.sendMessage() {
        val config = groupConfig[group.id] ?: return
        if (!config.isEnabledSendMessage) return
        val instances = groupInstances[group.id] ?: return

        val content = message.contentToString()
        instances.forEach {
            val match = """^${it.name}\s?(.*)""".toRegex().find(content) ?: return@forEach
            val (message) = match.destructured
            val cmd = "tellraw @a ${
                Json.encodeToString(
                    arrayListOf(
                        Chat("[群]"),
                        Chat("<${sender.nameCardOrNick}>", color = "dark_green"),
                        Chat(text = message, color = "white")
                    )
                )
            }"
            launch {
                apiMap[it.apiKey]!!.sendCommandInstance(it.uuid, it.daemonUUID, it.apiKey, cmd)
            }
        }
    }

    /**
     * Forge tps
     *
     */
    @EventHandler
    fun GroupMessageEvent.forgeTps() {
        val config = groupConfig[group.id] ?: return
        if (!config.isTps) return
        val instances = groupInstances[group.id] ?: return
        val content = message.contentToString()
        val match = """^tps\s?(.*)""".toRegex().find(content) ?: return
        val (name) = match.destructured
        val instance = instances.find { it.name == name.trim() } ?: return
        launch {
            apiMap[instance.apiKey]!!.sendCommandInstance(
                instance.uuid, instance.daemonUUID, instance.apiKey, "forge tps"
            )
            // 获取当前时间,忽略毫秒
            val time = LocalTime.now().withNano(0)
            var getSuccess = false
            var cumulativeTime = 0
            do {
                delay(1000)
                cumulativeTime++
                if (cumulativeTime >= 10) {
                    group.sendMessage("$name:获取 tps 超时")
                    return@launch
                }
                val log =
                    apiMap[instance.apiKey]!!.getInstanceLog(instance.uuid, instance.daemonUUID, instance.apiKey).data!!
                try {
                    val minecraftLog = log.toRemoveColorCodeMinecraftLog().last { it.contents.indexOf("Overall") != -1 }
                    if (minecraftLog.time >= time) {
                        group.sendMessage(minecraftLog.contents)
                        getSuccess = true
                    }
                } catch (e: NoSuchElementException) {
                    continue
                }
            } while (!getSuccess)
        }
    }

    /**
     * 启动服务器命令
     *
     */
    @EventHandler
    fun GroupMessageEvent.startServer() {
        val config = groupConfig[group.id] ?: return
        if (!config.isEnabledForceStart) return
        val instances = groupInstances[group.id] ?: return
        val content = message.contentToString()
        val match = """^启动\s?(.*)""".toRegex().find(content) ?: return
        val (name) = match.destructured
        val instance = instances.find { it.name == name.trim() } ?: return

        // 黑名单判断
        if (config.blacklist.any { it.id == sender.id }) return

        launch {
            runCatching {
                apiMap[instance.apiKey]!!.openInstance(
                    instance.uuid, instance.daemonUUID, instance.apiKey
                )
            }.onSuccess {
                group.sendMessage("[$name]启动成功")
            }.onFailure { e ->
                if (e.localizedMessage == "实例未处于关闭状态，无法再进行启动") {
                    group.sendMessage("检测到服务器正在运行中,尝试获取在线人数请稍等...")
                    val instant = apiMap[instance.apiKey]!!.sendCommandInstance(
                        instance.uuid, instance.daemonUUID, instance.apiKey, "list"
                    ).time
                    val time = Instant.ofEpochMilli(instant).atZone(ZoneId.systemDefault()).toLocalTime().withNano(0)
                    delay(1000)
                    val log = apiMap[instance.apiKey]!!.getInstanceLog(
                        instance.uuid, instance.daemonUUID, instance.apiKey
                    ).data!!.toRemoveColorCodeMinecraftLog()
                    val isFailure = log.filter {
                        // 过滤日志时间比时间戳早的日志
                        it.time >= time && it.time.hour == time.hour && it.time.minute == time.minute
                    }.none {
                        // 过滤不包含 online
                        it.contents.contains("online") || it.contents.contains("在线")
                    }
                    if (isFailure) {
                        group.sendMessage("获取在线人数失败,开始强行停止服务器...")
                        runCatching {
                            apiMap[instance.apiKey]!!.killInstance(
                                instance.uuid, instance.daemonUUID, instance.apiKey
                            )
                        }.onSuccess {
                            group.sendMessage("强行停止服务器成功,开始启动服务器...")
                            startServer()
                        }
                        return@launch
                    }
                    group.sendMessage("服务器未卡死,如果是tps低等问题请联系管理员重启.")
                }
            }
        }
    }

    @EventHandler
    fun GroupMessageEvent.playerChatMessages() {
        if (toCommandSender().hasPermission(MCSM.parentPermission).not()) return
        val instances = groupInstances[group.id] ?: return
        val content = message.contentToString()
        val match = """^分析日志\s?(.*)""".toRegex().find(content) ?: return
        val (name) = match.destructured
        val instance = instances.find { it.name == name.trim() } ?: return
        launch {
            val filesDownload = apiMap[instance.apiKey]!!.filesDownload(
                uuid = instance.uuid,
                remoteUuid = instance.daemonUUID,
                apikey = instance.apiKey,
                fileName = "logs/latest.log"
            ).data!!

            val logs = URL(filesDownload.toDownloadUrl(apiUrl = instance.apiUrl)).readText().toMinecraftLog()

            val forward = buildForwardMessage {
                bot.id named "服务器玩家聊天消息" says charMessage(logs).ifEmpty { "未找到匹配的玩家聊天消息." }
                bot.id named "服务器管理员修改记录" says opLogMessage(logs).ifEmpty { "未找到匹配的管理员修改记录." }
                bot.id named "服务器玩家上下线记录" says joinTheExitGameMessage(logs).ifEmpty { "未找到匹配的玩家上下线记录." }
            }.copy(
                title = "分析日志结果",
                preview = listOf("服务器玩家聊天消息", "服务器管理员修改记录", "服务器玩家上下线记录")
            )

            group.sendMessage(forward)
        }
    }

    private fun charMessage(logs: List<MinecraftLog>): String {
        var out = ""
        logs.forEach {
            val result = it.toCharMessageGame()
            if (result.isNotEmpty()) out += "$result\n"
        }
        return out.trimEnd()
    }

    private fun opLogMessage(logs: List<MinecraftLog>): String {
        var out = ""
        logs.forEach {
            val result = it.toAdminLog()
            if (result.isNotEmpty()) out += "$result\n"
        }
        return out.trimEnd()
    }

    private fun joinTheExitGameMessage(logs: List<MinecraftLog>): String {
        var out = ""
        logs.forEach {
            val result = it.toJoinTheExitGame()
            if (result.isNotEmpty()) out += "$result\n"
        }
        return out.trimEnd()
    }

    @EventHandler
    fun GroupMessageEvent.getCrashReports() {
        if (toCommandSender().hasPermission(MCSM.parentPermission).not()) return
        val instances = groupInstances[group.id] ?: return
        val content = message.contentToString()
        val match = """^崩溃报告\s?(.*)""".toRegex().find(content) ?: return
        val (name) = match.destructured
        val instance = instances.find { it.name == name.trim() } ?: return
        launch {
            // 获取文件列表
            val filesList = apiMap[instance.apiKey]!!.filesList(
                uuid = instance.uuid,
                remoteUuid = instance.daemonUUID,
                apikey = instance.apiKey,
                target = "crash-reports"
            ).data!!

            // 找出最新时间的日志
            val item = filesList.items.maxBy { it.toLocalDateTime() }
            // 获取下载日志地址
            val filesDownload = apiMap[instance.apiKey]!!.filesDownload(
                uuid = instance.uuid,
                remoteUuid = instance.daemonUUID,
                apikey = instance.apiKey,
                fileName = "crash-reports/${item.name}"
            ).data!!
            // 读取日志
            val log = URL(filesDownload.toDownloadUrl(apiUrl = instance.apiUrl)).readText()
            // 发送到 mclo
            val mclo = mcloApi.pasteLogFile(log)

            if (!mclo.success) group.sendMessage(mclo.error!!)
            else group.sendMessage("崩溃报告:${mclo.url}")
        }
    }
}
