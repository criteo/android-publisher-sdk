package com.criteo.publisher.model.nativeads

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.JsonSerializer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URI
import javax.inject.Inject

class NativeImpressionPixelTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  @Test
  fun fromJson_GivenEmptyUrl_ThrowAnException() {
    val json = """{"url": ""}"""

    assertThatCode {
      read(json)
    }.isInstanceOf(IOException::class.java)
  }

  @Test
  fun fromJson_GivenMalformedUrl_ThrowAnException() {
    val json = """{"url": "not a url"}"""

    assertThatCode {
      read(json)
    }.isInstanceOf(IOException::class.java)
  }

  @Test
  fun fromJson_GivenValidUrl_ReadIt() {
    val json = """{"url": "http://criteo.com"}"""

    val pixel = read(json)

    assertThat(pixel.url).isEqualTo(URI.create("http://criteo.com").toURL())
  }

  private fun read(json: String): NativeImpressionPixel {
    return jsonSerializer.read(NativeImpressionPixel::class.java, ByteArrayInputStream(json.toByteArray()))
  }

}