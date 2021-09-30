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

import com.criteo.publisher.mock.TestResource

/**
 * Setup a default uncaught thread handler which catch all throwables and rethrow them in test thread during tear down.
 * This is to prevent the process to crash because an uncaught exception occurred in a sub thread.
 */
class UncaughtThreadResource : TestResource {

  private var previousUncaughtThreadHandler: Thread.UncaughtExceptionHandler? = null
  private val caughtThrowable = mutableListOf<Throwable>()

  override fun setUp() {
    previousUncaughtThreadHandler = Thread.getDefaultUncaughtExceptionHandler()

    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
      caughtThrowable.add(throwable)
    }
  }

  override fun tearDown() {
    Thread.setDefaultUncaughtExceptionHandler(previousUncaughtThreadHandler)

    val throwable = caughtThrowable.fold(null as Throwable?) { first, throwable ->
      if (first == null) {
        throwable
      } else {
        first.addSuppressed(throwable)
        first
      }
    }

    if (throwable != null) {
      throw throwable
    }
  }
}
