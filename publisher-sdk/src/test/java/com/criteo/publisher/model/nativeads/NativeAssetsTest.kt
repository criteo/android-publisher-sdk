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
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.reflect.KClass

class NativeAssetsTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  @Test
  fun fromJson_GivenSampleData_ReadThem() {
    val productJson = getProductJson()
    val advertiserJson = getAdvertiserJson()
    val privacyJson = getPrivacyJson()
    val impressionPixelJson = getImpressionPixelJson()

    val json = """{
      |  "products": [
      |    $productJson
      |  ],
      |  "advertiser": $advertiserJson,
      |  "privacy": $privacyJson,
      |  "impressionPixels": [
      |    $impressionPixelJson
      |  ]
      |}""".trimMargin()

    val assets = read<NativeAssets>(json)

    assertThat(assets.nativeProducts).containsExactlyInAnyOrder(read(productJson))
    assertThat(assets.advertiser).isEqualTo(read<NativeAdvertiser>(advertiserJson))
    assertThat(assets.privacy).isEqualTo(read<NativePrivacy>(privacyJson))
    assertThat(assets.pixels).containsExactlyInAnyOrder(read(impressionPixelJson))
  }

  @Test
  fun fromJson_GivenSampleDataWithoutAnyProduct_ThrowException() {
    val advertiserJson = getAdvertiserJson()
    val privacyJson = getPrivacyJson()
    val impressionPixelJson = getImpressionPixelJson()

    val json = """{
      |  "products": [],
      |  "advertiser": $advertiserJson,
      |  "privacy": $privacyJson,
      |  "impressionPixels": [
      |    $impressionPixelJson
      |  ]
      |}""".trimMargin()

    assertThatCode {
      read<NativeAssets>(json)
    }.isInstanceOf(IOException::class.java)
  }

  @Test
  fun fromJson_GivenSampleDataWithoutAnyImpressionPixels_ThrowException() {
    val productJson = getProductJson()
    val advertiserJson = getAdvertiserJson()
    val privacyJson = getPrivacyJson()

    val json = """{
      |  "products": [
      |    $productJson
      |  ],
      |  "advertiser": $advertiserJson,
      |  "privacy": $privacyJson,
      |  "impressionPixels": []
      |}""".trimMargin()

    assertThatCode {
      read<NativeAssets>(json)
    }.isInstanceOf(IOException::class.java)
  }

  private fun getProductJson(): String {
    return """{
        |      "title": "myTitle",
        |      "description": "myDescription",
        |      "price": "10€",
        |      "clickUrl": "http://click.url",
        |      "callToAction": "myCTA",
        |      "image": {
        |        "url": "http://image.url"
        |      }
        |    }""".trimMargin()
  }

  private fun getImpressionPixelJson(): String {
    return """{"url": "http://pixel.url"}"""
  }

  private fun getPrivacyJson(): String {
    return """{
        |    "optoutClickUrl": "http://click.url",
        |    "optoutImageUrl": "http://image.url",
        |    "longLegalText": "my long legal text"
        |  }""".trimMargin()
  }

  private fun getAdvertiserJson(): String {
    return """{
        |    "domain": "myDomain",
        |    "description": "myDescription",
        |    "logoClickUrl": "http://click.url",
        |    "logo": {
        |      "url": "http://logo.url"
        |    }
        |  }""".trimMargin()
  }

  private inline fun <reified T : Any> read(json: String, klass: KClass<T> = T::class): T {
    return jsonSerializer.read(klass.java, ByteArrayInputStream(json.toByteArray()))
  }

}