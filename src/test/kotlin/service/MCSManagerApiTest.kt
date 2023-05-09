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
import top.limbang.mcsm.utils.*
import java.io.FileInputStream
import java.net.URL
import java.util.*
import kotlin.test.Test

internal class MCSManagerApiTest() {
    private val api: MCSManagerApi
    private val key: String
    private val uuid: String
    private val remoteUuid: String
    private val url: String

    init {
        val prop = Properties()
        prop.load(FileInputStream("debug-sandbox/local.properties"))
        url = prop.getProperty("url")
        key = prop.getProperty("key")
        uuid = prop.getProperty("uuid")
        remoteUuid = prop.getProperty("remoteUuid")
        api = RetrofitClient(url).getMCSManagerApi()
    }
    @Test
    fun filesDownload(){
        runBlocking {
            val filesDownload = api.filesDownload(uuid, remoteUuid, key,"logs/latest.log").data!!

            val log = URL(filesDownload.toDownloadUrl(apiUrl = url)).readText()

            val charMessageResult = charMessageRegex.findAll(log)
            charMessageResult.forEach {
                val (time,name, msg) = it.destructured
                println("$time <$name> $msg")
            }

            val opLogResult = opLogRegex.findAll(log)
            opLogResult.forEach {
                val (time, name, contents) = it.destructured
                println("$time ${name.ifEmpty { "服务器" }}: $contents")
            }

            val joinTheExitGameResult = joinTheExitGameRegex.findAll(log)
            joinTheExitGameResult.forEach {
                val (time, name, state) = it.destructured
                println("$time $name ${if (state == "joined") "加入游戏" else "退出游戏"}")
            }


        }
    }

    @Test
    fun filesList(){
        runBlocking {
            val filesList = api.filesList(uuid, remoteUuid, key,"crash-reports").data!!
            // 找出最新时间的日志
            val item = filesList.items.maxBy { it.toLocalDateTime() }
            println(item.name)
        }
    }
}