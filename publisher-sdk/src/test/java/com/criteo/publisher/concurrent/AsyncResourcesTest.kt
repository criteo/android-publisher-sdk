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

package com.criteo.publisher.concurrent

import org.junit.Test
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

class AsyncResourcesTest {

  @Test
  fun onNewAsyncResource_GivenNewResource_CallIt() {
    val asyncResources = spy(TestAsyncResources())

    asyncResources.newResource {}

    verify(asyncResources).onNewAsyncResource()
  }

  @Test
  fun onReleasedAsyncResource_GivenReleaseResources_CallItOnlyOnce() {
    val asyncResources = spy(TestAsyncResources())

    asyncResources.newResource {
      release()
      release()
    }

    verify(asyncResources).onReleasedAsyncResource()
  }

  private open class TestAsyncResources : AsyncResources() {
    public override fun onNewAsyncResource() {}
    public override fun onReleasedAsyncResource() {}
  }

}