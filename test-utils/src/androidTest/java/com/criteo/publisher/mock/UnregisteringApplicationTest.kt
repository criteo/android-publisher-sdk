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

import android.app.Application
import com.criteo.publisher.CriteoUtil
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.MockableDependencyProvider
import com.criteo.publisher.application.UnregisteringApplication
import org.junit.After
import org.junit.Test
import org.junit.runners.model.Statement
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UnregisteringApplicationTest {

  @After
  fun tearDown() {
    CriteoUtil.clearCriteo()
    MockableDependencyProvider.setInstance(null)
    UnregisteringApplication.unregisterAllActivityLifecycleCallbacks()
  }

  @Test
  fun givenInitializedCriteo_GivenApplicationWithCallbacks_UnregisterThem() {
    CriteoUtil.givenInitializedCriteo()

    givenApplicationWithCallbacks_UnregisterThem()
  }

  @Test
  fun givenCriteoBuilder_GivenApplicationWithCallbacks_UnregisterThem() {
    CriteoUtil.getCriteoBuilder().init()

    givenApplicationWithCallbacks_UnregisterThem()
  }

  @Test
  fun mockedDependencyRule_GivenApplicationWithCallbacks_UnregisterThem() {
    MockedDependenciesRule().withoutCdbMock().apply(object : Statement() {
      override fun evaluate() {
        givenApplicationWithCallbacks_UnregisterThem()
      }
    }, null, this).evaluate()
  }

  private fun givenApplicationWithCallbacks_UnregisterThem() {
    val application = DependencyProvider.getInstance().provideApplication()

    val callback1 = mock<Application.ActivityLifecycleCallbacks>()
    val callback2 = mock<Application.ActivityLifecycleCallbacks>()

    application.registerActivityLifecycleCallbacks(callback1)
    application.registerActivityLifecycleCallbacks(callback2)

    UnregisteringApplication.unregisterAllActivityLifecycleCallbacks()

    verify(application).unregisterActivityLifecycleCallbacks(callback1)
    verify(application).unregisterActivityLifecycleCallbacks(callback2)
  }
}
