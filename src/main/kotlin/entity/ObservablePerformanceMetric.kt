/*
 * Copyright (c) 2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.entity

import top.limbang.mcsm.network.entity.response.ObservableResponse

/**
 * Observable 模组监测到的一个性能指标条目
 *
 * @param type 类型
 * @param rate 性能速率值（单位：纳秒）
 * @param position 位置信息
 * @param category 类别（实体或方块）
 */
data class ObservablePerformanceMetric(
    val type: String,
    val rate: Double,
    val position: ObservableResponse.Position,
    val category: Category
) {
    /**
     * 性能指标类别枚举
     */
    enum class Category(val label: String) {
        ENTITY("[实体]"),  // 实体类别
        BLOCK("[方块]")   // 方块类别
    }

    /**
     * 计算微秒每刻 (μs/t)
     * 将纳秒转换为微秒，1 微秒 = 1000 纳秒
     */
    val microsPerTick: Int
        get() = (rate / 1000).toInt()
}