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

package com.criteo.publisher.util.jsonadapter

import com.criteo.publisher.mock.MockedDependenciesRule
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.URL
import javax.inject.Inject

class URLAdapterTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var moshi: Moshi

  private lateinit var adapter: JsonAdapter<URL>

  @Before
  fun setUp() {
    adapter = moshi.adapter(URL::class.java)
  }

  @Test
  fun fromJson_givenValidURLString_ShouldParse() {
    Assertions.assertThat(adapter.fromJson("\"http://click.url\"")).isEqualTo(URL("http://click.url"))
  }

  @Test
  fun fromJson_givenInvalidURLString_ShouldThrowIOException() {
    Assertions.assertThatIOException().isThrownBy {
      adapter.fromJson("\"\"")
    }
    Assertions.assertThatIOException().isThrownBy {
      adapter.fromJson("\"Hello, i'm invalid URL\"")
    }
  }

  @Test
  fun fromJson_givenNonString_ShouldThrowJsonDataException() {
    Assertions.assertThatExceptionOfType(JsonDataException::class.java).isThrownBy {
      adapter.fromJson("true")
    }
    Assertions.assertThatExceptionOfType(JsonDataException::class.java).isThrownBy {
      adapter.fromJson("null")
    }
    Assertions.assertThatExceptionOfType(JsonDataException::class.java).isThrownBy {
      adapter.fromJson("123123")
    }
  }

  @Test
  fun toJson_givenURL_ShouldWriteValidJson() {
    Assertions.assertThat(adapter.toJson(URL("http://click.url"))).isEqualTo("\"http://click.url\"")
  }

  @Test
  fun toJson_givenNull_ShouldThrowNullPointerException() {
    Assertions.assertThatExceptionOfType(NullPointerException::class.java).isThrownBy {
      adapter.toJson(null)
    }
  }
}
