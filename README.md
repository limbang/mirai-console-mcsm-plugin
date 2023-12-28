<div align="center">

[![](https://img.shields.io/github/v/release/limbang/mirai-console-mcsm-plugin?include_prereleases)](https://github.com/limbang/mirai-console-mcsm-plugin/releases)
![](https://img.shields.io/github/downloads/limbang/mirai-console-mcsm-plugin/total)
[![](https://img.shields.io/github/license/limbang/mirai-console-mcsm-plugin)](https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE)
[![](https://img.shields.io/badge/mirai-2.16.0-69c1b9)](https://github.com/mamoe/mirai)

本项目是基于 Mirai Console 编写的插件
<p>用于控制 <a href = "https://github.com/MCSManager/MCSManager">MCSM</a> api</p>

支持添加多个 MCSManager,每个群独立配置实例,共享 MCSManager
</div>

可选前置插件[mirai-plugin-general-interface](https://github.com/limbang/mirai-plugin-general-interface)用来支持同步事件

## 群里关键字触发
 - 启动服务器（服务器假死会强制结束在启动）:`启动 服务器名称` 例如：`启动 as`(启动 as 服务器)
 - 向实例发送消息:`服务器名称 消息` 例如 :`as 你们好`(向 as 实例发送 你们好)
 - 获取服务器tps:`tps 服务器名称` 例如 :`tps as`(获取 as 实例的 TPS)
 - 通知所有服务器:`通知 消息` 例如 :`通知 服务器1分钟后维护,请下线等待！！！`(需要 `top.limbang.mcsm:*` 权限)
 - 获取指定服务器分析日志:`分析日志 服务器名称` (需要 `top.limbang.mcsm:*` 权限)
 - 获取指定服务器崩溃报告:`崩溃报告 服务器名称` (需要 `top.limbang.mcsm:*` 权限)
## 指令

> 第一步添加需要管理的MCSMAPI接口和密钥（在个人资料里面）

`/mcsm addmcsm <MCSM 名称> <MCSM URL> <MCSM KEY>    # 添加需要管理的 MCSManager`
如:`/mcsm addmcsm limbang的mcsm https://mcsm.limbang.top 1f74a84b474a4ddbb3151cd750ae8c0a`

`/mcsm deletemcsm <MCSM 名称>    # 删除 MCSManager`

> 第二步查看所有MCSM列表

`/mcsm listmcsm    # 查看所有MCSM列表`

如获取到的信息如下：

```
所有列表如下:
  MCSM名称[limbang的mcsm]:
    守护进程ID[ad6f6b]:
      实例名称[服务器:521]
      实例名称[服务器:522]
      实例名称[服务器:523]
      实例名称[服务器:524]
```

> 第三步编辑需要管理的服务器实例

```shell
/mcsm add <昵称> <MCSM名称> <守护进程UUID> <实例名称>    # 添加本群需要管理的服务器实例,参数参考可以发送:/mcsm listmcsm
/mcsm delete <name>    # 删除本群的服务器实例
/mcsm rename <name> <newName>    # 重新命名服务器实例
```
如:`/mcsm add et2 limbang的mcsm ad6f6b 服务器:521`

> 实例命令

```shell
/mcsm list    # 获取本群的实例列表

/mcsm start <name>    # 启动实例
/mcsm stop <name>    # 停止实例
/mcsm restart <name>    # 重启实例
/mcsm kill <name>    # 终止实例

/mcsm ct <name> <tasksName> <count> <time> <command>    # 向实例创建计划任务
/mcsm dt <name> <tasksName>    # 向实例删除计划任务

/mcsm cmd <name> <command>    # 向实例发送命令
/mcsm log <name> <regex> <index> [maxSize]    # 获取指定实例的日志
```

> 配置命令(每个群独立)

```shell
# 添加黑名单后在群里发送启动命令将不理会
/config addBlacklist <member>    # 添加黑名单
/config removeBlacklist <member>    # 移除黑名单
/config setForceStart <value>    # 设置强制启动功能启用
/config setNotice <value>    # 设置通知消息功能启用
/config setSendMessage <value>    # 设置发送消息到服务器功能启用
/config setTps <value>    # 设置tps功能启用
```

> Mod命令

```shell
/mod spark <name>    # 向实例发送spark命令
```

## 鸣谢

> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) 是一个在各个方面都最大程度地提高开发人员的生产力的 IDE，适用于 JVM 平台语言。

特别感谢 [JetBrains](https://www.jetbrains.com/?from=mirai-console-mcsm-plugin) 为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai-console-mcsm-plugin) 等 IDE 的授权  
[<img src="docs/img/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/?from=mirai-console-mcsm-plugin)
