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
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class MetricTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  @Test
  fun read_GivenJsonWithoutImpressionId_ThrowIOException() {
    assertThatCode {
      jsonSerializer.read(Metric::class.java, "{}".toInputStream())
    }.isInstanceOf(IOException::class.java)
  }

  @Test
  fun read_GivenJsonNotMatchingMetric_IgnoreThem() {
    val metric = jsonSerializer.read(Metric::class.java, """{
      "impressionId": "id",
      "unknownKey": {}
    }""".trimIndent().toInputStream())

    assertThat(metric).isEqualTo(Metric.builder("id").build())
  }

  @Test
  fun read_GivenJsonFromWrite_ReturnEqualMetric() {
    val expectedMetric = Metric.builder("impId")
        .setCdbCallStartTimestamp(42L)
        .build()

    val json = jsonSerializer.writeIntoString(expectedMetric)
    val readMetric = jsonSerializer.read(Metric::class.java, json.toInputStream())

    assertThat(readMetric).isEqualTo(expectedMetric)
  }

  @Test
  fun read_GivenStream_DoNotCloseIt() {
    val stream = spy("""{"impressionId": "id"}""".toInputStream())

    jsonSerializer.read(Metric::class.java, stream)

    verify(stream, never()).close()
  }

  @Test
  fun write_GivenObjectFullySet_ReturnJson() {
    val metric = Metric.builder("impId")
        .setCdbCallStartTimestamp(42L)
        .setCdbCallEndTimestamp(1337L)
        .setCdbCallTimeout(true)
        .setCachedBidUsed(true)
        .setElapsedTimestamp(2L)
        .setRequestGroupId("requestId")
        .setReadyToSend(true)
        .setProfileId(3)
        .setZoneId(4)
        .build()

    val json = jsonSerializer.writeIntoString(metric)

    assertThat(json).isEqualToIgnoringWhitespace(
        """{
      "cdbCallStartTimestamp": 42,
      "cdbCallEndTimestamp": 1337,
      "cdbCallTimeout": true,
      "cachedBidUsed": true,
      "elapsedTimestamp": 2,
      "impressionId": "impId",
      "requestGroupId": "requestId",
      "zoneId": 4,
      "profileId": 3,
      "readyToSend": true
    }""".trimIndent())
  }

  @Test
  fun write_GivenEmptyObject_ReturnEmptyJson() {
    val metric = Metric.builder("impId").build()

    val json = jsonSerializer.writeIntoString(metric)

    assertThat(json).isEqualToIgnoringWhitespace("""{
      "cdbCallTimeout": false,
      "cachedBidUsed": false,
      "impressionId": "impId",
      "readyToSend": false
    }""".trimIndent())
  }

  private fun String.toInputStream(): InputStream {
    return ByteArrayInputStream(toByteArray(Charsets.UTF_8))
  }
}
