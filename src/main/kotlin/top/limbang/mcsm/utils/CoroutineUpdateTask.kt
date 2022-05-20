/*
 * Copyright 2022-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

/*
 * Copyright 2022-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

/*
 * Copyright 2022-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.utils

import kotlinx.coroutines.*
import top.limbang.mcsm.MCSM


class CoroutineUpdateTask {

    private var scope: CoroutineScope? = null

    fun scheduleUpdate(interval: Long, action: suspend () -> Unit) {
        cancel()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            while (isActive) {
                try {
                    action()
                } catch (e: Exception) {
                    MCSM.logger.error(e)
                    if (e is CancellationException) throw e
                }
                delay(interval)
            }
        }

        this.scope = scope
    }

    fun cancel() {
        scope?.cancel()
        scope = null
    }

}