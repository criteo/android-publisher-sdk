package com.criteo.publisher.model.nativeads

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.JsonSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.URI
import javax.inject.Inject

class NativePrivacyTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  @Test
  fun fromJson_GivenEmptyDataExceptUrl_ReturnsPrivacyWithEmptyData() {
    val json = """{
      |  "optoutClickUrl": "http://click.url",
      |  "optoutImageUrl": "http://image.url",
      |  "longLegalText": ""
      |}""".trimMargin()

    val privacy = read(json)

    assertThat(privacy.clickUrl).isEqualTo(URI.create("http://click.url"))
    assertThat(privacy.imageUrl).isEqualTo(URI.create("http://image.url").toURL())
    assertThat(privacy.legalText).isEqualTo("")
  }

  @Test
  fun fromJson_GivenSomeData_ReadThem() {
    val json = """{
      |  "optoutClickUrl": "http://click.url",
      |  "optoutImageUrl": "http://image.url",
      |  "longLegalText": "my long legal text"
      |}""".trimMargin()

    val privacy = read(json)

    assertThat(privacy.clickUrl).isEqualTo(URI.create("http://click.url"))
    assertThat(privacy.imageUrl).isEqualTo(URI.create("http://image.url").toURL())
    assertThat(privacy.legalText).isEqualTo("my long legal text")
  }

  private fun read(json: String): NativePrivacy {
    return jsonSerializer.read(NativePrivacy::class.java, ByteArrayInputStream(json.toByteArray()))
  }

}