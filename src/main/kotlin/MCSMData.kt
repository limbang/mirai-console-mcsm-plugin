/*
 * Copyright (c) 2022-2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Member

/**
 * ### 插件配置
 */
object MCSMData : AutoSavePluginData("mcsm") {
    @ValueDescription("mcsm 接口地址")
    var apiUrl: String by value()

    @ValueDescription("mcsm 接口key")
    var apiKey: String by value()

    @ValueDescription("存放服务器实例")
    var serverInstances: MutableMap<String, ServerInstances> by value()

    @ValueDescription("tps查看,默认打开")
    var isTps: Boolean by value(true)

    @ValueDescription("黑名单")
    val blacklist : MutableSet<Member> by value()

    @ValueDescription("是否启用发送消息到服务器功能，默认启用")
    var isEnabledSendMessage : Boolean by value(true)

    @ValueDescription("是否启用通知消息功能，默认启用")
    var isEnabledNotice : Boolean by value(true)

    @ValueDescription("是否启用强制启动功能，默认启用")
    var isEnabledForceStart : Boolean by value(true)

}

/**
 * ## 服务器实例
 *
 * @property uuid 服务器uuid
 * @property daemonUUid 守护进程uuid
 */
@Serializable
data class ServerInstances(
    val uuid: String,
    val daemonUUid: String
)
