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

import android.os.Build
import androidx.annotation.RequiresApi
import com.criteo.publisher.DependencyProvider

object MetricHelper {

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  @JvmStatic
  fun cleanState(dependencyProvider: DependencyProvider) {
    // Empty metric repository
    val repository = dependencyProvider.provideMetricRepository()
    for (metric in repository.allStoredMetrics) {
      repository.moveById(metric.impressionId) { true }
    }

    // Empty sending queue
    ConcurrentSendingQueueHelper.emptyQueue(dependencyProvider.provideMetricSendingQueue())
  }

  val MetricRequest.internalProfileId: Int
    get() = profileId

  val MetricRequest.internalFeedbacks: List<MetricRequest.MetricRequestFeedback>
    get() = feedbacks
}
