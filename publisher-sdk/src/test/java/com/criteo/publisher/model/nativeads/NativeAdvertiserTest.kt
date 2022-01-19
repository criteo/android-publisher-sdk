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
    assertThat(advertiser.logoClickUrl).isEqualTo(URI.create("http://click.url"))
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
    assertThat(advertiser.logoClickUrl).isEqualTo(URI.create("http://click.url"))
    assertThat(advertiser.logo.url).isEqualTo(URI.create("http://logo.url").toURL())
  }

  private fun read(json: String): NativeAdvertiser {
    return jsonSerializer.read(NativeAdvertiser::class.java, ByteArrayInputStream(json.toByteArray()))
  }
}
