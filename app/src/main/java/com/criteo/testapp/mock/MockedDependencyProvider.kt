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

package com.criteo.testapp.mock

import com.criteo.publisher.Criteo
import com.criteo.publisher.CriteoUtil
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.MockableDependencyProvider
import com.criteo.publisher.integration.IntegrationDetector
import com.criteo.publisher.integration.IntegrationRegistry
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever

internal object MockedDependencyProvider {

  /**
   * Setup a new mocked dependency provider.
   *
   * This method can be called many times. Old mocks are erased and a new fresh dependency provider
   * is setup.
   *
   * The Criteo SDK is also reinitialized (without any prefetched bids) so there is no issue in the
   * dependency graph.
   */
  fun startMocking(injections: MockInjection.() -> Unit) {
    // Mockito needs this property to know where to store generated classes
    val application = DependencyProvider.getInstance().provideApplication()
    System.setProperty("org.mockito.android.target", application.cacheDir.path)

    resetCriteo {
      MockableDependencyProvider.setInstance(null)
      val newInstance = DependencyProvider.getInstance()
      val dependencyProvider = spy(newInstance)
      MockableDependencyProvider.setInstance(dependencyProvider)

      injections(MockInjection(dependencyProvider))
    }
  }

  /**
   * Stop having a mocked dependency provider.
   *
   * This method can be called many times. A new fresh dependency provider is setup.
   *
   * The Criteo SDK is also reinitialized (without any prefetched bids) so there is no issue in the
   * dependency graph.
   */
  fun stopMocking() {
    resetCriteo {
      MockableDependencyProvider.setInstance(null)
    }
  }

  private fun resetCriteo(action: () -> Unit) {
    val oldInstance = DependencyProvider.getInstance()
    val application = oldInstance.provideApplication()
    val criteoPublisherId = oldInstance.provideCriteoPublisherId()

    action()

    CriteoUtil.clearCriteo()
    Criteo.Builder(application, criteoPublisherId).init()
  }

  internal class MockInjection(private val dependencyProvider: DependencyProvider) {
    fun <T> inject(bean: T) {
      // When mocking using the doReturn API of mockito, the method call return a null. But all
      // provide* methods of the dependency provider are @NonNull so Kotlin compiler automatically
      // inject a null check, which is trigger by Mockito returning null. The stubbing call is then
      // nullify to avoid triggering this and getting an NPE.
      val stubCall: DependencyProvider? = doReturn(bean).whenever(dependencyProvider)

      when (bean) {
        is IntegrationRegistry -> stubCall?.provideIntegrationRegistry()
        is IntegrationDetector -> stubCall?.provideIntegrationDetector()
        else -> throw UnsupportedOperationException("Not supported dependency")
      }
    }
  }
}
