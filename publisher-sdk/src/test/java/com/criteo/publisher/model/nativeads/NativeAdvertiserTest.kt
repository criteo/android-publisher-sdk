package com.criteo.publisher.model.nativeads

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.JsonSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.URI
import javax.inject.Inject

class NativeAdvertiserTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  @Test
  fun fromJson_GivenEmptyDataExceptUrl_ReturnsAdvertiserWithEmptyData() {
    val json = """{
      |  "domain": "",
      |  "description": "",
      |  "logoClickUrl": "http://click.url",
      |  "logo": {
      |    "url": "http://logo.url"
      |  }
      |}""".trimMargin()

    val advertiser = read(json)

    assertThat(advertiser.domain).isEqualTo("")
    assertThat(advertiser.description).isEqualTo("")
    assertThat(advertiser.logoClickUrl).isEqualTo(URI.create("http://click.url").toURL())
    assertThat(advertiser.logo.url).isEqualTo(URI.create("http://logo.url").toURL())
  }

  @Test
  fun fromJson_GivenSomeData_ReadThem() {
    val json = """{
      |  "domain": "myDomain",
      |  "description": "myDescription",
      |  "logoClickUrl": "http://click.url",
      |  "logo": {
      |    "url": "http://logo.url"
      |  }
      |}""".trimMargin()

    val advertiser = read(json)

    assertThat(advertiser.domain).isEqualTo("myDomain")
    assertThat(advertiser.description).isEqualTo("myDescription")
    assertThat(advertiser.logoClickUrl).isEqualTo(URI.create("http://click.url").toURL())
    assertThat(advertiser.logo.url).isEqualTo(URI.create("http://logo.url").toURL())
  }

  private fun read(json: String): NativeAdvertiser {
    return jsonSerializer.read(NativeAdvertiser::class.java, ByteArrayInputStream(json.toByteArray()))
  }

}