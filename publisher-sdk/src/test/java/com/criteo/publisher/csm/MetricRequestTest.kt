package com.criteo.publisher.csm

import com.criteo.publisher.Util.JsonSerializer
import com.criteo.publisher.Util.writeIntoString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MetricRequestTest {

  private val serializer = JsonSerializer()

  @Test
  fun create_GivenEmptyMetric_ReturnRequestFullOfNulls() {
    val metric = Metric.builder("id")
        .build()

    val request = MetricRequest.create(metric, "1.2.3", 456)

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

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedJson())
  }

  @Test
  fun create_GivenMetricRepresentingNetworkError_ReturnRequestFullOfNulls() {
    val metric = Metric.builder("id")
        .setCdbCallStartTimestamp(42L)
        .build()

    val request = MetricRequest.create(metric, "1.2.3", 456)

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

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedJson())
  }

  @Test
  fun create_GivenMetricRepresentingTimeout_ReturnRequestWithTimeoutFlag() {
    val metric = Metric.builder("id")
        .setCdbCallStartTimestamp(42L)
        .setCdbCallTimeout(true)
        .build()

    val request = MetricRequest.create(metric, "1.2.3", 456)

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

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedJson(
        isTimeout = true))
  }

  @Test
  fun create_GivenMetricRepresentingNoBid_ReturnRequestWithCdbCallEnd() {
    val metric = Metric.builder("id")
        .setCdbCallStartTimestamp(42L)
        .setCdbCallEndTimestamp(1337L)
        .build()

    val request = MetricRequest.create(metric, "1.2.3", 456)

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

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedJson(
        cdbCallEndElapsed = 1337 - 42))
  }

  @Test
  fun create_GivenMetricRepresentingExpiredBid_ReturnRequestWithCdbCallEndAndImpressionId() {
    val metric = Metric.builder("impId")
        .setCdbCallStartTimestamp(1L)
        .setCdbCallEndTimestamp(43L)
        .setCachedBidUsed(true)
        .build()

    val request = MetricRequest.create(metric, "1.2.3", 456)

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

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedJson(
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

    val request = MetricRequest.create(metric, "3.2.1", 654)

    assertThat(request.feedbacks).hasSize(1).allSatisfy {
      assertThat(it.slots).hasSize(1).allSatisfy {
        assertThat(it.impressionId).isEqualTo("impId")
        assertThat(it.cachedBidUsed).isTrue()
      }
      assertThat(it.elapsed).isEqualTo(1338 - 1)
      assertThat(it.isTimeout).isFalse()
      assertThat(it.cdbCallStartElapsed).isEqualTo(0L)
      assertThat(it.cdbCallEndElapsed).isEqualTo(43 - 1)
    }
    assertThat(request.wrapperVersion).isEqualTo("3.2.1")
    assertThat(request.profileId).isEqualTo(654)

    assertThat(serializer.writeIntoString(request)).isEqualToIgnoringWhitespace(expectedJson(
        impressionId = "impId",
        cdbCallEndElapsed = 43 - 1,
        cachedBidUsed = true,
        elapsed = 1338 - 1,
        wrapperVersion = "3.2.1",
        profileId = 654))
  }

  private fun expectedJson(
      impressionId: String = "id",
      cachedBidUsed: Boolean = false,
      isTimeout: Boolean = false,
      cdbCallEndElapsed: Long? = null,
      elapsed: Long? = null,
      wrapperVersion: String = "1.2.3",
      profileId: Int = 456
  ): String {
    return """
        {
          "feedbacks": [{
            "slots": [{
                "impressionId": "$impressionId",
                "cachedBidUsed": $cachedBidUsed
            }],
            ${elapsed?.let { "\"elapsed\": $it," } ?: ""}
            "isTimeout": $isTimeout,
            "cdbCallStartElapsed": 0
            ${cdbCallEndElapsed?.let { ",\"cdbCallEndElapsed\": $it" } ?: ""}
          }],
          "wrapper_version": "$wrapperVersion",
          "profile_id": $profileId
        }
      """.trimIndent()
  }

}