/*
 * Copyright (c) 2023-2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.command

import kotlinx.coroutines.delay
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.utils.MiraiLogger
import top.limbang.mcsm.MCSM
import top.limbang.mcsm.command.MCSMCompositeCommand.getInstance
import top.limbang.mcsm.command.MCSMCompositeCommand.isNotGroup
import top.limbang.mcsm.network.RetrofitClient
import top.limbang.mcsm.network.api.ObservableApi
import top.limbang.mcsm.utils.printPerformanceAnalysis
import top.limbang.mcsm.utils.toRemoveColorCodeMinecraftLog
import java.time.LocalTime
import kotlin.time.Duration.Companion.milliseconds

object ModCompositeCommand : CompositeCommand(
    owner = MCSM, primaryName = "mod", description = "Mod的一些指令"
) {

    internal val logger: MiraiLogger = MiraiLogger.Factory.create(this::class.java)

    private val observableApi = RetrofitClient("https://observable.tas.sh/").create<ObservableApi>()

    /**
     * # 使用 [Spark](https://github.com/lucko/spark) 命令检测实例并返回结果
     *
     * 发送 `spark profiler --threads * --timeout 30` 命令
     *
     * @param name 实例名称
     */
    @SubCommand("spark")
    @Description("向实例发送 spark 检测命令")
    suspend fun UserCommandSender.spark(name: String) {
        if (isNotGroup()) return
        val instance = getInstance(name)

        runCatching {
            MCSMCompositeCommand.apiMap[instance.apiKey]!!.sendCommandInstance(
                instance.uuid, instance.daemonUUID, instance.apiKey, "spark profiler --threads * --timeout 30"
            )
        }.onSuccess {
            val time = LocalTime.now().withNano(0)
            delay(1000.milliseconds)
            val result = MCSMCompositeCommand.apiMap[instance.apiKey]!!.getInstanceLog(
                instance.uuid, instance.daemonUUID, instance.apiKey
            ).data!!.toRemoveColorCodeMinecraftLog()
                .filter { it.time >= time && it.time.hour == time.hour && it.time.minute == time.minute }
                .filter { """\[⚡]\s(Initializing|Starting)""".toRegex().containsMatchIn(it.contents) }
            if (result.isEmpty()) {
                sendMessage("未安装 spark 模组")
                return
            }
            sendMessage("正在初始化 Spark 分析器,30秒后返回结果...")
            do {
                delay(1000.milliseconds)
                val sparkResult = MCSMCompositeCommand.apiMap[instance.apiKey]!!.getInstanceLog(
                    instance.uuid, instance.daemonUUID, instance.apiKey
                ).data!!.toRemoveColorCodeMinecraftLog().filter { it.time >= time }
                    .filter { """https://spark\.lucko\.me/([a-zA-Z0-9]{5,})""".toRegex().containsMatchIn(it.contents) }
                if (sparkResult.isNotEmpty()) sendMessage(sparkResult.last().contents)
            } while (sparkResult.isEmpty())
        }.onFailure {
            sendMessage(it.message ?: "未知错误")
            logger.error(it)
        }
    }

    /**
     * # 使用 [Observable](https://github.com/tasgon/observable) 命令检测实例并返回结果
     *
     * 发送 `observable run 30` 命令
     *
     * @param name 实例名称
     */
    @SubCommand("observable")
    @Description("向实例发送 observable 检测命令")
    suspend fun UserCommandSender.observable(name: String) {
        if (isNotGroup()) return
        val instance = getInstance(name)

        runCatching {
            MCSMCompositeCommand.apiMap[instance.apiKey]!!.sendCommandInstance(
                instance.uuid, instance.daemonUUID, instance.apiKey, "observable run 30"
            )
        }.onSuccess {
            val time = LocalTime.now().withNano(0)
            delay(1000.milliseconds)
            val result = MCSMCompositeCommand.apiMap[instance.apiKey]!!.getInstanceLog(
                instance.uuid, instance.daemonUUID, instance.apiKey
            ).data!!.toRemoveColorCodeMinecraftLog()
                .filter { it.time >= time && it.time.hour == time.hour && it.time.minute == time.minute }
                .filter { """Running\sObservable\s.*30""".toRegex().containsMatchIn(it.contents) }
            if (result.isEmpty()) {
                sendMessage("未安装 observable 模组")
                return
            }
            sendMessage("正在初始化 observable 分析器,30秒后返回结果...")
            do {
                delay(1000.milliseconds)
                val sparkResult = MCSMCompositeCommand.apiMap[instance.apiKey]!!.getInstanceLog(
                    instance.uuid, instance.daemonUUID, instance.apiKey
                ).data!!.toRemoveColorCodeMinecraftLog().filter { it.time >= time }
                    .filter { "https".toRegex().containsMatchIn(it.contents) }
                if (sparkResult.isNotEmpty()) {
                    sendMessage(sparkResult.last().contents)
                    val id = """/p/(\w+)""".toRegex().find(sparkResult.last().contents)!!.groupValues[1]
                    sendMessage(observableApi.getDiagnosticInformation(id).printPerformanceAnalysis())
                }
            } while (sparkResult.isEmpty())
        }.onFailure {
            sendMessage(it.message ?: "未知错误")
            logger.error(it)
        }
    }
}