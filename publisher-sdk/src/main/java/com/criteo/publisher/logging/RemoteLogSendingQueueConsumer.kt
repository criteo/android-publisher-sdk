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
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.csm.ConcurrentSendingQueue
import com.criteo.publisher.network.PubSdkApi
import com.criteo.publisher.util.AdvertisingInfo
import com.criteo.publisher.util.BuildConfigWrapper
import java.util.concurrent.Executor

@OpenForTesting
internal class RemoteLogSendingQueueConsumer(
    private val sendingQueue: ConcurrentSendingQueue<RemoteLogRecords>,
    private val api: PubSdkApi,
    private val buildConfigWrapper: BuildConfigWrapper,
    private val advertisingInfo: AdvertisingInfo,
    private val executor: Executor
) {
  fun sendRemoteLogBatch() {
    executor.execute(RemoteLogSendingTask(sendingQueue, api, buildConfigWrapper, advertisingInfo))
  }

  class RemoteLogSendingTask(
      private val sendingQueue: ConcurrentSendingQueue<RemoteLogRecords>,
      private val api: PubSdkApi,
      private val buildConfigWrapper: BuildConfigWrapper,
      private val advertisingInfo: AdvertisingInfo
  ) : SafeRunnable() {
    override fun runSafely() {
      val remoteLogRecords = sendingQueue.poll(buildConfigWrapper.remoteLogBatchSize)
      if (remoteLogRecords.isEmpty()) {
        return
      }

      var isSuccessful = false

      try {
        injectMissingDeviceId(remoteLogRecords)
        api.postLogs(remoteLogRecords)
        isSuccessful = true
      } finally {
        if (!isSuccessful) {
          remoteLogRecords.forEach {
            sendingQueue.offer(it)
          }
        }
      }
    }

    private fun injectMissingDeviceId(remoteLogRecords: List<RemoteLogRecords>) {
      // When called a first time on the main thread, the AdvertiserInfo can return null
      // In this context, we're in a worker thread. So we can inject the deviceId when it is missing.
      advertisingInfo.advertisingId?.let { deviceId ->
        remoteLogRecords.forEach {
          if (it.context.deviceId == null) {
            it.context.deviceId = deviceId
          }
        }
      }
    }
  }
}
