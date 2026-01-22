/*
 * Copyright (c) 2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import top.limbang.mcsm.network.entity.response.ObservableResponse

interface ObservableApi {

    /**
     * 获取诊断信息
     *
     * https://observable.tas.sh/v1/get/Fklkc
     */
    @GET("/v1/get/{id}")
    suspend fun getDiagnosticInformation(@Path("id") id: String): ObservableResponse
}