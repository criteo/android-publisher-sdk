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

import java.util.concurrent.atomic.AtomicBoolean

abstract class AsyncResources {
  @Suppress("TooGenericExceptionCaught")
  fun newResource(resourceHandler: AsyncResource.() -> Unit) {
    val resource = AsyncResource()
    try {
      resourceHandler(resource)
    } catch (t: Throwable) {
      resource.release()
      throw t
    }
  }

  protected abstract fun onNewAsyncResource()
  protected abstract fun onReleasedAsyncResource()

  inner class AsyncResource {
    private val isReleased = AtomicBoolean(false)

    init {
      onNewAsyncResource()
    }

    fun release() {
      if (isReleased.compareAndSet(false, true)) {
        onReleasedAsyncResource()
      }
    }
  }
}

internal class NoOpAsyncResources : AsyncResources() {
  override fun onNewAsyncResource() {
    // no-op
  }

  override fun onReleasedAsyncResource() {
    // no-op
  }
}
