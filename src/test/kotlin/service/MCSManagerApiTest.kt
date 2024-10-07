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
import top.limbang.mcsm.model.FilesRequest
import top.limbang.mcsm.service.MCSManagerApi
import top.limbang.mcsm.utils.*
import java.io.*
import java.net.URL
import java.util.*
import java.util.zip.GZIPInputStream
import kotlin.test.Test
import kotlin.test.assertNotNull

internal class MCSManagerApiTest() {
    private val api: MCSManagerApi
    private val key: String
    private val uuid: String
    private val daemonId: String
    private val url: String

    init {
        val prop = Properties()
        prop.load(FileInputStream("debug-sandbox/local.properties"))
        url = prop.getProperty("url")
        key = prop.getProperty("key")
        uuid = prop.getProperty("uuid")
        daemonId = prop.getProperty("daemonId")
        api = RetrofitClient(url).getMCSManagerApi()
    }

    @Test
    fun getAllDaemonList() {
        runBlocking {
           val response = api.getAllDaemonList(key)
            assertNotNull(response.data)
        }
    }

    @Test
    fun openInstance() {
        runBlocking {
            val response = api.openInstance(uuid, daemonId,key)
            assertNotNull(response.data)
        }
    }

    @Test
    fun sendCommandInstance() {
        runBlocking {
            val response = api.sendCommandInstance(uuid, daemonId,key,"list")
            assertNotNull(response.data)
        }
    }

    @Test
    fun filesDownload() {
        runBlocking {
            val filesDownload = api.filesDownload(uuid, daemonId, key, "logs/latest.log").data!!

            val log = URL(filesDownload.toDownloadUrl(apiUrl = url)).readText().toMinecraftLog()

            log.forEach {
                val result = it.toJoinTheExitGame()
                if (result.isNotEmpty()) println(result)
            }

            log.forEach {
                val result = it.toCharMessageGame()
                if (result.isNotEmpty()) println(result)
            }

            log.forEach {
                val result = it.toAdminLog()
                if (result.isNotEmpty()) println(result)
            }

        }
    }

    @Test
    fun filesList() {
        runBlocking {
            val filesList = api.filesList(uuid, daemonId, key, "crash-reports").data!!
            // 找出最新时间的日志
            val item = filesList.items.maxBy { it.toLocalDateTime() }
            println(item.name)
        }
    }

    @Test
    fun zipFile() {
        runBlocking {
            val filesList = api.filesList(uuid, daemonId, key, "logs").data!!
            val regex = """2023-05-20-\d+.log.gz""".toRegex()

            var concatStream: InputStream = ByteArrayInputStream(ByteArray(0))

            filesList.items.filter {
                regex.find(it.name) != null
            }.forEach {
                // 获取下载链接
                val url = api.filesDownload(uuid, daemonId, key, "logs/${it.name}").data!!.toDownloadUrl(url)
                val fileStream = GZIPInputStream(URL(url).openStream())
                concatStream = SequenceInputStream(concatStream, fileStream)
            }

            val reader = BufferedReader(InputStreamReader(concatStream))


            val log = reader.readText().toMinecraftLog()
            log.forEach {
                val result = it.toAdminLog()
                if (result.isNotEmpty()) println(result)
            }
        }
    }

    @Test
    fun files() {
        runBlocking {
            val whitelist = api.files(uuid, daemonId, key, FilesRequest("whitelist.json")).data!!
            println(whitelist)
        }
    }
}