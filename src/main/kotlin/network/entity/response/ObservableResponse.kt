/*
 * Copyright (c) 2026 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.network.entity.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Observable 性能监控数据的根对象
 */
@Serializable
data class ObservableResponse(
    /** Observable 性能监控数据的根对象 */
    val data: ObservableData,
    /** 诊断与元数据 */
    val diagnostics: Diagnostics
){
    @Serializable
    data class ObservableData(
        /** 实体性能数据，Key 为维度名 (如 "minecraft:overworld") */
        val entities: Map<String, List<EntityEntry>>,
        /** 方块性能数据，Key 为维度名 */
        val blocks: Map<String, List<BlockEntry>>,
        /** 总运行 Tick 数 */
        val ticks: Int,
        /** 全局追踪信息 */
        val traces: TraceNode
    ){
        /**
         * 实体性能条目
         */
        @Serializable
        data class EntityEntry(
            /** 实体 ID */
            val entityId: Int,
            /** 实体类型 */
            val type: String,
            /** 实体位置 */
            val position: Position,
            /** 每 tick 消耗的时间或占用比 */
            val rate: Double,
            /** 运行 Tick 数 */
            val ticks: Int,
            /** 全局追踪信息 */
            val traces: TraceNode
        )

        /**
         * 方块性能条目 (通常指 TileEntity/BlockEntity)
         */
        @Serializable
        data class BlockEntry(
            /** 方块类型 */
            val type: String,
            /** 方块位置 */
            val position: Position,
            /** 每 tick 消耗的时间或占用比 */
            val rate: Double,
            /** 运行 Tick 数 */
            val ticks: Int,
            /** 全局追踪信息 */
            val traces: TraceNode
        )
    }

    @Serializable
    data class Diagnostics(
        /** 执行分析的用户，可能为空 */
        val user: String? = null,
        /** 分析开始的时间戳 */
        val start: Long,
        /** 分析持续时长 */
        val duration: Int,
        /** Minecraft 版本 */
        val minecraftVersion: String,
        /** 模组加载器 */
        val modLoader: String,
        /** Observable 版本 */
        val observableVersion: String,
        /** 诊断与元数据 */
        val additionalDiagnostics: AdditionalDiagnostics
    ){
        @Serializable
        data class AdditionalDiagnostics(
            /** 系统报告详情 */
            @SerialName("System Report") val systemReport: String,
            /** 已安装的模组列表字符串 */
            @SerialName("Mods") val mods: String,
        )
    }


    /**
     * 坐标位置信息
     */
    @Serializable
    data class Position(
        val x: Int,
        val y: Int,
        val z: Int
    )

    /**
     * 性能追踪节点信息
     */
    @Serializable
    data class TraceNode(
        /** 类名 */
        val className: String,
        /** 方法名 */
        val methodName: String,
        /** 节点调用次数 */
        val count: Int,
        /** 子节点的类名/描述列表 */
        val children: List<String>? = null
    )
}


