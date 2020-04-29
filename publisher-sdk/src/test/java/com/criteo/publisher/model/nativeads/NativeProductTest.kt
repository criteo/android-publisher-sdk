package com.criteo.publisher.model.nativeads

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.JsonSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.URI
import javax.inject.Inject

class NativeProductTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  @Test
  fun fromJson_GivenEmptyDataExceptUrl_ReturnsProductWithEmptyData() {
    val json = """{
      |  "title": "",
      |  "description": "",
      |  "price": "",
      |  "clickUrl": "http://criteo.com",
      |  "callToAction": "",
      |  "image": {
      |    "url": "http://criteo.com"
      |  }
      |}""".trimMargin()

    val product = read(json)

    assertThat(product.title).isEqualTo("")
    assertThat(product.description).isEqualTo("")
    assertThat(product.price).isEqualTo("")
    assertThat(product.clickUrl).isEqualTo(URI.create("http://criteo.com").toURL())
    assertThat(product.callToAction).isEqualTo("")
    assertThat(product.imageUrl).isEqualTo(URI.create("http://criteo.com").toURL())
  }

  @Test
  fun fromJson_GivenSomeData_ReadThem() {
    val json = """{
      |  "title": "myTitle",
      |  "description": "myDescription",
      |  "price": "10€",
      |  "clickUrl": "http://click.url",
      |  "callToAction": "myCTA",
      |  "image": {
      |    "url": "http://image.url"
      |  }
      |}""".trimMargin()

    val product = read(json)

    assertThat(product.title).isEqualTo("myTitle")
    assertThat(product.description).isEqualTo("myDescription")
    assertThat(product.price).isEqualTo("10€")
    assertThat(product.clickUrl).isEqualTo(URI.create("http://click.url").toURL())
    assertThat(product.callToAction).isEqualTo("myCTA")
    assertThat(product.imageUrl).isEqualTo(URI.create("http://image.url").toURL())
  }

  private fun read(json: String): NativeProduct {
    return jsonSerializer.read(NativeProduct::class.java, ByteArrayInputStream(json.toByteArray()))
  }

}