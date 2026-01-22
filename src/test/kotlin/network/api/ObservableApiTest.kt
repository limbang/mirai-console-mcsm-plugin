/*
 * Copyright (c) 2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package network.api

import kotlinx.coroutines.runBlocking
import top.limbang.mcsm.network.RetrofitClient
import top.limbang.mcsm.network.api.ObservableApi
import top.limbang.mcsm.utils.printPerformanceAnalysis
import kotlin.test.Test

internal class ObservableApiTest {

    private val api = RetrofitClient(baseUrl = "https://observable.tas.sh/", isDebug = true).create<ObservableApi>()

    @Test
    fun getDiagnosticInformation() {
        runBlocking {
            val observable = api.getDiagnosticInformation("Fklkc")
            println(observable.printPerformanceAnalysis())
        }
    }
}