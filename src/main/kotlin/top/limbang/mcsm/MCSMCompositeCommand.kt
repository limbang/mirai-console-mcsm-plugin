/*
 * Copyright 2022-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import top.limbang.mcsm.MCSMData.apiKey
import top.limbang.mcsm.MCSMData.apiUrl
import top.limbang.mcsm.MCSMData.serverInstances
import top.limbang.mcsm.model.Tasks
import top.limbang.mcsm.service.MCSMService


object MCSMCompositeCommand : CompositeCommand(MCSM, "mcsm") {
    private var service: MCSMService? = null

    @SubCommand("add", "添加")
    @Description("添加api管理")
    suspend fun CommandSender.add(url: String, key: String) {
        apiUrl = url
        apiKey = key
        service = MCSMApi(url).get()
        sendMessage("[$name]添加成功.")
    }

    @SubCommand("daemonList", "守护进程列表")
    @Description("获取守护进程列表")
    suspend fun CommandSender.daemonList() {
        val service = getService(this) ?: return
        runCatching { service.getAllDaemonList(apiKey) }.onSuccess {
            it.forEach { daemon ->
                var msg = "守护进程[${daemon.uuid}],实例如下:\n"
                daemon.instances.forEach { instances ->
                    msg += "${instances.instanceUuid} -> ${instances.config.nickname}\n"
                }
                sendMessage(msg)
            }
        }.onFailure {
            sendMessage(it.message ?: "未知错误")
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
    suspend fun CommandSender.addServerInstances(name: String, uuid: String, daemonUUid: String) {
        serverInstances[name] = ServerInstances(uuid, daemonUUid)
        sendMessage("[$name]添加成功.")
    }

    @SubCommand("deleteServerInstances", "删除服务器实例")
    @Description("删除需要管理的服务器实例")
    suspend fun CommandSender.deleteServerInstances(name: String) {
        serverInstances.remove(name) ?: return
        sendMessage("[$name]删除成功.")
    }

    @SubCommand("start", "启动")
    @Description("启动实例")
    suspend fun CommandSender.start(name: String) {
        if (serverInstances[name] == null) {
            sendMessage("${name}不存在，请查询后输入")
            return
        }
        val instances = serverInstances[name]!!
        val service = getService(this) ?: return
        runCatching { service.openInstance(instances.uuid, instances.daemonUUid, apiKey) }.onSuccess {
            sendMessage("开启实例[${it["instanceUuid"]}]成功")
        }.onFailure {
            sendMessage(it.localizedMessage)
        }
    }

    @SubCommand("stop", "停止")
    @Description("停止实例")
    suspend fun CommandSender.stop(name: String) {
        if (serverInstances[name] == null) {
            sendMessage("${name}不存在，请查询后输入")
            return
        }
        val instances = serverInstances[name]!!
        val service = getService(this) ?: return
        runCatching { service.stopInstance(instances.uuid, instances.daemonUUid, apiKey) }.onSuccess {
            sendMessage("关闭实例[${it["instanceUuid"]}]成功")
        }.onFailure {
            sendMessage(it.localizedMessage)
        }
    }

    @SubCommand("kill", "终止")
    @Description("终止实例")
    suspend fun CommandSender.kill(name: String) {
        if (serverInstances[name] == null) {
            sendMessage("${name}不存在，请查询后输入")
            return
        }
        val instances = serverInstances[name]!!
        val service = getService(this) ?: return
        runCatching { service.killInstance(instances.uuid, instances.daemonUUid, apiKey) }.onSuccess {
            sendMessage("终止实例[${it["instanceUuid"]}]成功")
        }.onFailure {
            sendMessage(it.localizedMessage)
        }
    }

    @SubCommand("restart", "重启")
    @Description("重启实例")
    suspend fun CommandSender.restart(name: String) {
        if (serverInstances[name] == null) {
            sendMessage("${name}不存在，请查询后输入")
            return
        }
        val instances = serverInstances[name]!!
        val service = getService(this) ?: return
        runCatching { service.restartInstance(instances.uuid, instances.daemonUUid, apiKey) }.onSuccess {
            sendMessage("重启实例[${it["instanceUuid"]}]成功")
        }.onFailure {
            sendMessage(it.localizedMessage)
        }
    }

    @SubCommand("command", "cmd", "命令")
    @Description("向实例发送命令")
    suspend fun CommandSender.command(name: String, vararg command: String) {
        var cmd = ""
        command.forEach { cmd += "$it " }

        if (serverInstances[name] == null) {
            sendMessage("${name}不存在，请查询后输入")
            return
        }
        val instances = serverInstances[name]!!
        val service = getService(this) ?: return
        runCatching { service.sendCommandInstance(instances.uuid, instances.daemonUUid, apiKey, cmd) }.onSuccess {
            sendMessage("向实例[${it["instanceUuid"]}]发送命令成功")
        }.onFailure {
            sendMessage(it.localizedMessage)
        }
    }

    @SubCommand("createTasks", "ct", "创建任务")
    @Description("向实例创建计划任务")
    suspend fun CommandSender.createTasks(name: String,tasksName: String,count:Int,time:Int, vararg command: String) {
        var payload = ""
        command.forEach { payload += "$it " }
        if (serverInstances[name] == null) {
            sendMessage("${name}不存在，请查询后输入")
            return
        }
        val instances = serverInstances[name]!!
        val service = getService(this) ?: return
        val tasks = Tasks(name = tasksName, count = count, time = time, payload = payload)
        runCatching { service.createScheduledTasks(instances.uuid, instances.daemonUUid, apiKey, tasks) }.onSuccess {
            sendMessage("创建计划任务[$tasksName]:$it")
        }.onFailure {
            sendMessage(it.localizedMessage)
        }
    }

    @SubCommand("deleteTasks", "dt", "删除任务")
    @Description("向实例删除计划任务")
    suspend fun CommandSender.deleteTasks(name: String,tasksName: String) {
        if (serverInstances[name] == null) {
            sendMessage("${name}不存在，请查询后输入")
            return
        }
        val instances = serverInstances[name]!!
        val service = getService(this) ?: return
        runCatching { service.deleteScheduledTasks(instances.uuid, instances.daemonUUid, apiKey, tasksName) }.onSuccess {
            sendMessage("删除计划任务[$tasksName]:$it")
        }.onFailure {
            sendMessage(it.localizedMessage)
        }
    }


    private suspend fun getService(commandSender: CommandSender): MCSMService? {
        if (apiUrl.isEmpty()) {
            commandSender.sendMessage("未设置API信息.")
            return null
        }
        service = service ?: MCSMApi(apiUrl).get()
        return service
    }
}

