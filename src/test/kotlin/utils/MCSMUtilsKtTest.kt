package utils

import top.limbang.mcsm.model.FilesDownloadResponse
import top.limbang.mcsm.utils.toDownloadUrl
import kotlin.test.Test

class MCSMUtilsKtTest {

    @Test
    fun toDownloadUrl() {
        val apiUrl = "https://mcsm.baidu.com/api/"
        println(FilesDownloadResponse("password","localhost").toDownloadUrl(apiUrl))
        println(FilesDownloadResponse("password","ws://baidu.com").toDownloadUrl(apiUrl))
        println(FilesDownloadResponse("password","wss://localhost").toDownloadUrl(apiUrl))
        println(FilesDownloadResponse("password","http://baidu.com").toDownloadUrl(apiUrl))
        println(FilesDownloadResponse("password","https://localhost").toDownloadUrl(apiUrl))
    }
}