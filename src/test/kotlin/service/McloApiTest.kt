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
import kotlin.test.Test

internal class  McloApiTest {
    val api = RetrofitClient(apiUrl = "https://api.mclo.gs/1/").getMcloApi()

    @Test
    fun pasteLogFile(){
        runBlocking {
           val mclo = api.pasteLogFile("test 12345")
            println( mclo.success)
            println( mclo.url)
        }
    }

    @Test
    fun getLogContent(){
        runBlocking {
            println( api.getLogContent("LNY97uU").string())
        }
    }

    @Test
    fun getInsights(){
        runBlocking {
            println( api.getInsights("LNY97uU").string())
        }
    }
}