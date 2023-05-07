/*
 * Copyright (c) 2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.utils

import top.limbang.mcsm.model.FilesDownload

/**
 * FilesDownload 处理成能下载的 url
 *
 * @return url
 */
fun FilesDownload.toDownloadUrl(apiUrl: String, fileName: String = "latest.log"): String {

    val address = when {
        addr.indexOf("wss://") != -1 -> addr.replace("wss://", "https://")
        addr.indexOf("ws://") != -1 -> addr.replace("ws://", "http://")
        addr.indexOf("https://") == -1 && addr.indexOf("http://") == -1 -> "http://${this.addr}"
        else -> this.addr
    }
    val (baseUrl) = """://(.*?)[:/]""".toRegex().find(apiUrl)!!.destructured

    return "${address.replace("localhost", baseUrl)}/download/${password}/$fileName"
}