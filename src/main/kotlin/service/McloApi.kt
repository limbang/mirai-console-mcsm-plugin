/*
 * Copyright (c) 2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.service

import model.McloUrl
import okhttp3.ResponseBody
import retrofit2.http.*

interface McloApi {

    /**
     * Paste a log file
     */
    @FormUrlEncoded
    @POST("log")
    suspend fun pasteLogFile(@Field("content") content: String): McloUrl


    /**
     * Get the raw log file content
     */
    @GET("raw/{id}")
    suspend fun getLogContent(@Path("id") id: String): ResponseBody

    /**
     * Get insights
     */
    @GET("insights/{id}")
    suspend fun getInsights(@Path("id") id: String): ResponseBody

}