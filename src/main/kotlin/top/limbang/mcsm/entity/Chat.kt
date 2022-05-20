/*
 * Copyright 2022-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.entity

import kotlinx.serialization.Serializable

/**
 * 聊天
 *
 * @property text 内容
 * @property bold 粗体
 * @property italic 斜体
 * @property underlined 下划线
 * @property strikethrough 删除线
 * @property obfuscated 随机滚动,乱码
 * @property color color
 * @property extra
 * @constructor Create empty Chat
 */
@Serializable
data class Chat(
    /** 内容 */
    val text: String,
    /** 粗体 */
    val bold: Boolean = false,
    /** 斜体 */
    val italic: Boolean = false,
    /** 下划线 */
    val underlined: Boolean = false,
    /** 删除线 */
    val strikethrough: Boolean = false,
    /** 随机滚动,乱码 */
    val obfuscated: Boolean = false,
    /** 颜色 */
    val color: String? = null,
    val extra: List<Chat>? = null
)