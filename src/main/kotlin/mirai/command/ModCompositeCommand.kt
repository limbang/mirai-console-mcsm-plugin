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
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import top.limbang.mcsm.mirai.MCSM
import top.limbang.mcsm.mirai.command.MCSMCompositeCommand.getInstance
import top.limbang.mcsm.mirai.command.MCSMCompositeCommand.isNotGroup
import top.limbang.mcsm.utils.toRemoveColorCodeMinecraftLog
import java.time.LocalTime

object ModCompositeCommand : CompositeCommand(
    owner = MCSM,
    primaryName = "mod",
    description = "Mod的一些指令"
) {
    @SubCommand("spark")
    @Description("向实例发送spark命令")
    suspend fun UserCommandSender.spark(name: String) {
        if (isNotGroup()) return
        val instance = getInstance(name)

        runCatching {
            MCSMCompositeCommand.apiMap[instance.apiKey]!!.sendCommandInstance(
                instance.uuid,
                instance.daemonUUID,
                instance.apiKey,
                "spark profiler --threads * --timeout 30"
            )
        }.onSuccess {
            val time = LocalTime.now().withNano(0)
            delay(1000)
            val result = MCSMCompositeCommand.apiMap[instance.apiKey]!!.getInstanceLog(
                instance.uuid, instance.daemonUUID, instance.apiKey
            ).data!!
                .toRemoveColorCodeMinecraftLog()
                .filter { it.time >= time && it.time.hour == time.hour && it.time.minute == time.minute }
                .filter { "Initializing".toRegex().containsMatchIn(it.contents) }
            if (result.isEmpty()) {
                sendMessage("未安装 spark 模组")
                return
            }
            sendMessage("正在初始化 Spark 分析器,30秒后返回结果...")
            do {
                delay(1000)
                val sparkResult = MCSMCompositeCommand.apiMap[instance.apiKey]!!.getInstanceLog(
                    instance.uuid, instance.daemonUUID, instance.apiKey
                ).data!!
                    .toRemoveColorCodeMinecraftLog()
                    .filter { it.time >= time }
                    .filter { "https".toRegex().containsMatchIn(it.contents) }
                if (sparkResult.isNotEmpty()) sendMessage(sparkResult.last().contents)
            } while (sparkResult.isEmpty())
        }.onFailure {
            sendMessage(it.localizedMessage)
        }
    }
}