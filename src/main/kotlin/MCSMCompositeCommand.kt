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
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.event.broadcast
import top.limbang.mcsm.MCSM.isLoadGeneralPluginInterface
import top.limbang.mcsm.MCSMData.apiKey
import top.limbang.mcsm.MCSMData.apiUrl
import top.limbang.mcsm.MCSMData.isTps
import top.limbang.mcsm.MCSMData.serverInstances
import top.limbang.mcsm.model.Tasks
import top.limbang.mcsm.service.MCSManagerApi
import top.limbang.mcsm.utils.removeColorCodeLog
import top.limbang.mcsm.utils.toRemoveColorCodeMinecraftLog
import top.limbang.mirai.event.RenameEvent
import java.time.LocalTime


object MCSMCompositeCommand : CompositeCommand(MCSM, "mcsm") {

    val api = RetrofitClient(apiUrl).getMCSManagerApi()

    @SubCommand("addApi", "添加Api")
    @Description("添加api管理")
    suspend fun CommandSender.add(url: String, key: String) {
        if (!checkPermission(MCSM.PERMISSION_ADMIN)) return
        apiUrl = url
        apiKey = key
        sendMessage("[$name]添加成功,请重启.")
    }

    /**
     * 是否没有设置 api key
     */
    fun isNotSetApiKey() = apiUrl.isEmpty() || apiKey.isEmpty()

    /**
     * 检查权限
     *
     * @param permission
     * @return
     */
    private suspend fun CommandSender.checkPermission(permission: Permission): Boolean {
        if (isNotSetApiKey()) {
            sendMessage("未设置 MCSManager Api 或 key")
            return false
        }
        return if (hasPermission(permission)) {
            true
        } else {
            sendMessage("你没有 ${permission.description}")
            false
        }
    }

    /**
     * 检测服务器和权限
     *
     * @param name
     * @param permission
     * @return
     */
    private suspend fun CommandSender.serverCheck(name: String, permission: Permission): Boolean {
        if (!checkPermission(permission)) return false
        if (serverInstances[name] == null) {
            sendMessage("${name}不存在，请查询后输入")
            return false
        }
        return true
    }

    @SubCommand("daemonList", "守护进程列表")
    @Description("获取守护进程列表")
    suspend fun CommandSender.daemonList() {
        if (!checkPermission(MCSM.PERMISSION_ADMIN)) return
        api.getAllDaemonList(apiKey).forEach { daemon ->
            var msg = "守护进程[${daemon.uuid}],实例如下:\n"
            daemon.instances.forEach { instances ->
                msg += "${instances.instanceUuid} -> ${instances.config.nickname}\n"
            }
            sendMessage(msg)
        }
    }

    @SubCommand("list", "列表")
    @Description("获取列表")
    suspend fun CommandSender.list() {
        if (serverInstances.isEmpty()) {
            sendMessage("当前列表为空.")
            return
        }
        var list = "列表如下:\n"
        serverInstances.forEach { (s, serverInstances) ->
            list += "[$s] "
        }
        sendMessage(list)
    }

    @SubCommand("addServerInstances", "添加服务器实例")
    @Description("添加需要管理的服务器实例")
    suspend fun CommandSender.addServerInstances(name: String, uuid: String, daemonUuid: String) {
        if (!checkPermission(MCSM.PERMISSION_SERVER)) return
        serverInstances[name] = ServerInstances(uuid, daemonUuid)
        sendMessage("[$name]添加成功.")
    }

    @SubCommand("deleteServerInstances", "删除服务器实例")
    @Description("删除需要管理的服务器实例")
    suspend fun CommandSender.deleteServerInstances(name: String) {
        if (!checkPermission(MCSM.PERMISSION_SERVER)) return
        serverInstances.remove(name) ?: return
        sendMessage("[$name]删除成功.")
    }

