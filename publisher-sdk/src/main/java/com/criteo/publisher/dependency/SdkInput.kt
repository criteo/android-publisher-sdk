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

import android.app.Application
import com.criteo.publisher.CriteoNotInitializedException
import com.criteo.publisher.util.TextUtils

data class SdkInput(
    val application: LazyDependency<Application>,
    val criteoPublisherId: LazyDependency<String>,
    val usPrivacyOptOut: Boolean?
) {

  constructor() : this(
      LazyDependency { throw CriteoNotInitializedException("Application reference is required") },
      LazyDependency { throw CriteoNotInitializedException("Criteo Publisher Id is required") },
      null
  )

  fun withApplication(value: Application?): SdkInput {
    return this.copy(application = LazyDependency {
      // In case Java code call this Kotlin code
      value ?: throw CriteoNotInitializedException("Application reference is required")
    })
  }

  fun withCriteoPublisherId(value: String): SdkInput {
    return this.copy(criteoPublisherId = LazyDependency {
      if (TextUtils.isEmpty(value)) {
        throw CriteoNotInitializedException("Criteo Publisher Id is required")
      } else {
        value
      }
    })
  }

  fun withUsPrivacyOptOut(value: Boolean?): SdkInput {
    return this.copy(usPrivacyOptOut = value)
  }
}
