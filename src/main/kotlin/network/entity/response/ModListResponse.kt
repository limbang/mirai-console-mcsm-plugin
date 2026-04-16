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
import top.limbang.mcsm.network.entity.response.TransferTask

/**
 * 模组列表响应数据类
 *
 * 用于表示从服务器获取的模组列表响应数据，包含分页信息和模组详情。
 *
 * @property downloadFileFromURLTask 从 URL 下载文件的任务数量
 * @property downloadTasks 下载任务列表，包含待下载的任务标识
 * @property folders 文件夹列表，包含模组所在的文件夹路径
 * @property mods 模组列表，包含当前页面的所有模组详细信息
 * @property page 当前页码
 * @property pageSize 每页显示的模组数量
 * @property total 模组总数
 */
@Serializable
data class ModListResponse(
    val downloadFileFromURLTask: Int,
    val downloadTasks: List<TransferTask>,
    val folders: List<String>,
    val mods: List<ModInfo>,
    val page: Int,
    val pageSize: Int,
    val total: Int
)
