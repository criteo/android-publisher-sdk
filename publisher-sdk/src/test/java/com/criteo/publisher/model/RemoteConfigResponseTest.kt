package com.criteo.publisher.model

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.JsonSerializer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.inject.Inject

class RemoteConfigResponseTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  @Test
  fun read_GivenEmptyJson_ReturnEmptyObject() {
    val json = """{
    }""".trimIndent()

    val response = readFromString(json)

    assertThat(response.killSwitch).isNull()
    assertThat(response.androidDisplayUrlMacro).isNull()
    assertThat(response.androidAdTagUrlMode).isNull()
    assertThat(response.androidAdTagDataMacro).isNull()
    assertThat(response.androidAdTagDataMode).isNull()
  }

  @Test
  fun read_GivenFullJson_ReturnFullObject() {
    val json = """{
      "killSwitch": true,
      "AndroidDisplayUrlMacro": "%%macroUrl%%",
      "AndroidAdTagUrlMode": "<html />",
      "AndroidAdTagDataMacro": "%%macroData%%",
      "AndroidAdTagDataMode": "<body />"
    }""".trimIndent()

    val response = readFromString(json)

    assertThat(response.killSwitch).isTrue()
    assertThat(response.androidDisplayUrlMacro).isEqualTo("%%macroUrl%%")
    assertThat(response.androidAdTagUrlMode).isEqualTo("<html />")
    assertThat(response.androidAdTagDataMacro).isEqualTo("%%macroData%%")
    assertThat(response.androidAdTagDataMode).isEqualTo("<body />")
  }

  @Test
  fun read_GivenBooleanWithNumber_ThrowIOException() {
    val json = """{
      "killSwitch": 1
    }""".trimIndent()

    assertThatCode {
      readFromString(json)
    }.isInstanceOf(IOException::class.java)
  }

  @Test
  fun read_GivenBooleanWithTrueString_ReturnTrue() {
    val json = """{
      "killSwitch": "true"
    }""".trimIndent()

    val response = readFromString(json)

    assertThat(response.killSwitch).isTrue()
  }

  @Test
  fun read_GivenBooleanWithNotTheTrueString_ReturnFalse() {
    val json = """{
      "killSwitch": "anything"
    }""".trimIndent()

    val response = readFromString(json)

    assertThat(response.killSwitch).isFalse()
  }

  @Test
  fun read_GivenIllFormedJson_ThrowIOException() {
    assertThatCode {
      readFromString("{")
    }.isInstanceOf(IOException::class.java)
  }

  @Test
  fun read_GivenEmptyString_ThrowIOException() {
    assertThatCode {
      readFromString("")
    }.isInstanceOf(IOException::class.java)
  }

  private fun readFromString(json: String): RemoteConfigResponse {
    return ByteArrayInputStream(json.toByteArray(Charsets.UTF_8)).use {
      jsonSerializer.read(RemoteConfigResponse::class.java, it)
    }
  }

}