/*
 * Copyright (c) 2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.network.entity.response

import kotlinx.serialization.Serializable

@Serializable
data class TransferTask(
    val id: String? = null,           // 仅上传任务有
    val path: String,
    val total: Long,
    val current: Long,
    val status: Int,
    val error: String? = null,        // 仅下载任务可能有
    val type: String                  // "download" 或 "upload"
)