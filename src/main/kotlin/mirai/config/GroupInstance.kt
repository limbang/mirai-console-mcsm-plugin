/*
 * Copyright (c) 2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.mirai.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.ValueDescription

@Serializable
data class GroupInstance(
    @ValueDescription("名称") val name: String,
    @ValueDescription("实例UUID") val uuid: String,
    @ValueDescription("守护进程UUID") val daemonUUID: String,
    @ValueDescription("mcsm后台地址如:https//mc.limbang.top") val apiUrl: String,
    @ValueDescription("mcsm key 在 我的信息 -> API 接口密钥") val apiKey: String,
)