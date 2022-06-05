<div align="center">

[![](https://img.shields.io/github/v/release/limbang/mirai-console-mcsm-plugin?include_prereleases)](https://github.com/limbang/mirai-console-mcsm-plugin/releases)
![](https://img.shields.io/github/downloads/limbang/mirai-console-mcsm-plugin/total)
[![](https://img.shields.io/github/license/limbang/mirai-console-mcsm-plugin)](https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE)
[![](https://img.shields.io/badge/mirai-2.11.1-69c1b9)](https://github.com/mamoe/mirai)

本项目是基于 Mirai Console 编写的插件
<p>用于控制 <a href = "https://github.com/MCSManager/MCSManager">MCSM</a> api</p>
</div>

## 群里关键字触发
 - 向实例发送消息:`服务器名称 消息` 例如 :`as 你们好`(向 as 实例发送 你们好)
 - 获取服务器tps:`ftps 服务器名称` 例如 :`ftps as`(获取 as 实例的 TPS)
 - 通知所有服务器:`通知 消息` 例如 :`通知 服务器1分钟后维护,请下线等待！！！`(需要 `top.limbang.mcsm:command.server` 或以上权限)
## 指令

```shell
# 添加api管理
/mcsm addApi <url> <key>    
/mcsm 添加Api <url> <key>
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
# 重新命名服务器实例
/mcsm rename <name> <newName>
/mcsm 重命名服务器实例 <name> <newName>
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
# 获取指定实例的日志  regex=正则  index=正则的字字符串  maxSize=最大显示多少（从后往前获取）
/mcsm 获取日志消息 <name> <regex> <index> [maxSize]
/mcsm log <name> <regex> <index> [maxSize]
```

