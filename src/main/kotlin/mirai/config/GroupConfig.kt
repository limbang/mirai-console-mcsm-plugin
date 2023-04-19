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
import net.mamoe.mirai.contact.Member

@Serializable
data class GroupConfig(
    @ValueDescription("是否启用强制启动功能，默认启用")
    var isEnabledForceStart : Boolean = true,
    @ValueDescription("是否启用通知消息功能，默认启用")
    var isEnabledNotice : Boolean = true,
    @ValueDescription("是否启用发送消息到服务器功能，默认启用")
    var isEnabledSendMessage : Boolean = true,
    @ValueDescription("tps查看,默认打开")
    var isTps: Boolean = true,
    @ValueDescription("黑名单")
    val blacklist : MutableSet<Member> = mutableSetOf()
)