/*
 * Copyright 2022-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package utils

import top.limbang.mcsm.utils.toMinecraftLog
import kotlin.test.Test


internal class LogUtilsKtTest {

    // 原版 fabric
    private val vanilla = """
    [20:52:51] [Server thread/INFO]: Preparing level "world"
    [20:52:51] [Server thread/INFO]: Loaded 488 advancements
    [20:52:51] [Server thread/INFO]: Preparing start region for level 0
    [20:52:52] [Server thread/INFO]: Preparing spawn area: 64%
    [20:52:52] [Server thread/INFO]: Done (1.542s)! For help, type "help" or "?"
    """

    private val forge = """
    [21:03:59] [Server thread/INFO] [FML]: Injecting itemstacks
    [21:04:06] [Server thread/INFO] [minecraft/MinecraftServer]: Preparing spawn area: 75%
    [21:04:07] [Server thread/INFO] [minecraft/MinecraftServer]: Preparing spawn area: 89%
    [21:04:08] [Server thread/INFO] [minecraft/DedicatedServer]: Done (9.145s)! For help,type "help" or "?"
    """


    @Test
    fun toMinecraftLog() {
        println(vanilla.toMinecraftLog())
        println(forge.toMinecraftLog())
    }
}