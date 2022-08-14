<div align="center">

[![](https://img.shields.io/github/v/release/limbang/mirai-console-mcsm-plugin?include_prereleases)](https://github.com/limbang/mirai-console-mcsm-plugin/releases)
![](https://img.shields.io/github/downloads/limbang/mirai-console-mcsm-plugin/total)
[![](https://img.shields.io/github/license/limbang/mirai-console-mcsm-plugin)](https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE)
[![](https://img.shields.io/badge/mirai-2.11.1-69c1b9)](https://github.com/mamoe/mirai)

本项目是基于 Mirai Console 编写的插件
<p>用于控制 <a href = "https://github.com/MCSManager/MCSManager">MCSM</a> api</p>
</div>

可选前置插件[mirai-plugin-general-interface](https://github.com/limbang/mirai-plugin-general-interface)用来支持同步事件

## 群里关键字触发
 - 启动服务器（服务器假死会强制结束在启动）:`启动 服务器名称` 例如：`启动 as`(启动 as 服务器)
 - 向实例发送消息:`服务器名称 消息` 例如 :`as 你们好`(向 as 实例发送 你们好)
 - 获取服务器tps:`tps 服务器名称` 例如 :`tps as`(获取 as 实例的 TPS)
 - 通知所有服务器:`通知 消息` 例如 :`通知 服务器1分钟后维护,请下线等待！！！`(需要 `top.limbang.mcsm:*` 权限)
## 指令

```shell
  /mcsm addApi <url> <key>    # 添加api管理
  /mcsm addBlacklist <member>    # 添加到黑名单
  /mcsm 添加黑名单 <member>    # 添加到黑名单
  /mcsm add <name> <uuid> <daemonUuid>    # 添加需要管理的服务器实例
  /mcsm cmd <name> <command>    # 向实例发送命令
  /mcsm ct <name> <tasksName> <count> <time> <command>    # 向实例创建计划任务
  /mcsm daemonList    # 获取守护进程列表
  /mcsm delete <name>    # 删除需要管理的服务器实例
  /mcsm dt <name> <tasksName>    # 向实例删除计划任务
  /mcsm log <name> <regex> <index> [maxSize]    # 获取指定实例的日志
  /mcsm kill <name>    # 终止实例
  /mcsm list    # 获取列表
  /mcsm removeBlacklist <member>    # 移除到黑名单
  /mcsm 移除黑名单 <member>    # 移除到黑名单
  /mcsm rename <name> <newName>    # 重新命名服务器实例
  /mcsm restart <name>    # 重启实例
  /mcsm setTps <value>    # 设置tps功能启用
  /mcsm spark <name>    # 向实例发送spark命令
  /mcsm start <name>    # 启动实例
  /mcsm stop <name>    # 停止实例 
```

