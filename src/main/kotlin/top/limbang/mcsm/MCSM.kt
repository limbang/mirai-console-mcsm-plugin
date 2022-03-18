package top.limbang.mcsm

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

object MCSM : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mcsm",
        name = "mcsm",
        version = "1.0.0",
    ) {
        author("limbang")
    }
) {
    override fun onDisable() {
        MCSMCompositeCommand.unregister()
    }

    override fun onEnable() {
        MCSMData.reload()
        MCSMCompositeCommand.register()
    }
}

