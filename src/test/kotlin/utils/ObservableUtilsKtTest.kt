/*
 * Copyright (c) 2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package utils

import top.limbang.mcsm.network.config.SerializationConfig
import top.limbang.mcsm.network.entity.response.ObservableResponse
import top.limbang.mcsm.utils.printPerformanceAnalysis
import java.io.File
import kotlin.test.Test

class ObservableUtilsKtTest {

    private val json = File("debug-sandbox/Fklkc.json").readText()

    @Test
    fun printPerformanceAnalysis() {
        val observable = SerializationConfig.json.decodeFromString<ObservableResponse>(json)
        println(observable.printPerformanceAnalysis())
    }
}