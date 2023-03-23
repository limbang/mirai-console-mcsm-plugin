/*
 * Copyright (c) 2022-2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */



package top.limbang.mcsm.interceptor

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import top.limbang.mcsm.exception.MCSMException
import top.limbang.mcsm.model.MCSMResponse
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class StatusInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        // HTTP 不在 200-300 的错误状态码处理
        if (response.isSuccessful.not()) {
            val errorMessage = try {
                Json.decodeFromString<MCSMResponse<String>>(getResponseBody(response.body!!)).data!!
            } catch (e: SerializationException) {
                "序列化错误,服务器异常."
            }
            throw MCSMException(errorMessage)
        }
        return response
    }

    private fun getResponseBody(responseBody: ResponseBody): String {
        val contentLength = responseBody.contentLength()
        val source = responseBody.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        val buffer = source.buffer
        val contentType = responseBody.contentType()
        val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
        return if (contentLength != 0L) buffer.clone().readString(charset) else ""
    }
}

