/*
 * Copyright (c) 2022-2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.mirai

import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.registerTo
import top.limbang.mcsm.RetrofitClient
import top.limbang.mcsm.mirai.command.GroupConfigCompositeCommand
import top.limbang.mcsm.mirai.command.MCSMCompositeCommand
import top.limbang.mcsm.mirai.command.MCSMCompositeCommand.apiMap
import top.limbang.mcsm.mirai.command.MCSMCompositeCommand.renameInstance
import top.limbang.mcsm.mirai.command.MCSMListener
import top.limbang.mcsm.mirai.command.ModCompositeCommand
import top.limbang.mcsm.mirai.config.MCSMData
import top.limbang.mcsm.mirai.config.MCSMData.mcsmList
import top.limbang.mirai.event.GroupRenameEvent

object MCSM : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mcsm",
        name = "MCSManager API",
        version = "1.1.6",
    ) {
        author("limbang")
        info("MCSManager api 插件")
        dependsOn("top.limbang.general-plugin-interface", true)
    }
) {
    /** 是否加载通用插件接口 */
    val isLoadGeneralPluginInterface: Boolean = try {
        Class.forName("top.limbang.mirai.GeneralPluginInterface")
        true
    } catch (e: Exception) {
        logger.info("未加载通用插件接口,limbang插件系列改名无法同步.")
        logger.info("前往 https://github.com/limbang/mirai-plugin-general-interface/releases 下载")
        false
    }

    override fun onDisable() {
        MCSMCompositeCommand.unregister()
        GroupConfigCompositeCommand.unregister()
        ModCompositeCommand.unregister()
        MCSMListener.cancel()
    }

    override fun onEnable() {
        MCSMData.reload()
        MCSMCompositeCommand.register()
        GroupConfigCompositeCommand.register()
        ModCompositeCommand.register()
        initAPI() // 数据读取后初始化API

        // 创建事件通道
        val eventChannel = GlobalEventChannel.parentScope(this)

        MCSMListener.registerTo(eventChannel)

        if (!isLoadGeneralPluginInterface) return
        // 监听改名事件
        eventChannel.subscribeAlways<GroupRenameEvent> {
            logger.info("GroupRenameEvent: pluginId = $pluginId oldName = $oldName groupId=$groupId newName = $newName")
            if (pluginId == MCSM.id) return@subscribeAlways
            renameInstance(oldName, newName, groupId, true)
        }
    }

    private fun initAPI() {
        mcsmList.forEach {
            val apiService = RetrofitClient(it.url).getMCSManagerApi()
            apiMap[it.key] = apiService
        }
    }

}

