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

import com.criteo.publisher.annotation.OpenForTesting
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@OpenForTesting
@JsonClass(generateAdapter = true)
data class MetricRequest internal constructor(
    val feedbacks: List<MetricRequestFeedback>,
    @Json(name = "wrapper_version")
    val wrapperVersion: String,
    @Json(name = "profile_id")
    val profileId: Int
) {
  constructor(
      metrics: Collection<Metric>,
      sdkVersion: String,
      profileId: Int
  ) : this(metrics.map { MetricRequestFeedback(it) }, sdkVersion, profileId)

  @OpenForTesting
  @JsonClass(generateAdapter = true)
  data class MetricRequestSlot(
      val impressionId: String,
      val zoneId: Int?,
      val cachedBidUsed: Boolean
  )

  @OpenForTesting
  @JsonClass(generateAdapter = true)
  data class MetricRequestFeedback(
      val slots: List<MetricRequestSlot>,
      val elapsed: Long?,
      @Json(name = "isTimeout")
      val isTimeout: Boolean,
      val cdbCallStartElapsed: Long,
      val cdbCallEndElapsed: Long?,
      val requestGroupId: String?
  ) {

    constructor(metric: Metric) : this(
        listOf(
            MetricRequestSlot(
                metric.impressionId,
                metric.zoneId,
                metric.isCachedBidUsed
            )
        ),
        calculateDifferenceSafely(metric.elapsedTimestamp, metric.cdbCallStartTimestamp),
        metric.isCdbCallTimeout,
        0L,
        calculateDifferenceSafely(metric.cdbCallEndTimestamp, metric.cdbCallStartTimestamp),
        metric.requestGroupId
    )

    companion object {
      private fun calculateDifferenceSafely(
          leftOperand: Long?,
          rightOperand: Long?
      ): Long? {
        return if (leftOperand == null || rightOperand == null) {
          null
        } else leftOperand - rightOperand
      }
    }
  }
}
