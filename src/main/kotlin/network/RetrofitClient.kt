/*
 * Copyright (c) 2022-2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import top.limbang.mcsm.network.config.SerializationConfig.json
import top.limbang.mcsm.network.interceptor.StatusInterceptor
import java.util.concurrent.TimeUnit

class RetrofitClient(
    baseUrl: String,
    private val isDebug: Boolean = false,
    private val timeout: Long = 15L
) {
    private val logger = LoggerFactory.getLogger(RetrofitClient::class.java)

    // 确保 baseUrl 以 / 结尾，避免 Retrofit 运行时崩溃
    private val sanitizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

    /**
     * ### 创建 okhttp 客户端
     */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .addInterceptor(StatusInterceptor())
            .apply {
                // 根据调试模式添加日志拦截器
                if (isDebug) {
                    val loggingInterceptor = HttpLoggingInterceptor { message ->
                        if (message.isNotBlank()) logger.debug(message)
                    }.apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                    addInterceptor(loggingInterceptor)
                }
            }
            .build()
    }

    /**
     * ### 创建 Retrofit 实例
     */
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(sanitizedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }


    /**
     * 创建指定类型的 Retrofit 服务实例
     *
     * @param T 需要创建的服务接口类型，必须使用 reified 类型参数
     * @return 返回指定类型的 Retrofit 服务实例
     */
    inline fun <reified T> create(): T = retrofit.create(T::class.java)

}