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

package com.criteo.publisher.dependency

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.function.Supplier

class LazyDependencyTest {

  @Test
  fun get_GivenSupplier_CallItOnlyOnceAndCacheResult() {
    val supplier = mock<Supplier<Any>>() {
      on { get() } doReturn "foo"
    }

    val lazyDependency = LazyDependency { supplier.get() }

    verify(supplier, never()).get()

    val dependency1 = lazyDependency.get()
    val dependency2 = lazyDependency.get()

    verify(supplier, times(1)).get()
    assertThat(dependency1).isSameAs(dependency2).isEqualTo("foo")
  }

  @Test
  fun get_GivenSupplierThrowing_RethrowButDontCacheException() {
    val exception = RuntimeException()
    val supplier = mock<Supplier<Any>>() {
      on { get() } doThrow exception doReturn "foo"
    }

    val lazyDependency = LazyDependency { supplier.get() }

    assertThatCode {
      lazyDependency.get()
    }.isEqualTo(exception)

    assertThatCode {
      lazyDependency.get()
    }.doesNotThrowAnyException()

    val dependency = lazyDependency.get()

    verify(supplier, times(2)).get()
    assertThat(dependency).isEqualTo("foo")
  }
}
