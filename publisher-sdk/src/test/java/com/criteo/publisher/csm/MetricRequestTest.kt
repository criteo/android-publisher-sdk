package com.criteo.publisher.csm

import com.criteo.publisher.Util.JsonSerializer
import com.criteo.publisher.Util.writeIntoString
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectAssert
import org.junit.Test

class MetricRequestTest {

  private val serializer = JsonSerializer()

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
        .setCdbCallStartTimestamp(1L)
        .setCdbCallEndTimestamp(43L)
        .setCachedBidUsed(true)
        .setElapsedTimestamp(1338L)
        .build()

    val request = MetricRequest.create(listOf(metric1, metric2), "1.2.3", 456)

    assertThat(request.feedbacks).hasSize(2)
    assertThat(request.feedbacks[0]).matchEmptyMetric("id1")
    assertThat(request.feedbacks[1]).matchConsumedBidMetric("id2")
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedMultipleJson(
        listOf(
            feedbackJson(impressionId = "id1"),
            feedbackJson(
                impressionId = "id2",
                cdbCallEndElapsed = 43 - 1,
                cachedBidUsed = true,
                elapsed = 1338 - 1)
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

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedSingleJson())
  }

  @Test
  fun create_GivenMetricRepresentingNetworkError_ReturnRequestFullOfNulls() {
    val metric = Metric.builder("id")
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
    }
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedSingleJson())
  }

  @Test
  fun create_GivenMetricRepresentingTimeout_ReturnRequestWithTimeoutFlag() {
    val metric = Metric.builder("id")
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
    }
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedSingleJson(
        isTimeout = true))
  }

  @Test
  fun create_GivenMetricRepresentingNoBid_ReturnRequestWithCdbCallEnd() {
    val metric = Metric.builder("id")
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
    }
    assertThat(request.wrapperVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(456)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedSingleJson(
        cdbCallEndElapsed = 1337 - 42))
  }

  @Test
  fun create_GivenMetricRepresentingExpiredBid_ReturnRequestWithCdbCallEndAndImpressionId() {
    val metric = Metric.builder("impId")
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
        .setCdbCallStartTimestamp(1L)
        .setCdbCallEndTimestamp(43L)
        .setCachedBidUsed(true)
        .setElapsedTimestamp(1338L)
        .build()

    val request = MetricRequest.create(listOf(metric), "3.2.1", 654)

    assertThat(request.feedbacks).hasSize(1).allSatisfy {
      assertThat(it).matchConsumedBidMetric("impId")
    }
    assertThat(request.wrapperVersion).isEqualTo("3.2.1")
    assertThat(request.profileId).isEqualTo(654)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedSingleJson(
        impressionId = "impId",
        cdbCallEndElapsed = 43 - 1,
        cachedBidUsed = true,
        elapsed = 1338 - 1,
        wrapperVersion = "3.2.1",
        profileId = 654))
  }

  private fun ObjectAssert<MetricRequest.MetricRequestFeedback>.matchEmptyMetric(impressionId: String) {
    satisfies {
      assertThat(it.slots).hasSize(1).allSatisfy {
        assertThat(it.impressionId).isEqualTo(impressionId)
        assertThat(it.cachedBidUsed).isFalse()
      }
      assertThat(it.elapsed).isNull()
      assertThat(it.isTimeout).isFalse()
      assertThat(it.cdbCallStartElapsed).isEqualTo(0L)
      assertThat(it.cdbCallEndElapsed).isNull()
    }
  }

  private fun ObjectAssert<MetricRequest.MetricRequestFeedback>.matchConsumedBidMetric(
      impressionId: String,
      cdbCallStartTimestamp: Long = 1L,
      cdbCallEndTimestamp: Long = 43L,
      elapsedTimestamp: Long = 1338L
  ) {
    satisfies {
      assertThat(it.slots).hasSize(1).allSatisfy {
        assertThat(it.impressionId).isEqualTo(impressionId)
        assertThat(it.cachedBidUsed).isTrue()
      }
      assertThat(it.elapsed).isEqualTo(elapsedTimestamp - cdbCallStartTimestamp)
      assertThat(it.isTimeout).isFalse()
      assertThat(it.cdbCallStartElapsed).isEqualTo(0L)
      assertThat(it.cdbCallEndElapsed).isEqualTo(cdbCallEndTimestamp - cdbCallStartTimestamp)
    }
  }

  private fun expectedEmptyJson(wrapperVersion: String = "1.2.3", profileId: Int = 456)
      = expectedMultipleJson(wrapperVersion = wrapperVersion, profileId = profileId)

  private fun expectedSingleJson(
      impressionId: String = "id",
      cachedBidUsed: Boolean = false,
      isTimeout: Boolean = false,
      cdbCallEndElapsed: Long? = null,
      elapsed: Long? = null,
      wrapperVersion: String = "1.2.3",
      profileId: Int = 456
  ): String {
    val feedbackJson = feedbackJson(
        impressionId,
        cachedBidUsed,
        isTimeout,
        cdbCallEndElapsed,
        elapsed
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

  private fun feedbackJson(
      impressionId: String = "id",
      cachedBidUsed: Boolean = false,
      isTimeout: Boolean = false,
      cdbCallEndElapsed: Long? = null,
      elapsed: Long? = null
  ): String {
    return """{
      "slots": [{
          "impressionId": "$impressionId",
          "cachedBidUsed": $cachedBidUsed
      }],
      ${elapsed?.let { "\"elapsed\": $it," } ?: ""}
      "isTimeout": $isTimeout,
      "cdbCallStartElapsed": 0
      ${cdbCallEndElapsed?.let { ",\"cdbCallEndElapsed\": $it" } ?: ""}
    }""".trimIndent()
  }

}