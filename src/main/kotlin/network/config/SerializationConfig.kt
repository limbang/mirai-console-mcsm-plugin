/*
 * Copyright (c) 2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.network.config

import kotlinx.serialization.json.Json

/**
 * 序列化配置
 */
object SerializationConfig {
    /**
     * Json 序列化
     */
    val json = Json {
        ignoreUnknownKeys = true // 核心配置：忽略后端返回的多余字段
        isLenient = true         // 宽容模式
        encodeDefaults = true    // 序列化时包含默认值
        coerceInputValues = true // 如果类型不匹配（如null转默认值），尝试强制转换
    }
}