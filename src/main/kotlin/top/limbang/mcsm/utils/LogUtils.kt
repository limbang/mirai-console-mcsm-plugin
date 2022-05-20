/*
 * Copyright 2022-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.utils

import top.limbang.mcsm.entity.Level
import top.limbang.mcsm.entity.MinecraftLog
import java.time.LocalTime


/**
 * 删除日志的颜色代码
 */
fun String.removeColorCodeLog(): String {
    return """\[[\d;K]*m""".toRegex().replace(this, "")
}

/**
 * 把日志转成 [MinecraftLog]
 */
fun String.toMinecraftLog(): List<MinecraftLog> {
    val minecraftLogList = mutableListOf<MinecraftLog>()
    """\[(\d+):(\d+):(\d+)]\s\[[a-zA-Z\s]+/([A-Z]+)]\s\[([\S/]+)]:\s(.*)""".toRegex().findAll(this).forEach {
        minecraftLogList.add(
            MinecraftLog(
                LocalTime.of(it.groupValues[1].toInt(), it.groupValues[2].toInt(), it.groupValues[3].toInt()),
                Level.valueOf(it.groupValues[4]),
                it.groupValues[5],
                it.groupValues[6]
            )
        )
    }
    return minecraftLogList
}

/**
 * 把日志转成删除颜色代码后的 [MinecraftLog]
 *
 */
fun String.toRemoveColorCodeMinecraftLog() = this.removeColorCodeLog().toMinecraftLog()