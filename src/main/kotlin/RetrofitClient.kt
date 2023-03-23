/*
 * Copyright (c) 2022-2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import top.limbang.mcsm.converter.toConverterFactory
import top.limbang.mcsm.interceptor.StatusInterceptor
import top.limbang.mcsm.service.MCSManagerApi
import java.util.concurrent.TimeUnit

class RetrofitClient(
    apiUrl: String,
    httpLoggingInterceptor: Interceptor? = null,
    format: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
) {

    /**
     * ### 创建 okhttp 客户端
     */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .also { if (httpLoggingInterceptor != null) it.addInterceptor(httpLoggingInterceptor) }
            .addInterceptor(StatusInterceptor())
            .build()
    }

    /**
     * ### 创建 Retrofit 实例
     */
    private val instance by lazy {
        Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(format.toConverterFactory())
            .client(okHttpClient)
            .build()
    }


    /**
     * ### 获取 MCSManager api
     */
    fun getMCSManagerApi(): MCSManagerApi {
        return instance.create(MCSManagerApi::class.java)
    }
}