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
package com.criteo.publisher.context

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ContextDataTest {

  @Test
  fun getData_GivenNoData_ReturnEmpty() {
    val contextData = ContextData()

    val data = contextData.data

    assertThat(data).isEmpty()
  }

  @Test
  fun getData_GivenData_ReturnEmpty() {
    val list = mutableListOf("a", "b")

    val contextData = ContextData()
        .set("1", "overridden")
        .set("2", 2)
        .set("3", 3.0)
        .set("1", list)

    list.add("c")

    val expected = mapOf(
        "1" to listOf("a", "b"),
        "2" to 2L,
        "3" to 3.0
    )

    val data = contextData.data

    assertThat(data).containsExactlyInAnyOrderEntriesOf(expected)
  }
}
