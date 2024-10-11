/*
 * Copyright (c) 2024 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.model

import kotlinx.serialization.Serializable

/**
 * ### 获取文件请求
 *
 * @property target 文件路径
 */
@Serializable
data class GetFilesRequest(val target: String)

/**
 * ### 更新文件请求
 *
 * @property target 文件路径
 * @property text 文件内容
 */
@Serializable
data class UpdateFilesRequest(val target: String, val text: String)