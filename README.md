# mirai-console-mcsm-plugin

[mcsm](https://github.com/MCSManager/MCSManager) api 控制

```shell
# 添加api管理
/mcsm add <url> <key>    
/mcsm 添加 <url> <key>
# 添加需要管理的服务器实例
/mcsm addServerInstances <name> <uuid> <daemonUUid>    
/mcsm 添加服务器实例 <name> <uuid> <daemonUUid> 
# 向实例发送命令
/mcsm command <name> <command>    
/mcsm cmd <name> <command>
/mcsm 命令 <name> <command>
# 向实例创建计划任务
/mcsm createTasks <name> <tasksName> <count> <time> <command>    
/mcsm ct <name> <tasksName> <count> <time> <command>
/mcsm 创建任务 <name> <tasksName> <count> <time> <command>
# 获取守护进程列表
/mcsm daemonList    
/mcsm 守护进程列表
# 删除需要管理的服务器实例
/mcsm deleteServerInstances <name>
/mcsm 删除服务器实例 <name>
# 向实例删除计划任务
/mcsm deleteTasks <name> <tasksName>    
/mcsm dt <name> <tasksName>
/mcsm 删除任务 <name> <tasksName>
# 终止实例
/mcsm kill <name>
/mcsm 终止 <name>
# 获取列表
/mcsm list
/mcsm 列表
# 重启实例
/mcsm restart <name>
/mcsm 重启 <name>
# 启动实例
/mcsm start <name>
/mcsm 启动 <name>
# 停止实例
/mcsm stop <name>
/mcsm 停止 <name>
```

