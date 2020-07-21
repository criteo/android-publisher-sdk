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
package com.criteo.publisher.csm

import com.criteo.publisher.SafeRunnable
import com.criteo.publisher.integration.Integration
import com.criteo.publisher.network.PubSdkApi
import com.criteo.publisher.util.BuildConfigWrapper

internal class MetricSendingTask(
    private val queue: MetricSendingQueue,
    private val api: PubSdkApi,
    private val buildConfigWrapper: BuildConfigWrapper
) : SafeRunnable() {

  override fun runSafely() {
    val metrics: Collection<Metric> = queue.poll(buildConfigWrapper.csmBatchSize)
    if (metrics.isEmpty()) {
      return
    }

    val metricsToRollback = metrics.toMutableList()
    try {
      val metricPerRequests = createRequests(metrics)
      metricPerRequests.forEach {
        api.postCsm(it.key)
        metricsToRollback.removeAll(it.value)
      }
    } finally {
      if (metricsToRollback.isNotEmpty()) {
        rollback(metricsToRollback)
      }
    }
  }

  private fun createRequests(metrics: Collection<Metric>): Map<MetricRequest, Collection<Metric>> {
    val sdkVersion = buildConfigWrapper.sdkVersion

    val metricsPerProfile = metrics.groupBy {
      it.profileId ?: Integration.FALLBACK.profileId
    }

    return metricsPerProfile.mapKeys {
      MetricRequest.create(
          it.value,
          sdkVersion,
          it.key
      )
    }
  }

  private fun rollback(metrics: Collection<Metric>) {
    metrics.forEach {
      queue.offer(it)
    }
  }

}