/*
 * Copyright 2022-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.service

import retrofit2.http.*
import top.limbang.mcsm.model.Daemon
import top.limbang.mcsm.model.Tasks

/**
 * ### Minecraft服务器管理器服务
 */
interface MCSManagerApi {

    /**
     * ### 获取所有守护进程列表
     * @param apikey API 密钥
     */
    @GET("service/remote_services")
    suspend fun getAllDaemonList(@Query("apikey") apikey: String): List<Daemon>

    /**
     * ### 开启实例
     * @param uuid 守护进程下的实例 UUID
     * @param remoteUuid 守护进程 UUID
     * @param apikey API 密钥
     */
    @GET("protected_instance/open")
    suspend fun openInstance(
        @Query("uuid") uuid: String,
        @Query("remote_uuid") remoteUuid: String,
        @Query("apikey") apikey: String
    ) : Map<String, String>

    /**
     * ### 关闭实例
     * @param uuid 守护进程下的实例 UUID
     * @param remoteUuid 守护进程 UUID
     * @param apikey API 密钥
     */
    @GET("protected_instance/stop")
    suspend fun stopInstance(
        @Query("uuid") uuid: String,
        @Query("remote_uuid") remoteUuid: String,
        @Query("apikey") apikey: String
    ) : Map<String, String>

    /**
     * ### 终止实例
     * @param uuid 守护进程下的实例 UUID
     * @param remoteUuid 守护进程 UUID
     * @param apikey API 密钥
     */
    @GET("protected_instance/kill")
    suspend fun killInstance(
        @Query("uuid") uuid: String,
        @Query("remote_uuid") remoteUuid: String,
        @Query("apikey") apikey: String
    ) : Map<String, String>

    /**
     * ### 重启实例
     * @param uuid 守护进程下的实例 UUID
     * @param remoteUuid 守护进程 UUID
     * @param apikey API 密钥
     */
    @GET("protected_instance/restart")
    suspend fun restartInstance(
        @Query("uuid") uuid: String,
        @Query("remote_uuid") remoteUuid: String,
        @Query("apikey") apikey: String
    ) : Map<String, String>

    /**
     * ### 发送命令到应用实例
     * @param uuid 守护进程下的实例 UUID
     * @param remoteUuid 守护进程 UUID
     * @param apikey API 密钥
     * @param command //要执行的命令 如：ping www.baidu.com
     */
    @GET("protected_instance/command")
    suspend fun sendCommandInstance(
        @Query("uuid") uuid: String,
        @Query("remote_uuid") remoteUuid: String,
        @Query("apikey") apikey: String,
        @Query("command") command: String
    ) : Map<String, String>

    /**
     * ### 创建计划任务
     * @param uuid 守护进程下的实例 UUID
     * @param remoteUuid 守护进程 UUID
     * @param apikey API 密钥
     */
    @POST("protected_schedule")
    suspend fun createScheduledTasks(
        @Query("uuid") uuid: String,
        @Query("remote_uuid") remoteUuid: String,
        @Query("apikey") apikey: String,
        @Body tasks : Tasks
    ) : Boolean

    /**
     * ### 创建计划任务
     * @param uuid 守护进程下的实例 UUID
     * @param remoteUuid 守护进程 UUID
     * @param apikey API 密钥
     */
    @DELETE("protected_schedule")
    suspend fun deleteScheduledTasks(
        @Query("uuid") uuid: String,
        @Query("remote_uuid") remoteUuid: String,
        @Query("apikey") apikey: String,
        @Query("task_name") taskName : String
    ) : Boolean

    /**
     * ### 获取实例日志
     * @param uuid 守护进程下的实例 UUID
     * @param remoteUuid 守护进程 UUID
     * @param apikey API 密钥
     */
    @GET("protected_instance/outputlog")
    suspend fun getInstanceLog(
        @Query("uuid") uuid: String,
        @Query("remote_uuid") remoteUuid: String,
        @Query("apikey") apikey: String
    ) : String
}