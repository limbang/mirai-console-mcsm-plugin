/*
 * Copyright (c) 2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package network.entity.response


import kotlinx.serialization.Serializable

/**
 * 模组信息数据类
 *
 * 用于表示 Minecraft 模组的详细信息，包括模组的基本属性、状态和文件信息。
 *
 * @property name Mod/插件名称
 * @property version 版本号
 * @property id 模组 ID
 * @property description 描述
 * @property type 类型 "mod" | "plugin" | "unknown"
 * @property file 文件名
 * @property enabled 是否启用
 * @property hash SHA1 哈希值（可选）
 * @property folder 所在文件夹（mods/plugins）
 */
@Serializable
data class ModInfo(
    val name: String,
    val version: String,
    val id: String,
    val description: String,
    val type: String,
    val `file`: String,
    val enabled: Boolean,
    val hash: String,
    val folder: String,
)
