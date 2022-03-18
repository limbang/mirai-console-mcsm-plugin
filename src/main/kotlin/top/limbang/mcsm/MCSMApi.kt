/*
 * Copyright 2022-2022 limbang and contributors.
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
import top.limbang.mcsm.service.MCSMService
import java.util.concurrent.TimeUnit

class MCSMApi(
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
     * ### 创建 Minecraft服务器管理器 服务
     */
    private val minecraftServerManagerService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(format.toConverterFactory())
            .client(okHttpClient)
            .build()
        retrofit.create(MCSMService::class.java)
    }


    /**
     * ### 获取 Minecraft服务器管理器 服务
     */
    fun get(): MCSMService {
        return minecraftServerManagerService
    }
}