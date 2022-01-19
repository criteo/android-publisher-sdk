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
