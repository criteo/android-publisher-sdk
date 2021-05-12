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

package com.criteo.publisher.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class MapUtilTest {

  @Test
  fun getOrCompute_GivenValueAlreadyInMap_ReturnItWithoutExecutingFunction() {
    val map = ConcurrentHashMap<String, Any>()
    var isComputed = false

    map["hello"] = "there"
    val value = map.getOrCompute("hello") {
      isComputed = true
      "world"
    }

    assertThat(map["hello"]).isEqualTo("there").isEqualTo(value)
    assertThat(isComputed).isFalse()
  }

  @Test
  fun getOrCompute_GivenValueNotInMap_ReturnComputedValue() {
    val map = ConcurrentHashMap<String, Any>()
    val computed = mock<Any>()

    val value = map.getOrCompute("hello") { computed }

    assertThat(map["hello"]).isSameAs(computed).isSameAs(value)
  }

  @Test
  fun getOrCompute_GivenConcurrentValueSetInMapDuringComputation_ReturnConcurrentValue() {
    val map = ConcurrentHashMap<String, Any>()
    val concurrent = mock<Any>()
    val isComputing = CountDownLatch(1)
    val isConcurrentModificationDone = CountDownLatch(1)
    val executor = Executors.newSingleThreadExecutor()

    executor.submit {
      isComputing.await()
      map["hello"] = concurrent
      isConcurrentModificationDone.countDown()
    }

    val value = map.getOrCompute("hello") {
      isComputing.countDown()
      isConcurrentModificationDone.await()
      mock()
    }

    assertThat(map["hello"]).isSameAs(concurrent).isSameAs(value)
  }

}
