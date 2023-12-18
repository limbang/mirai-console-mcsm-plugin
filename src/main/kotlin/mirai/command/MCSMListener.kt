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
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.MiraiLogger
import top.limbang.mcsm.RetrofitClient
import top.limbang.mcsm.entity.Chat
import top.limbang.mcsm.entity.MinecraftLog
import top.limbang.mcsm.mirai.MCSM
import top.limbang.mcsm.mirai.command.MCSMCompositeCommand.apiMap
import top.limbang.mcsm.mirai.config.GroupInstance
import top.limbang.mcsm.mirai.config.MCSMData.groupConfig
import top.limbang.mcsm.mirai.config.MCSMData.groupInstances
import top.limbang.mcsm.model.FilesList
import top.limbang.mcsm.utils.*
import java.io.*
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.util.zip.GZIPInputStream
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
            val response = apiMap[instance.apiKey]!!.sendCommandInstance(
                instance.uuid, instance.daemonUUID, instance.apiKey, "forge tps"
            )
            // 获取命令发送成功的时间戳以默认时区转成时间
            val time = Instant.ofEpochMilli(response.time).atZone(ZoneId.systemDefault()).toLocalTime().withNano(0)
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

    /**
     * 获取指定服务器的最新日志
     *
     */
    @EventHandler
    fun GroupMessageEvent.getLatestLog() {
        if (toCommandSender().hasPermission(MCSM.parentPermission).not()) return
        val instances = groupInstances[group.id] ?: return
        val content = message.contentToString()
        val match = """^分析日志\s?(.*)""".toRegex().find(content) ?: return
        val (name) = match.destructured
        val instance = instances.find { it.name == name.trim() } ?: return
        launch {
            val url = getDownloadUrl(instance, "logs/latest.log")
            val logs = URL(url).readText().toMinecraftLog()
            sendMinecraftLog(logs)
        }
    }

    /**
     * 获取指定服务器的指定日期日志
     *
     */
    @EventHandler
    fun GroupMessageEvent.getSpecifiedDateLog() {
        if (toCommandSender().hasPermission(MCSM.parentPermission).not()) return
        val instances = groupInstances[group.id] ?: return
        val content = message.contentToString()
        val (name,date)  = """^分析指定日志\s?(.*)\s(\d{4}-\d{2}-\d{2})""".toRegex().find(content)?.destructured ?: return
        val instance = instances.find { it.name == name.trim() } ?: return
        launch {
            sendMinecraftLog(getLogs(instance,date))
        }
    }

    /**
     * 获取指定目录的文件列表
     *
     * @param instance 实例信息
     * @return 文件列表
     * @throws IOException 获取文件列表时可能发生 IO 异常
     */
    @Throws(IOException::class)
    private suspend fun getFilesList(instance: GroupInstance,target:String): FilesList {
        return apiMap[instance.apiKey]?.runCatching {
            filesList(
                uuid = instance.uuid,
                remoteUuid = instance.daemonUUID,
                apikey = instance.apiKey,
                target = target
            )
        }?.getOrNull()?.data ?: throw IOException("无法获取文件列表")
    }

    /**
     * 获取指定文件的下载链接
     *
     * @param instance 实例信息
     * @param fileName 文件名称
     * @return 下载链接
     * @throws IOException 获取下载链接时可能发生 IO 异常
     */
    @Throws(IOException::class)
    private suspend fun getDownloadUrl(instance: GroupInstance, fileName: String): String {
        return apiMap[instance.apiKey]?.runCatching {
            filesDownload(
                uuid = instance.uuid,
                remoteUuid = instance.daemonUUID,
                apikey = instance.apiKey,
                fileName = fileName
            )
        }?.getOrNull()?.data?.toDownloadUrl(instance.apiUrl) ?: throw IOException("无法获取下载链接")
    }

    private suspend fun getLogs(instance: GroupInstance, date: String): List<MinecraftLog> {
        // 1. 构建日志文件名正则表达式
        val regex = """$date-\d+.log.gz""".toRegex()

        // 2. 获取指定目录下的所有文件列表
        val filesList = getFilesList(instance, "logs")

        // 3. 从所有符合条件的文件中下载日志数据，并将它们拼接为一个流
        var concatStream: InputStream = ByteArrayInputStream(ByteArray(0))

        filesList.items.filter { regex.find(it.name) != null }.forEach {
            val url = getDownloadUrl(instance, "logs/${it.name}")
            // 使用 GZIPInputStream 解压缩并下载文件流，并与之前的文件流进行拼接
            val fileStream = GZIPInputStream(URL(url).openStream())
            concatStream = SequenceInputStream(concatStream, fileStream)
        }

        // 4. 将拼接后的流解析为 MinecraftLog 对象
        return BufferedReader(InputStreamReader(concatStream)).use { it.readText() }.toMinecraftLog()
    }

    /**
     * 向群里发送 Minecraft 的日志
     *
     * @param logs
     */
    private suspend fun GroupMessageEvent.sendMinecraftLog(logs: List<MinecraftLog>) {

        charMessage(logs).run {
            delay(60)
            if (isNotEmpty()) group.sendImage("服务器玩家聊天记录：\n$this".toImage().toInput(), "png")
        }

        joinTheExitGameMessage(logs).run {
            delay(60)
            if (isNotEmpty()) group.sendImage("服务器玩家上下线记录：\n$this".toImage().toInput(), "png")
        }

        opLogMessage(logs).run {
            delay(60)
            if (isNotEmpty()) group.sendImage("服务器管理员修改记录：\n$this".toImage().toInput(), "png")
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
