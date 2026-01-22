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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 分析并打印格式化的性能报告
 *
 * @param topN 每一维度显示的最高消耗条目数量，默认为 10
 */
fun ObservableResponse.printPerformanceAnalysis(topN: Int = 10): String = buildString {
    /// 1. 格式化诊断元数据
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())
    val startTime = formatter.format(Instant.ofEpochMilli(diagnostics.start))

    // 2. 构建报告总标题
    appendLine("Observable 性能分析报告")
    appendLine("----------------------")
    appendLine("开始时间: $startTime")
    appendLine("分析时长: ${diagnostics.duration/1000} 秒")
    appendLine("游戏版本: ${diagnostics.minecraftVersion} (${diagnostics.modLoader})")
    appendLine("Observable 版本: ${diagnostics.observableVersion}")

    // 3. 获取并排序所有维度
    val allDimensions = (data.entities.keys + data.blocks.keys).sorted()

    allDimensions.forEach { dim ->
        // 4. 汇总当前维度的指标
        val metrics = mutableListOf<ObservablePerformanceMetric>()

        data.entities[dim]?.mapTo(metrics) {
            ObservablePerformanceMetric(it.type, it.rate, it.position, ObservablePerformanceMetric.Category.ENTITY)
        }
        data.blocks[dim]?.mapTo(metrics) {
            ObservablePerformanceMetric(it.type, it.rate, it.position, ObservablePerformanceMetric.Category.BLOCK)
        }

        val totalMicros = metrics.sumOf { it.rate } / 1000

        // 5. 构建维度区块头
        appendLine("\n◈ 维度: $dim")
        appendLine("  总消耗: ${"%,d".format(totalMicros.toInt())} μs/t | 对象总数: ${metrics.size}")
        appendLine("  " + "─".repeat(64))

        // 6. 排序并取 Top N 写入
        metrics.sortedByDescending { it.rate }
            .take(topN)
            .forEach { metric ->
                val categoryLabel = metric.category.label
                val typeStr = metric.type.padEnd(35)
                val rateStr = metric.microsPerTick.toString().padStart(8)
                val posStr = "(${metric.position.x}, ${metric.position.y}, ${metric.position.z})"

                appendLine("  $categoryLabel $typeStr $rateStr μs/t   $posStr")
            }
    }
}