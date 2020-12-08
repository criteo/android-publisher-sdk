/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.logging

import com.criteo.publisher.SafeRunnable
import com.criteo.publisher.csm.ConcurrentSendingQueue
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogLevel.Companion.fromAndroidLogLevel
import com.criteo.publisher.model.Config
import java.util.concurrent.Executor

internal class RemoteHandler(
    private val remoteLogRecordsFactory: RemoteLogRecordsFactory,
    private val sendingQueue: ConcurrentSendingQueue<RemoteLogRecords>,
    private val config: Config,
    private val executor: Executor
) : LogHandler {
  override fun log(tag: String, logMessage: LogMessage) {
    fromAndroidLogLevel(logMessage.level)?.takeIf { it >= config.remoteLogLevel } ?: return

    remoteLogRecordsFactory.createLogRecords(logMessage)?.let {
      // Asynchronously post log to avoid doing IO on the current thread
      executor.execute(object : SafeRunnable() {
        override fun runSafely() {
          sendingQueue.offer(it)
        }
      })
    }
  }
}
