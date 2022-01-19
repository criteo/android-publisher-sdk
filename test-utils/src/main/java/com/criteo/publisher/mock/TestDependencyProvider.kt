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

import android.content.SharedPreferences
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.logging.Logger
import com.criteo.publisher.network.CdbMock

@OpenForTesting
class TestDependencyProvider : DependencyProvider() {

  fun <T : Any> inject(klass: Class<T>, value: T) {
    services[klass] = value
  }

  fun provideLogger(): Logger {
    throw UnsupportedOperationException("Logger is not provided")
  }

  fun provideCdbMock(): CdbMock {
    throw UnsupportedOperationException("CdbMock is not provided")
  }

  @Deprecated(message = "Some tests were already build with injected shared preferences. But the SDK is dealing with" +
      "2 kinds of shared prefs: internal and app. This one refers to the internal one and you should explicit it in" +
      "the test.",
      replaceWith = ReplaceWith(
          "provideSharedPreferencesFactory().internal",
          "android.content.SharedPreferences"
      )
  )
  fun provideInternalSharedPreferences(): SharedPreferences {
    return getOrCreate(SharedPreferences::class.java) {
      provideSharedPreferencesFactory().internal
    }
  }
}
