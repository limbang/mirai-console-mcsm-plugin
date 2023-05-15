/*
 * Copyright (c) 2022-2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.entity

import java.time.LocalTime

/**
 * Minecraft log
 *
 * @property time 时间
 * @property thread 线程
 * @property level log级别
 * @property channels 通道 forge 特有
 * @property contents 内容
 * @constructor Create empty Minecraft log
 */
data class MinecraftLog(
    val time: LocalTime,
    val thread: String,
    val level: Level,
    val channels: String,
    val contents: String
)
