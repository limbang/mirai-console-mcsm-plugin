/*
 * Copyright (c) 2022-2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.model

import kotlinx.serialization.Serializable

@Serializable
data class TasksRequest (
    val name: String,
    val action : String = "command",
    val count : Int,
    val type : Int = 1,
    val payload : String,
    val time : Int
)