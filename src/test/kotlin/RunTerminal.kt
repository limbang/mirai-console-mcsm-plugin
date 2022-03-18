package top.limbang

import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import top.limbang.mcsm.MCSM
import java.io.File
import java.io.FileInputStream
import java.util.*


fun setupWorkingDir() {
    // see: net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
    System.setProperty("user.dir", File("debug-sandbox").absolutePath)
}

suspend fun main() {
    setupWorkingDir()

    MiraiConsoleTerminalLoader.startAsDaemon()

    val pluginInstance = MCSM

    pluginInstance.load() // 主动加载插件, Console 会调用 Mcsm.onLoad
    pluginInstance.enable() // 主动启用插件, Console 会调用 Mcsm.onEnable

    // 读取账号配置
    val pros = Properties()
    val file = FileInputStream("local.properties")
    pros.load(file)

    val username = (pros["username"] as String).toLong()
    val password = pros["password"] as String


    val bot = MiraiConsole.addBot(username, password).alsoLogin()

    MiraiConsole.job.join()
}
