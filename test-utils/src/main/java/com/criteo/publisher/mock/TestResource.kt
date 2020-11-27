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

package com.criteo.publisher.mock

interface TestResource {
  fun setUp() {
    // no-op by default
  }

  fun tearDown() {
    // no-op by default
  }

  class CompositeTestResource(private val testResources: List<TestResource>) : TestResource {
    override fun setUp() {
      testResources.forEach {
        it.setUp()
      }
    }

    override fun tearDown() {
      testResources.asReversed().forEach {
        it.tearDown()
      }
    }
  }
}
