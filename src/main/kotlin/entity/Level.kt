/*
 * Copyright (c) 2022-2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.entity


enum class Level(private val levelStr: String) {

    ERROR("ERROR"), WARN("WARN"), FATAL("FATAL"), INFO("INFO"), DEBUG("DEBUG");

    override fun toString(): String {
        return levelStr
    }
}