    @SubCommand("rename", "重命名服务器实例")
    @Description("重新命名服务器实例")
    suspend fun CommandSender.rename(name: String, newName: String) {
        if (!checkPermission(MCSM.PERMISSION_SERVER)) return
        if (renameInstance(name, newName, false)) sendMessage("原[$name]修改[$newName]成功.")
        else sendMessage("没有找到[$name]实例.")
    }

    internal suspend fun renameInstance(name: String, newName: String, isEvent: Boolean): Boolean {
        val server = serverInstances[name]
        return if (server != null) {
            serverInstances.remove(name)
            serverInstances[newName] = server

            if (isLoadGeneralPluginInterface) {
                // 发布改名广播
                // 不是事件就发布改名广播
                if (!isEvent) RenameEvent(MCSM.id, name, newName).broadcast()
            }

            true
        } else false
    }

    @SubCommand("start", "启动")
    @Description("启动实例")
    suspend fun CommandSender.start(name: String) {
        if (!serverCheck(name, MCSM.PERMISSION_START)) return
        serverInstances[name]?.let { instance ->
            runCatching { api.openInstance(instance.uuid, instance.daemonUUid, apiKey) }.onSuccess {
                sendMessage("开启实例[${it["instanceUuid"]}]成功")
            }.onFailure {
                if (it.localizedMessage == "实例未处于关闭状态，无法再进行启动") {
                    sendMessage("实例未处于关闭状态,尝试获取在线人数请稍等...")
                    val result = api.sendCommand(name, "list")
                    if (result.isNotEmpty()) {
                        sendMessage("$result\n服务器未卡死,如果是tps低等问题请联系管理员重启.")
                        return
                    }
                    sendMessage("获取在线人数失败,开始强行停止服务器...")
                    runCatching { api.killInstance(instance.uuid, instance.daemonUUid, apiKey) }.onSuccess {
                        sendMessage("强行停止服务器成功,开始启动服务器...")
                        start(name)
                    }
                }
            }
        }
    }

    @SubCommand("stop", "停止")
    @Description("停止实例")
    suspend fun CommandSender.stop(name: String) {
        if (!serverCheck(name, MCSM.PERMISSION_SERVER)) return
        serverInstances[name]?.let {
            runCatching { api.stopInstance(it.uuid, it.daemonUUid, apiKey) }.onSuccess {
                sendMessage("关闭实例[${it["instanceUuid"]}]成功")
            }.onFailure { sendMessage(it.localizedMessage) }
        }
    }

    @SubCommand("kill", "终止")
    @Description("终止实例")
    suspend fun CommandSender.kill(name: String) {
        if (!serverCheck(name, MCSM.PERMISSION_SERVER)) return
        serverInstances[name]?.let {
            runCatching { api.killInstance(it.uuid, it.daemonUUid, apiKey) }.onSuccess {
                sendMessage("终止实例[${it["instanceUuid"]}]成功")
            }.onFailure { sendMessage(it.localizedMessage) }
        }
    }

    @SubCommand("restart", "重启")
    @Description("重启实例")
    suspend fun CommandSender.restart(name: String) {
        if (!serverCheck(name, MCSM.PERMISSION_SERVER)) return
        serverInstances[name]?.let {
            runCatching { api.restartInstance(it.uuid, it.daemonUUid, apiKey) }.onSuccess {
                sendMessage("重启实例[${it["instanceUuid"]}]成功")
            }.onFailure { sendMessage(it.localizedMessage) }
        }
    }

    /**
     * 以后面带一个空格的方式拼接可变参数
     *
     * @param arg 可变参数
     * @return 拼接后的字符串
     */
    private fun spliceVararg(arg: Array<out String>): String {
        var str = ""
        arg.forEach { str += "$it " }
        return str
    }

    @SubCommand("command", "cmd", "命令")
    @Description("向实例发送命令")
    suspend fun CommandSender.command(name: String, vararg command: String) {
        if (!serverCheck(name, MCSM.PERMISSION_SERVER)) return
        val result = api.sendCommand(name, spliceVararg(command))
        if (result.isNotEmpty()) sendMessage(result)
    }

