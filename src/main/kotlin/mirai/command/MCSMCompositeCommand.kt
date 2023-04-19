/*
 * Copyright (c) 2022-2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.mirai.command

import kotlinx.coroutines.delay
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.broadcast
import top.limbang.mcsm.RetrofitClient
import top.limbang.mcsm.mirai.MCSM
import top.limbang.mcsm.mirai.MCSM.isLoadGeneralPluginInterface
import top.limbang.mcsm.mirai.config.GroupInstance
import top.limbang.mcsm.mirai.config.MCSMData.groupInstances
import top.limbang.mcsm.mirai.config.MCSMData.mcsmList
import top.limbang.mcsm.model.MCSManager
import top.limbang.mcsm.model.Tasks
import top.limbang.mcsm.service.MCSManagerApi
import top.limbang.mcsm.utils.removeColorCodeLog
import top.limbang.mcsm.utils.toRemoveColorCodeMinecraftLog
import top.limbang.mirai.event.GroupRenameEvent
import java.time.Instant
import java.time.ZoneId

@OptIn(ConsoleExperimentalApi::class)
object MCSMCompositeCommand : CompositeCommand(
    owner = MCSM,
    primaryName = "mcsm",
    description = "控制 MCSM API 的指令"
) {
    private val http = """^https?://""".toRegex()
    val api: MutableMap<String, MCSManagerApi> = mutableMapOf()

    @SubCommand("addmcsm")
    @Description("添加需要管理的 MCSManager")
    suspend fun CommandSender.addmcsm(
        @Name("MCSM 名称") name: String,
        @Name("MCSM URL") url: String,
        @Name("MCSM KEY") key: String
    ) {

        if (!http.containsMatchIn(url)) {
            sendMessage("添加 API URL 错误,URL要以： https:// 或 http:// 开头。")
            return
        }

        val apiUrl = if (url.endsWith("/")) "${url}api/" else "$url/api/"

        val apiService = RetrofitClient(apiUrl).getMCSManagerApi()

        // 验证是否能正常获取守护进程列表
        runCatching { apiService.getAllDaemonList(key) }.onFailure {
            sendMessage(it.localizedMessage)
            return
        }.onSuccess {

            val mcsm = MCSManager(
                name = name,
                url = apiUrl,
                key = key,
                daemons = it.data!!
            )

            // 删掉重复项
            deleteMCSM(name)

            // 添加
            mcsmList.add(mcsm)
            api[key] = apiService
            sendMessage("[$name]添加成功")
        }
    }

    @SubCommand("deletemcsm")
    @Description("删除 MCSManager")
    suspend fun CommandSender.deletemcsm(@Name("MCSM 名称") name: String) {
        if (deleteMCSM(name)) sendMessage("[$name]删除成功") else sendMessage("[$name]不存在")
    }

    private fun deleteMCSM(name: String): Boolean {
        mcsmList.find { it.name == name }?.let {
            mcsmList.remove(it)
            return true
        }
        return false
    }

    @SubCommand("listmcsm")
    @Description("查看所有MCSM列表")
    suspend fun CommandSender.listmcsm() {
        var msg = "所有列表如下:\n"

        mcsmList.forEach { mcsm ->
            msg += "  MCSM名称[${mcsm.name}]:\n"
            mcsm.daemons.forEach { daemon ->
                msg += "    守护进程ID[${daemon.uuid.substring(0, 6)}]:\n"
                daemon.instances.forEach { instance ->
                    msg += "      实例名称[${instance.config.nickname}]\n"
                }
            }
        }

        sendMessage(msg)
    }

    internal suspend fun UserCommandSender.isNotGroup() = (subject !is Group).also {
        if (it) sendMessage("请在群内发送命令")
    }

    @SubCommand("list")
    @Description("获取本群的实例列表")
    suspend fun UserCommandSender.listGroup() {
        if (isNotGroup()) return
        val instances = groupInstances[subject.id]
        if (instances == null) {
            sendMessage("当前列表为空.")
            return
        }
        var list = "列表如下:\n"
        instances.forEach {
            list += "[${it.name}] "
        }
        sendMessage(list)
    }


    @SubCommand("add")
    @Description("添加本群需要管理的服务器实例,参数参考可以发送:/mcsm listmcsm")
    suspend fun UserCommandSender.addGroupInstances(
        @Name("昵称") name: String,
        @Name("MCSM名称") mcsmName: String,
        @Name("守护进程UUID") daemonUUID: String,
        @Name("实例名称") instanceName: String
    ) {
        if (isNotGroup()) return
        if (groupInstances[subject.id] == null)
            groupInstances[subject.id] = mutableListOf()

        val mcsm = mcsmList.find { it.name == mcsmName } ?: return
        val daemon = mcsm.daemons.find { it.uuid.indexOf(daemonUUID) != -1 } ?: return
        val instance = daemon.instances.find { it.config.nickname == instanceName } ?: return

        val instances = groupInstances[subject.id]!!

        // 如果已经添加就删除在添加
        val removeInstance = instances.find { it.name == name }
        if (removeInstance != null) {
            instances.remove(removeInstance)
        }

        instances.add(
            GroupInstance(
                name = name,
                uuid = instance.instanceUuid,
                daemonUUID = daemon.uuid,
                apiUrl = mcsm.url,
                apiKey = mcsm.key
            )
        )

        // 检查下群配置,没有就初始化
        GroupConfigCompositeCommand.checkGroupConfig(subject.id)

        sendMessage("[$name]添加成功.")
    }

    @SubCommand("delete")
    @Description("删除本群的服务器实例")
    suspend fun UserCommandSender.deleteGroupInstances(name: String) {
        if (isNotGroup()) return
        if (groupInstances[subject.id]!!.remove(getInstance(name))) sendMessage("[$name]删除成功.")
    }

    @SubCommand("rename")
    @Description("重新命名服务器实例")
    suspend fun UserCommandSender.rename(name: String, newName: String) {
        if (isNotGroup()) return
        if (renameInstance(name, newName, subject.id, false)) sendMessage("原[$name]修改[$newName]成功.")
        else sendMessage("没有找到[$name]实例.")
    }

    internal suspend fun renameInstance(name: String, newName: String, groupID: Long, isEvent: Boolean): Boolean {
        val instance = groupInstances[groupID]!!.find { name == it.name }
        return if (instance != null) {
            groupInstances[groupID]!!.remove(instance)
            groupInstances[groupID]!!.add(instance.copy(name = newName))

            if (isLoadGeneralPluginInterface) {
                // 发布改名广播
                // 不是事件就发布改名广播
                if (!isEvent) GroupRenameEvent(groupID, MCSM.id, name, newName).broadcast()
            }
            true
        } else false
    }

    internal suspend fun UserCommandSender.getInstance(name: String) = try {
        groupInstances[subject.id]!!.find { name == it.name }!!
    } catch (e: Exception) {
        sendMessage(e.localizedMessage)
        throw e
    }

    @SubCommand("start")
    @Description("启动实例")
    suspend fun UserCommandSender.start(name: String) {
        if (isNotGroup()) return
        val instance = getInstance(name)
        runCatching {
            api[instance.apiKey]!!.openInstance(
                uuid = instance.uuid,
                remoteUuid = instance.daemonUUID,
                apikey = instance.apiKey
            )
        }.onSuccess {
            sendMessage("开启实例[$name]成功")
        }.onFailure {
            sendMessage(it.localizedMessage)
        }
    }

    @SubCommand("stop")
    @Description("停止实例")
    suspend fun UserCommandSender.stop(name: String) {
        if (isNotGroup()) return
        val instance = getInstance(name)
        runCatching {
            api[instance.apiKey]!!.stopInstance(
                uuid = instance.uuid,
                remoteUuid = instance.daemonUUID,
                apikey = instance.apiKey
            )
        }.onSuccess {
            sendMessage("关闭实例[$name]成功")
        }.onFailure { sendMessage(it.localizedMessage) }
    }


    @SubCommand("kill")
    @Description("终止实例")
    suspend fun UserCommandSender.kill(name: String) {
        if (isNotGroup()) return
        val instance = getInstance(name)
        runCatching {
            api[instance.apiKey]!!.killInstance(
                uuid = instance.uuid,
                remoteUuid = instance.daemonUUID,
                apikey = instance.apiKey
            )
        }.onSuccess {
            sendMessage("终止实例[$name]成功")
        }.onFailure { sendMessage(it.localizedMessage) }
    }


    @SubCommand("restart")
    @Description("重启实例")
    suspend fun UserCommandSender.restart(name: String) {
        if (isNotGroup()) return
        val instance = getInstance(name)
        runCatching {
            api[instance.apiKey]!!.restartInstance(
                uuid = instance.uuid,
                remoteUuid = instance.daemonUUID,
                apikey = instance.apiKey
            )
        }.onSuccess {
            sendMessage("重启实例[$name]成功")
        }.onFailure { sendMessage(it.localizedMessage) }
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
        return str.trim()
    }

    @SubCommand("cmd")
    @Description("向实例发送命令")
    suspend fun UserCommandSender.command(name: String, vararg command: String) {
        if (isNotGroup()) return
        val instance = getInstance(name)
        val result = api[instance.apiKey]!!.sendCommand(subject.id, name, spliceVararg(command))
        if (result.isNotEmpty()) sendMessage(result)
    }


    private suspend fun MCSManagerApi.sendCommand(groupID: Long, name: String, command: String): String {
        val instance = groupInstances[groupID]!!.find { name == it.name }!!
        runCatching {
            sendCommandInstance(
                instance.uuid,
                instance.daemonUUID,
                instance.apiKey,
                command
            )
        }.onSuccess { response ->
            // 获取命令发送成功的时间戳以默认时区转成时间
            val time = Instant.ofEpochMilli(response.time).atZone(ZoneId.systemDefault()).toLocalTime().withNano(0)
            delay(1000)
            var message = ""
            getInstanceLog(instance.uuid, instance.daemonUUID, instance.apiKey).data!!
                .toRemoveColorCodeMinecraftLog()
                .filter { it.time >= time && it.time.hour == time.hour && it.time.minute == time.minute }
                .filter { !"<.*>".toRegex().containsMatchIn(it.message) }
                .forEach { message += "${it.message}\n" }
            return if (message.isNotEmpty()) message.substring(0, message.length - 1) else message
        }.onFailure { return it.localizedMessage }
        return ""
    }

    @SubCommand("ct")
    @Description("向实例创建计划任务")
    suspend fun UserCommandSender.createTasks(
        name: String, tasksName: String, count: Int, time: Int, vararg command: String
    ) {
        if (isNotGroup()) return
        val instance = getInstance(name)
        val tasks = Tasks(name = tasksName, count = count, time = time, payload = spliceVararg(command))
        runCatching {
            api[instance.apiKey]!!.createScheduledTasks(
                instance.uuid,
                instance.daemonUUID,
                instance.apiKey,
                tasks
            )
        }.onSuccess {
            sendMessage("创建计划任务[$tasksName]:$it")
        }.onFailure { sendMessage(it.localizedMessage) }

    }

    @SubCommand("dt")
    @Description("向实例删除计划任务")
    suspend fun UserCommandSender.deleteTasks(name: String, tasksName: String) {
        if (isNotGroup()) return
        val instance = getInstance(name)
        runCatching {
            api[instance.apiKey]!!.deleteScheduledTasks(
                instance.uuid,
                instance.daemonUUID,
                instance.apiKey,
                tasksName
            )
        }.onSuccess {
            sendMessage("删除计划任务[$tasksName]:$it")
        }.onFailure { sendMessage(it.localizedMessage) }

    }

    /**
     * 获取实例日志
     *
     * @param name 实例名称
     * @param regex 匹配的正则
     * @param index 匹配组的第几个
     */
    @SubCommand("log")
    @Description("获取指定实例的日志")
    suspend fun UserCommandSender.getInstanceLog(name: String, regex: String, index: Int, maxSize: Int = 20) {
        if (isNotGroup()) return
        val instance = getInstance(name)
        runCatching {
            api[instance.apiKey]!!.getInstanceLog(
                instance.uuid,
                instance.daemonUUID,
                instance.apiKey
            )
        }.onSuccess {
            sendMessage(logToString(it.data!!.removeColorCodeLog(), regex.toRegex(), index, maxSize))
        }.onFailure { sendMessage(it.localizedMessage) }
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


}
