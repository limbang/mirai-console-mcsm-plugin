/*
 * Copyright (c) 2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package service

import kotlinx.coroutines.runBlocking
import top.limbang.mcsm.RetrofitClient
import top.limbang.mcsm.service.MCSManagerApi
import java.io.FileInputStream
import java.net.URL
import java.util.*
import kotlin.test.Test

internal class MCSManagerApiTest() {
    private val api: MCSManagerApi
    private val key: String
    private val uuid: String
    private val remoteUuid: String

    init {
        val prop = Properties()
        prop.load(FileInputStream("debug-sandbox/local.properties"))
        val url = prop.getProperty("url")
        key = prop.getProperty("key")
        uuid = prop.getProperty("uuid")
        remoteUuid = prop.getProperty("remoteUuid")
        api = RetrofitClient(url).getMCSManagerApi()
    }

    private val charMessageRegex = """\[(\d{2}:\d{2}:\d{2})].*DedicatedServer]:\s<((?!\[吉祥物]亮亮).*)>\s(.*)""".toRegex()
    private val opLogRegex = """\[(\d{2}:\d{2}:\d{2})].*DedicatedServer]:\s\[(.*):(.*)]""".toRegex()
    private val joinedTheGameRegex = """\[(\d{2}:\d{2}:\d{2})].*DedicatedServer]:\s(.*) joined the game""".toRegex()
    private val leftTheGameRegex = """\[(\d{2}:\d{2}:\d{2})].*DedicatedServer]:\s(.*) left the game""".toRegex()

    @Test
    fun filesDownload(){
        runBlocking {
            val filesDownload = api.filesDownload(uuid, remoteUuid, key,"logs/latest.log").data!!
            val url = "${filesDownload.addr.run {
                when {
                    indexOf("wss://") != -1 -> replace("wss://", "https://")
                    indexOf("ws://") != -1 -> replace("ws://", "http://")
                    indexOf("https://") != -1 && indexOf("http://") != -1 -> "http://$this"
                    else -> "http://$this"
                }
            }}/download/${filesDownload.password}/latest.log"

            val log = URL(url).readText()

            val charMessageResult = charMessageRegex.findAll(log)
            charMessageResult.forEach {
                val (time,name, msg) = it.destructured
                println("$time $name:$msg")
            }

            val opLogResult = opLogRegex.findAll(log)
            opLogResult.forEach {
                val (time,name,info) = it.destructured
                println("$time $name:$info")
            }

            val joinedTheGameResult = joinedTheGameRegex.findAll(log)
            joinedTheGameResult.forEach {
                val (time,name) = it.destructured
                println("$time $name 加入游戏")
            }

            val leftTheGameResult = leftTheGameRegex.findAll(log)
            leftTheGameResult.forEach {
                val (time,name) = it.destructured
                println("$time $name 离开游戏")
            }

        }
    }
}