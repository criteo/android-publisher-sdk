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
    assertThat(product.clickUrl).isEqualTo(URI.create("http://criteo.com"))
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
    assertThat(product.clickUrl).isEqualTo(URI.create("http://click.url"))
    assertThat(product.callToAction).isEqualTo("myCTA")
    assertThat(product.imageUrl).isEqualTo(URI.create("http://image.url").toURL())
  }

  private fun read(json: String): NativeProduct {
    return jsonSerializer.read(NativeProduct::class.java, ByteArrayInputStream(json.toByteArray()))
  }

}