<div align="center">

[![](https://img.shields.io/github/v/release/limbang/mirai-console-mcsm-plugin?include_prereleases)](https://github.com/limbang/mirai-console-mcsm-plugin/releases)
![](https://img.shields.io/github/downloads/limbang/mirai-console-mcsm-plugin/total)
[![](https://img.shields.io/github/license/limbang/mirai-console-mcsm-plugin)](https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE)
[![](https://img.shields.io/badge/mirai-2.14.0-69c1b9)](https://github.com/mamoe/mirai)

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

> 第一步添加需要管理的MCSMAPI接口和密钥（在个人资料里面）

`/mcsm addApi <url> <key>`

> 第二步获取守护进程列表的信息

`/mcsm daemonList`

如获取到的信息如下：

`守护进程[1f74a84b474a4ddbb3151cd750ae8c0a],实例如下:
060755623f834e0e99b464705f77c560 -> 服务器:523（et2）`

> 第三步编辑需要管理的服务器实例

```shell
/mcsm add <name> <实例ID> <守护进程ID>  # 添加需要管理的服务器实例
/mcsm delete <name>    # 删除需要管理的服务器实例
/mcsm rename <name> <newName>    # 重新命名服务器实例
```
如:`/mcsm add et2 060755623f834e0e99b464705f77c560 1f74a84b474a4ddbb3151cd750ae8c0a`

> 管理实例的命令

```shell
/mcsm list    # 获取列表
/mcsm start <name>    # 启动实例
/mcsm stop <name>    # 停止实例
/mcsm restart <name>    # 重启实例
/mcsm kill <name>    # 终止实例
/mcsm ct <name> <tasksName> <count> <time> <command>    # 向实例创建计划任务
/mcsm dt <name> <tasksName>    # 向实例删除计划任务
```

> MC服务器的命令

```shell
# 添加黑名单后在群里发送启动命令将不理会
/mcsm addBlacklist <member> # 添加黑名单
/mcsm removeBlacklist <member> # 移除黑名单
/mcsm setForceStart <value>    # 设置群里强制启动功能启用
/mcsm setNotice <value>    # 设置群里通知消息功能启用
/mcsm setSendMessage <value>    # 设置群里发送消息到服务器功能启用
/mcsm setTps <value>    # 设置群里tps功能启用

/mcsm spark <name>    # 向实例发送spark命令
/mcsm cmd <name> <command>    # 向实例发送命令
/mcsm log <name> <regex> <index> [maxSize]    # 获取指定实例的日志
```

