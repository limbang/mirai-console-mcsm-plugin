package utils

import top.limbang.mcsm.model.FilesDownload
import top.limbang.mcsm.utils.toDownloadUrl
import kotlin.test.Test

class MCSMUtilsKtTest {

    @Test
    fun toDownloadUrl() {
        val apiUrl = "https://mcsm.baidu.com/api/"
        println(FilesDownload("password","localhost").toDownloadUrl(apiUrl))
        println(FilesDownload("password","ws://baidu.com").toDownloadUrl(apiUrl))
        println(FilesDownload("password","wss://localhost").toDownloadUrl(apiUrl))
        println(FilesDownload("password","http://baidu.com").toDownloadUrl(apiUrl))
        println(FilesDownload("password","https://localhost").toDownloadUrl(apiUrl))
    }
}