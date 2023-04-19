/*
 * Copyright (c) 2022-2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.mirai.config

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.limbang.mcsm.model.MCSManager

/**
 * ### 插件配置
 */
object MCSMData : AutoSavePluginData("mcsm") {

    @ValueDescription("QQ群 MCSM 插件配置")
    val groupConfig : MutableMap<Long,GroupConfig> by value()

    @ValueDescription("QQ群 实例数据保存")
    val groupInstances : MutableMap<Long,MutableList<GroupInstance>> by value()

    @ValueDescription("存放 MCSManager")
    val mcsmList : MutableList<MCSManager> by value()
}