/*
 * Copyright (c) 2022-2023 limbang and contributors.
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
    return """\[[\d;]*?[mK]""".toRegex().replace(this, "")
}

private val minecraftLogRegex =
    """\[.*?(\d{2}):(\d{2}):(\d{2}).*?]\s\[(.*?)/([A-Z]{4,5})](?:\s\[(.*?)])?:\s(.*)""".toRegex()

/**
 * 把日志转成 [MinecraftLog]
 */
fun String.toMinecraftLog(): List<MinecraftLog> {
    val minecraftLogList = mutableListOf<MinecraftLog>()
    minecraftLogRegex.findAll(this).forEach {
        val (hour, minute, second, thread, level, channels, contents) = it.destructured
        minecraftLogList.add(
            MinecraftLog(
                time = LocalTime.of(hour.toInt(), minute.toInt(), second.toInt()),
                thread = thread,
                level = try {
                    Level.valueOf(level)
                } catch (e: IllegalArgumentException) {
                    Level.INFO
                },
                channels = channels.ifEmpty { "" },
                contents = contents
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

/**
 * 加入退出游戏正则
 *
 */
private val JOIN_THE_EXIT_GAME_REGEX = """^(.*) (joined|left) the game""".toRegex()

/**
 * 转成加入退出游戏日志
 *
 * @return 如果不是该日志返回空字符串
 */
fun MinecraftLog.toJoinTheExitGame(): String {
    val (name, state) = JOIN_THE_EXIT_GAME_REGEX.find(contents)?.destructured ?: return ""
    return "$time $name ${if (state == "joined") "加入游戏" else "退出游戏"}"
}

/**
 * 玩家聊天正则
 *
 */
private val CHAR_MESSAGE_REGEX = """^<((?!\[吉祥物]亮亮|亮亮).*)> (.*)""".toRegex()

/**
 * 转成玩家聊天日志
 *
 * @return 如果不是该日志返回空字符串
 */
fun MinecraftLog.toCharMessageGame(): String {
    val (name, message) = CHAR_MESSAGE_REGEX.find(contents)?.destructured ?: return ""
    return "$time <$name> $message"
}

/**
 * 管理员操作正则
 *
 */
private val Admin_LOG_REGEX =
    """^\[?(.*?)(?>: )?((?:Given|Gave).+|Opped.+|De-opped.+|Set.+Mode|Teleported.+|Made.+operator|Added.+time|Banned.+|Unbanned.+|Kicked.+game|Killed.+)]?""".toRegex()

/**
 * 设置模式正则
 */
private val SET_MODE_REGEX = """^Set (.+?)(?:'s)? game mode to (.+) Mode""".toRegex()

/**
 * 传送正则
 */
private val TELEPORTED_REGEX = """^Teleported (.+) to ([^]]+)]?$""".toRegex()

/**
 * 加速时间正则
 */
private val SPEED_UP_TIME_REGEX = """^Added (\d+) to the time""".toRegex()

/**
 * op deop kill ban pardon 正则
 */
private val OP_DEOP_KILL_BAN_PARDAN_REGEX = """^(De-opped|Opped|Killed|Banned player|Unbanned player) (.+)""".toRegex()

/**
 * 提出游戏正则
 */
private val KICKED_REGEX = """^Kicked (.+) from the game(?::\s)?(.*)""".toRegex()

private val GAVE_XP_REGEX = """^(?:Gave|Given) (.+) levels to ([^]]+)""".toRegex()

private val GATE_ITEM_REGEX = """^(?:Gave|Given)\s+(.+) \* (\d+) to ([^]]+)""".toRegex()

/**
 * 转成管理员操作日志
 *
 * @return 如果不是该日志返回空字符串
 */
fun MinecraftLog.toAdminLog(): String {
    val (name, content) = Admin_LOG_REGEX.find(contents)?.destructured ?: return ""

    SET_MODE_REGEX.find(content)?.let {
        val (player, modeEn) = it.destructured
        val mode = when (modeEn) {
            "Survival" -> "生存"
            "Creative" -> "创造"
            "Adventure" -> "冒险"
            "Spectator" -> "旁观"
            else -> "错误"
        }
        return "$time ${name.ifEmpty { "服务器" }}: 设置${if (player == "own") "自己" else " $player "}为${mode}模式"
    }

    TELEPORTED_REGEX.find(content)?.let {
        val (player, toPlayer) = it.destructured
        return "$time ${name.ifEmpty { "服务器" }}: 已将 $player 传送至 $toPlayer"
    }

    SPEED_UP_TIME_REGEX.find(content)?.let {
        return "$time ${name.ifEmpty { "服务器" }}: 将时间调快了 ${it.groupValues[1]}"
    }

    OP_DEOP_KILL_BAN_PARDAN_REGEX.find(content)?.let {
        val (cmd, player) = it.destructured
        val msg = when (cmd) {
            "De-opped" -> "已夺去 $player 的管理员权限"
            "Opped" -> "已将 $player 设为管理员"
            "Killed" -> "已清除 $player"
            "Banned player" -> "已封禁玩家 $player"
            "Unbanned player" -> "已解封玩家 $player"
            else -> "错误"
        }
        return "$time ${name.ifEmpty { "服务器" }}: $msg"
    }

    KICKED_REGEX.find(content)?.let {
        val (player, msg) = it.destructured
        return "$time ${name.ifEmpty { "服务器" }}: 已将 $player 踢出游戏${if (msg.isNotEmpty()) ": $msg" else ""}"
    }

    GAVE_XP_REGEX.find(content)?.let {
        val (levels, player) = it.destructured
        return "$time ${name.ifEmpty { "服务器" }}: 给予 $player $levels 级经验"
    }

    GATE_ITEM_REGEX.find(content)?.let {
        val (item, quantity, player) = it.destructured
        return "$time ${name.ifEmpty { "服务器" }}: 给予 $player $item * $quantity"
    }


    return contents
}
