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

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.JsonSerializer
import com.criteo.publisher.util.writeIntoString
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectAssert
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class MetricRequestTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var serializer: JsonSerializer

  @Test
  fun create_GivenNoMetric_ReturnEmptyRequest() {
    val request = MetricRequest.create(emptyList(), "1.2.3", 456)

    assertThat(request.feedbacks).isEmpty()
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedEmptyJson())
  }

  @Test
  fun create_GivenMultipleMetric_ReturnRequestWithManyFeedbacks() {
    val metric1 = Metric.builder("id1")
        .build()

    val metric2 = Metric.builder("id2")
        .setRequestGroupId("requestId")
        .setCdbCallStartTimestamp(1L)
        .setCdbCallEndTimestamp(43L)
        .setCachedBidUsed(true)
        .setElapsedTimestamp(1338L)
        .setZoneId(1339)
        .build()

    val request = MetricRequest.create(listOf(metric1, metric2), "1.2.3", 456)

    assertThat(request.feedbacks).hasSize(2)
    assertThat(request.feedbacks[0]).matchEmptyMetric("id1")
    assertThat(request.feedbacks[1]).matchConsumedBidMetric("id2")
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedMultipleJson(
        listOf(
            feedbackJson(impressionId = "id1", requestGroupId = null),
            feedbackJson(
                impressionId = "id2",
                cdbCallEndElapsed = 43 - 1,
                cachedBidUsed = true,
                elapsed = 1338 - 1,
                zoneId = 1339
            )
        )
    ))
  }

  @Test
  fun create_GivenEmptyMetric_ReturnRequestFullOfNulls() {
    val metric = Metric.builder("id")
        .build()

    val request = MetricRequest.create(listOf(metric), "1.2.3", 456)

    assertThat(request.feedbacks).hasSize(1).allSatisfy {
      assertThat(it).matchEmptyMetric("id")
    }
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    val actualJson = serializer.writeIntoString(request)
    val expectedJson = expectedSingleJson(requestGroupId = null)
    assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson)
  }

  @Test
  fun create_GivenMetricRepresentingNetworkError_ReturnRequestFullOfNulls() {
    val metric = Metric.builder("id")
        .setRequestGroupId("requestId")
        .setCdbCallStartTimestamp(42L)
        .build()

    val request = MetricRequest.create(listOf(metric), "1.2.3", 456)

    assertThat(request.feedbacks).hasSize(1).allSatisfy {
      assertThat(it.slots).hasSize(1).allSatisfy {
        assertThat(it.impressionId).isEqualTo("id")
        assertThat(it.cachedBidUsed).isFalse()
      }
      assertThat(it.elapsed).isNull()
      assertThat(it.isTimeout).isFalse()
      assertThat(it.cdbCallStartElapsed).isEqualTo(0L)
      assertThat(it.cdbCallEndElapsed).isNull()
      assertThat(it.requestGroupId).isEqualTo("requestId")
    }
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedSingleJson())
  }

  @Test
  fun create_GivenMetricRepresentingTimeout_ReturnRequestWithTimeoutFlag() {
    val metric = Metric.builder("id")
        .setRequestGroupId("requestId")
        .setCdbCallStartTimestamp(42L)
        .setCdbCallTimeout(true)
        .build()

    val request = MetricRequest.create(listOf(metric), "1.2.3", 456)

    assertThat(request.feedbacks).hasSize(1).allSatisfy {
      assertThat(it.slots).hasSize(1).allSatisfy {
        assertThat(it.impressionId).isEqualTo("id")
        assertThat(it.cachedBidUsed).isFalse()
      }
      assertThat(it.elapsed).isNull()
      assertThat(it.isTimeout).isTrue()
      assertThat(it.cdbCallStartElapsed).isEqualTo(0L)
      assertThat(it.cdbCallEndElapsed).isNull()
      assertThat(it.requestGroupId).isEqualTo("requestId")
    }
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedSingleJson(
        isTimeout = true))
  }

  @Test
  fun create_GivenMetricRepresentingNoBid_ReturnRequestWithCdbCallEnd() {
    val metric = Metric.builder("id")
        .setRequestGroupId("requestId")
        .setCdbCallStartTimestamp(42L)
        .setCdbCallEndTimestamp(1337L)
        .build()

    val request = MetricRequest.create(listOf(metric), "1.2.3", 456)

    assertThat(request.feedbacks).hasSize(1).allSatisfy {
      assertThat(it.slots).hasSize(1).allSatisfy {
        assertThat(it.impressionId).isEqualTo("id")
        assertThat(it.cachedBidUsed).isFalse()
      }
      assertThat(it.elapsed).isNull()
      assertThat(it.isTimeout).isFalse()
      assertThat(it.cdbCallStartElapsed).isEqualTo(0L)
      assertThat(it.cdbCallEndElapsed).isEqualTo(1337 - 42)
      assertThat(it.requestGroupId).isEqualTo("requestId")
    }
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedSingleJson(
        cdbCallEndElapsed = 1337 - 42))
  }

  @Test
  fun create_GivenMetricRepresentingExpiredBid_ReturnRequestWithCdbCallEndAndImpressionId() {
    val metric = Metric.builder("impId")
        .setRequestGroupId("requestId")
        .setCdbCallStartTimestamp(1L)
        .setCdbCallEndTimestamp(43L)
        .setCachedBidUsed(true)
        .build()

    val request = MetricRequest.create(listOf(metric), "1.2.3", 456)

    assertThat(request.feedbacks).hasSize(1).allSatisfy {
      assertThat(it.slots).hasSize(1).allSatisfy {
        assertThat(it.impressionId).isEqualTo("impId")
        assertThat(it.cachedBidUsed).isTrue()
      }
      assertThat(it.elapsed).isNull()
      assertThat(it.isTimeout).isFalse()
      assertThat(it.cdbCallStartElapsed).isEqualTo(0L)
      assertThat(it.cdbCallEndElapsed).isEqualTo(43 - 1)
      assertThat(it.requestGroupId).isEqualTo("requestId")
    }
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedSingleJson(
        impressionId = "impId",
        cdbCallEndElapsed = 43 - 1,
        cachedBidUsed = true))
  }

  @Test
  fun create_GivenMetricRepresentingConsumedBid_ReturnRequestNonNull() {
    val metric = Metric.builder("impId")
        .setRequestGroupId("requestId")
        .setCdbCallStartTimestamp(1L)
        .setCdbCallEndTimestamp(43L)
        .setCachedBidUsed(true)
        .setElapsedTimestamp(1338L)
        .setZoneId(1339)
        .build()

    val request = MetricRequest.create(listOf(metric), "3.2.1", 654)

    assertThat(request.feedbacks).hasSize(1).allSatisfy {
      assertThat(it).matchConsumedBidMetric("impId")
    }
    assertThat(request.wrapperVersion).isEqualTo("3.2.1")
    assertThat(request.profileId).isEqualTo(654)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(
        expectedSingleJson(
            impressionId = "impId",
            cdbCallEndElapsed = 43 - 1,
            cachedBidUsed = true,
            elapsed = 1338 - 1,
            wrapperVersion = "3.2.1",
            profileId = 654,
            zoneId = 1339
        )
    )
  }

  private fun ObjectAssert<MetricRequest.MetricRequestFeedback>.matchEmptyMetric(
      impressionId: String
  ) {
    satisfies {
      assertThat(it.slots).hasSize(1).allSatisfy {
        assertThat(it.impressionId).isEqualTo(impressionId)
        assertThat(it.cachedBidUsed).isFalse()
      }
      assertThat(it.elapsed).isNull()
      assertThat(it.isTimeout).isFalse()
      assertThat(it.cdbCallStartElapsed).isEqualTo(0L)
      assertThat(it.cdbCallEndElapsed).isNull()
      assertThat(it.cdbCallEndElapsed).isNull()
      assertThat(it.requestGroupId).isNull()
    }
  }

  @Suppress("LongParameterList")
  private fun ObjectAssert<MetricRequest.MetricRequestFeedback>.matchConsumedBidMetric(
      impressionId: String,
      requestGroupId: String = "requestId",
      cdbCallStartTimestamp: Long = 1L,
      cdbCallEndTimestamp: Long = 43L,
      elapsedTimestamp: Long = 1338L,
      zoneId: Int = 1339
  ) {
    satisfies {
      assertThat(it.slots).hasSize(1).allSatisfy {
        assertThat(it.impressionId).isEqualTo(impressionId)
        assertThat(it.zoneId).isEqualTo(zoneId)
        assertThat(it.cachedBidUsed).isTrue()
      }
      assertThat(it.elapsed).isEqualTo(elapsedTimestamp - cdbCallStartTimestamp)
      assertThat(it.isTimeout).isFalse()
      assertThat(it.cdbCallStartElapsed).isEqualTo(0L)
      assertThat(it.cdbCallEndElapsed).isEqualTo(cdbCallEndTimestamp - cdbCallStartTimestamp)
      assertThat(it.requestGroupId).isEqualTo(requestGroupId)
    }
  }

  private fun expectedEmptyJson(wrapperVersion: String = "1.2.3", profileId: Int = 456) =
      expectedMultipleJson(wrapperVersion = wrapperVersion, profileId = profileId)

  @Suppress("LongParameterList")
  private fun expectedSingleJson(
      impressionId: String = "id",
      requestGroupId: String? = "requestId",
      cachedBidUsed: Boolean = false,
      isTimeout: Boolean = false,
      cdbCallEndElapsed: Long? = null,
      elapsed: Long? = null,
      wrapperVersion: String = "1.2.3",
      profileId: Int = 456,
      zoneId: Int? = null
  ): String {
    val feedbackJson = feedbackJson(
        impressionId,
        requestGroupId,
        cachedBidUsed,
        isTimeout,
        cdbCallEndElapsed,
        elapsed,
        zoneId
    )

    return expectedMultipleJson(listOf(feedbackJson), wrapperVersion, profileId)
  }

  private fun expectedMultipleJson(
      feedbackJsons: List<String> = listOf(),
      wrapperVersion: String = "1.2.3",
      profileId: Int = 456
  ): String {
    val feedbacks = feedbackJsons.joinToString(",")
    return """
        {
          "feedbacks": [$feedbacks],
          "wrapper_version": "$wrapperVersion",
          "profile_id": $profileId
        }
      """.trimIndent()
  }

  @Suppress("LongParameterList")
  private fun feedbackJson(
      impressionId: String = "id",
      requestGroupId: String? = "requestId",
      cachedBidUsed: Boolean = false,
      isTimeout: Boolean = false,
      cdbCallEndElapsed: Long? = null,
      elapsed: Long? = null,
      zoneId: Int? = null
  ): String {
    return """{
      "slots": [{
          "impressionId": "$impressionId",
          ${zoneId?.let { "\"zoneId\": $it," } ?: ""}
          "cachedBidUsed": $cachedBidUsed
      }],
      ${elapsed?.let { "\"elapsed\": $it," } ?: ""}
      "isTimeout": $isTimeout,
      "cdbCallStartElapsed": 0
      ${cdbCallEndElapsed?.let { ",\"cdbCallEndElapsed\": $it" } ?: ""}
      ${requestGroupId?.let { ",\"requestGroupId\": \"$it\"" } ?: ""}
    }""".trimIndent()
  }
}
