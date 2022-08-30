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
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class BooleanJsonAdapterTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var moshi: Moshi

  private lateinit var adapter: JsonAdapter<Boolean>

  @Before
  fun setUp() {
    adapter = moshi.adapter(Boolean::class.java)
  }

  @Test
  fun fromJson_givenValidBooleanString_ShouldParse() {
    assertThat(adapter.fromJson("\"true\"")).isTrue
    assertThat(adapter.fromJson("\"false\"")).isFalse
  }

  @Test
  fun fromJson_givenValidBooleanValue_ShouldParse() {
    assertThat(adapter.fromJson("true")).isTrue
    assertThat(adapter.fromJson("false")).isFalse
  }

  @Test
  fun fromJson_givenNonStringAndNonBooleanValue_ShouldThrowJsonDataException() {
    assertThatExceptionOfType(JsonDataException::class.java).isThrownBy {
      adapter.fromJson("123.123")
    }
    assertThatExceptionOfType(JsonDataException::class.java).isThrownBy {
      adapter.fromJson("123123")
    }
  }

  @Test
  fun toJson_givenBoolean_ShouldWriteValidJson() {
    assertThat(adapter.toJson(true)).isEqualTo("true")
    assertThat(adapter.toJson(false)).isEqualTo("false")
  }
}
