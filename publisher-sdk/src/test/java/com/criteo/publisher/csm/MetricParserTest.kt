package com.criteo.publisher.csm

import com.criteo.publisher.mock.MockedDependenciesRule
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import java.io.*
import javax.inject.Inject

class MetricParserTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var parser: MetricParser

  @Test
  fun read_GivenEmptyInputStream_ThrowEOF() {
     assertThatCode {
       parser.read("".toInputStream())
     }.isInstanceOf(EOFException::class.java)
  }

  @Test
  fun read_GivenIllFormedJson_ThrowIOException() {
    assertThatCode {
      parser.read("{".toInputStream())
    }.isInstanceOf(IOException::class.java)
  }

  @Test
  fun read_GivenJsonWithoutImpressionId_ThrowIOException() {
    assertThatCode {
      parser.read("{}".toInputStream())
    }.isInstanceOf(IOException::class.java)
  }

  @Test
  fun read_GivenJsonNotMatchingMetric_IgnoreThem() {
    val metric = parser.read("""{
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

    val json = parser.writeIntoString(expectedMetric)
    val readMetric = parser.read(json.toInputStream())

    assertThat(readMetric).isEqualTo(expectedMetric)
  }

  @Test
  fun read_GivenStream_DoNotCloseIt() {
    val stream = spy("""{"impressionId": "id"}""".toInputStream())

    parser.read(stream)

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
        .setReadyToSend(true)
        .build()

    val json = parser.writeIntoString(metric)

    assertThat(json).isEqualToIgnoringWhitespace("""{
      "cdbCallStartTimestamp": 42,
      "cdbCallEndTimestamp": 1337,
      "cdbCallTimeout": true,
      "cachedBidUsed": true,
      "elapsedTimestamp": 2,
      "impressionId": "impId",
      "readyToSend": true
    }""".trimIndent())
  }

  @Test
  fun write_GivenEmptyObject_ReturnEmptyJson() {
    val metric = Metric.builder("impId").build()

    val json = parser.writeIntoString(metric)

    assertThat(json).isEqualToIgnoringWhitespace("""{
      "cdbCallTimeout": false,
      "cachedBidUsed": false,
      "impressionId": "impId",
      "readyToSend": false
    }""".trimIndent())
  }

  @Test
  fun write_GivenStreamThatThrowsWhenWriting_ThrowIoException() {
    val metric = Metric.builder("id").build()
    val stream = mock<OutputStream> {
      on { write(any<Int>()) } doThrow(IOException::class)
      on { write(any<ByteArray>()) } doThrow(IOException::class)
      on { write(any(), any(), any()) } doThrow(IOException::class)
      on { flush() } doThrow(IOException::class)
    }

    assertThatCode {
      parser.write(metric, stream)
    }.isInstanceOf(IOException::class.java)
  }

  @Test
  fun write_GivenStream_DoNotCloseIt() {
    val metric = Metric.builder("id").build()
    val stream = mock<OutputStream>()

    parser.write(metric, stream)

    verify(stream, never()).close()
  }

  private fun String.toInputStream(): InputStream {
    return ByteArrayInputStream(toByteArray(Charsets.UTF_8))
  }

  private fun MetricParser.writeIntoString(metric: Metric): String {
    with(ByteArrayOutputStream()) {
      this@writeIntoString.write(metric, this)
      return String(toByteArray(), Charsets.UTF_8)
    }
  }

}