    private suspend fun MCSManagerApi.sendCommand(name: String, command: String): String {
        serverInstances[name]?.let { server ->
            runCatching { sendCommandInstance(server.uuid, server.daemonUUid, apiKey, command) }.onSuccess {
                val time = LocalTime.now().withNano(0)
                delay(1000)
                var message = ""
                getInstanceLog(server.uuid, server.daemonUUid, apiKey).toRemoveColorCodeMinecraftLog()
                    .filter { it.channels == "minecraft/DedicatedServer" }
                    .filter { it.time >= time && it.time.hour == time.hour && it.time.minute == time.minute }
                    .filter { !"<.*>".toRegex().containsMatchIn(it.message) }
                    .forEach { message += "${it.message}\n" }
                return if (message.isNotEmpty()) message.substring(0, message.length - 1) else message
            }.onFailure { return it.localizedMessage }
        }
        return ""
    }

    @SubCommand("createTasks", "ct", "创建任务")
    @Description("向实例创建计划任务")
    suspend fun CommandSender.createTasks(
        name: String, tasksName: String, count: Int, time: Int, vararg command: String
    ) {
        if (!serverCheck(name, MCSM.PERMISSION_ADMIN)) return
        serverInstances[name]?.let {
            val tasks = Tasks(name = tasksName, count = count, time = time, payload = spliceVararg(command))
            runCatching { api.createScheduledTasks(it.uuid, it.daemonUUid, apiKey, tasks) }.onSuccess {
                sendMessage("创建计划任务[$tasksName]:$it")
            }.onFailure { sendMessage(it.localizedMessage) }
        }
    }

    @SubCommand("deleteTasks", "dt", "删除任务")
    @Description("向实例删除计划任务")
    suspend fun CommandSender.deleteTasks(name: String, tasksName: String) {
        if (!serverCheck(name, MCSM.PERMISSION_ADMIN)) return
        serverInstances[name]?.let {
            runCatching { api.deleteScheduledTasks(it.uuid, it.daemonUUid, apiKey, tasksName) }.onSuccess {
                sendMessage("删除计划任务[$tasksName]:$it")
            }.onFailure { sendMessage(it.localizedMessage) }
        }
    }

    /**
     * 获取实例日志
     *
     * @param name 实例名称
     * @param regex 匹配的正则
     * @param index 匹配组的第几个
     */
    @SubCommand("log", "获取日志消息")
    @Description("获取指定实例的日志")
    suspend fun CommandSender.getInstanceLog(name: String, regex: String, index: Int, maxSize: Int = 20) {
        if (!serverCheck(name, MCSM.PERMISSION_ADMIN)) return
        serverInstances[name]?.let {
            runCatching { api.getInstanceLog(it.uuid, it.daemonUUid, apiKey) }.onSuccess {
                sendMessage(logToString(it.removeColorCodeLog(), regex.toRegex(), index, maxSize))
            }.onFailure { sendMessage(it.localizedMessage) }
        }
    }

    private fun logToString(log: String, regex: Regex, index: Int, maxSize: Int): String {
        val matchResults = regex.findAll(log).toList()
        if (matchResults.isEmpty()) return "无匹配日志..."
        if (maxSize == 1) return matchResults.last().groupValues[index]
        val results = if (matchResults.size > maxSize)
            matchResults.subList(matchResults.size - maxSize, matchResults.size)
        else
            matchResults.subList(0, matchResults.size)
        var message = ""
        results.forEach { result ->
            message += "${result.groupValues[index]}\n"
        }
        return message.substring(0, message.length - 1)
    }

    @SubCommand
    @Description("设置tps功能启用")
    suspend fun CommandSender.setTps(value: Boolean) {
        if (!checkPermission(MCSM.PERMISSION_ADMIN)) return
        isTps = value
        sendMessage("tps功能:$isTps")
    }
}
