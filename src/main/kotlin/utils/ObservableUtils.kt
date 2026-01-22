/*
 * Copyright (c) 2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.utils

import top.limbang.mcsm.entity.ObservablePerformanceMetric
import top.limbang.mcsm.network.entity.response.ObservableResponse

/**
 * 分析并打印格式化的性能报告
 *
 * @param topN 每一维度显示的最高消耗条目数量，默认为 10
 */
fun ObservableResponse.printPerformanceAnalysis(topN: Int = 10): String = buildString {
    // 1. 获取所有维度并排序
    val allDimensions = (data.entities.keys + data.blocks.keys).distinct().sorted()

    allDimensions.forEach { dim ->
        // 2. 转换并合并当前维度的所有数据
        val metrics = mutableListOf<ObservablePerformanceMetric>()

        data.entities[dim]?.mapTo(metrics) {
            ObservablePerformanceMetric(it.type, it.rate, it.position, ObservablePerformanceMetric.Category.ENTITY)
        }
        data.blocks[dim]?.mapTo(metrics) {
            ObservablePerformanceMetric(it.type, it.rate, it.position, ObservablePerformanceMetric.Category.BLOCK)
        }

        // 3. 统计该维度的总消耗 (μs/t)
        val totalMicros = metrics.sumOf { it.rate } / 1000

        // 4. 构建维度页眉
        appendLine("\n" + "═".repeat(70))
        appendLine("维度: $dim")
        appendLine("总消耗: ${"%,d".format(totalMicros.toInt())} μs/t | 采样对象总数: ${metrics.size}")
        appendLine("─".repeat(70))

        // 5. 排序并取 Top N
        metrics.sortedByDescending { it.rate }
            .take(topN)
            .forEach { metric ->
                val categoryLabel = metric.category.label
                val typeStr = metric.type.padEnd(35)
                val rateStr = metric.microsPerTick.toString().padStart(8)
                val posStr = "(${metric.position.x}, ${metric.position.y}, ${metric.position.z})"

                // 拼接每一行数据
                appendLine("$categoryLabel $typeStr $rateStr μs/t    $posStr")
            }
    }
}