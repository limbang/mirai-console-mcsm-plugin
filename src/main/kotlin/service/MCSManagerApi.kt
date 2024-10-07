/*
 * Copyright (c) 2022-2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.service

import retrofit2.http.*
import top.limbang.mcsm.model.*

/**
 * ### Minecraft服务器管理器服务
 */
interface MCSManagerApi {

    /**
     * ### 获取所有守护进程列表
     * @param apikey API 密钥
     */
    @GET("service/remote_services")
    suspend fun getAllDaemonList(@Query("apikey") apikey: String): MCSMResponse<List<Daemon>>

    /**
     * ### 开启实例
     * @param uuid 守护进程下的实例 UUID
     * @param daemonId 守护进程 UUID
     * @param apikey API 密钥
     */
    @GET("protected_instance/open")
    suspend fun openInstance(
        @Query("uuid") uuid: String,
        @Query("daemonId") daemonId: String,
        @Query("apikey") apikey: String
    ): MCSMResponse<Map<String, String>>

    /**
     * ### 关闭实例
     * @param uuid 守护进程下的实例 UUID
     * @param daemonId 守护进程 UUID
     * @param apikey API 密钥
     */
    @GET("protected_instance/stop")
    suspend fun stopInstance(
        @Query("uuid") uuid: String,
        @Query("daemonId") daemonId: String,
        @Query("apikey") apikey: String
    ): MCSMResponse<Map<String, String>>

    /**
     * ### 终止实例
     * @param uuid 守护进程下的实例 UUID
     * @param daemonId 守护进程 UUID
     * @param apikey API 密钥
     */
    @GET("protected_instance/kill")
    suspend fun killInstance(
        @Query("uuid") uuid: String,
        @Query("daemonId") daemonId: String,
        @Query("apikey") apikey: String
    ): MCSMResponse<Map<String, String>>

    /**
     * ### 重启实例
     * @param uuid 守护进程下的实例 UUID
     * @param daemonId 守护进程 UUID
     * @param apikey API 密钥
     */
    @GET("protected_instance/restart")
    suspend fun restartInstance(
        @Query("uuid") uuid: String,
        @Query("daemonId") daemonId: String,
        @Query("apikey") apikey: String
    ): MCSMResponse<Map<String, String>>

    /**
     * ### 发送命令到应用实例
     * @param uuid 守护进程下的实例 UUID
     * @param daemonId 守护进程 UUID
     * @param apikey API 密钥
     * @param command //要执行的命令 如：ping www.baidu.com
     */
    @GET("protected_instance/command")
    suspend fun sendCommandInstance(
        @Query("uuid") uuid: String,
        @Query("daemonId") daemonId: String,
        @Query("apikey") apikey: String,
        @Query("command") command: String
    ): MCSMResponse<Unit>

    /**
     * ### 创建计划任务
     * @param uuid 守护进程下的实例 UUID
     * @param daemonId 守护进程 UUID
     * @param apikey API 密钥
     */
    @POST("protected_schedule")
    suspend fun createScheduledTasks(
        @Query("uuid") uuid: String,
        @Query("daemonId") daemonId: String,
        @Query("apikey") apikey: String,
        @Body tasks: TasksRequest
    ): MCSMResponse<Boolean>

    /**
     * ### 创建计划任务
     * @param uuid 守护进程下的实例 UUID
     * @param daemonId 守护进程 UUID
     * @param apikey API 密钥
     */
    @DELETE("protected_schedule")
    suspend fun deleteScheduledTasks(
        @Query("uuid") uuid: String,
        @Query("daemonId") daemonId: String,
        @Query("apikey") apikey: String,
        @Query("task_name") taskName: String
    ): MCSMResponse<Boolean>

    /**
     * ### 获取实例日志
     * @param uuid 守护进程下的实例 UUID
     * @param daemonId 守护进程 UUID
     * @param apikey API 密钥
     */
    @GET("protected_instance/outputlog")
    suspend fun getInstanceLog(
        @Query("uuid") uuid: String,
        @Query("daemonId") daemonId: String,
        @Query("apikey") apikey: String
    ): MCSMResponse<String>

    /**
     * ### 请求下载文件
     * @param uuid 守护进程下的实例 UUID
     * @param daemonId 守护进程 UUID
     * @param apikey API 密钥
     * @param fileName 文件名称
     */
    @GET("files/download")
    suspend fun filesDownload(
        @Query("uuid") uuid: String,
        @Query("daemonId") daemonId: String,
        @Query("apikey") apikey: String,
        @Query("file_name") fileName: String
    ): MCSMResponse<FilesDownloadResponse>

    /**
     * ### 查看指定实例的文件列表
     * @param uuid 守护进程下的实例 UUID
     * @param daemonId 守护进程 UUID
     * @param apikey API 密钥
     * @param target 查看的文件目录，如：/xxx
     * @param page 第几页，0代表第一页
     * @param pageSize 每页容量，不得超过40
     */
    @GET("/api/files/list")
    suspend fun filesList(
        @Query("uuid") uuid: String,
        @Query("daemonId") daemonId: String,
        @Query("apikey") apikey: String,
        @Query("target") target: String,
        @Query("page") page: Int = 0,
        @Query("page_size") pageSize: Int = 40,
    ): MCSMResponse<FilesListResponse>

}