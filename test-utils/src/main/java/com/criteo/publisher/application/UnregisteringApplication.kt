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

package com.criteo.publisher.application

import android.app.Application
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.spy

private typealias UnregisteringApplicationAction = () -> Unit

object UnregisteringApplication {
  private val unregisteringApplicationActions = mutableListOf<UnregisteringApplicationAction>()

  @JvmStatic
  fun of(application: Application): Application {
    // In Kotlin, it is not possible to delegate to Application automatically because it is a class, not an interface.
    // Application is a really big class, so overloading is done via Mockito rules instead.

    val spyApplication = spy(application)
    doAnswer {
      unregisteringApplicationActions.add {
        spyApplication.unregisterActivityLifecycleCallbacks(it.getArgument(0))
      }
      it.callRealMethod()
    }.`when`(spyApplication).registerActivityLifecycleCallbacks(any())

    return spyApplication
  }

  @JvmStatic
  fun unregisterAllActivityLifecycleCallbacks() {
    unregisteringApplicationActions.forEach {
      it.invoke()
    }
    unregisteringApplicationActions.clear()
  }
}
