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

import com.criteo.publisher.CriteoUtil.givenInitializedCriteo
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SdkServiceLifecycleManagerTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var sdkServiceLifecycleManager: SdkServiceLifecycleManager

  @Test
  fun givenSdkServiceProvidedByDependencyProvider_AllGetsRegistered() {
    val dependencyProvider = mockedDependenciesRule.dependencyProvider

    val methods = DependencyProvider::class.java.declaredMethods.filter { it.name.startsWith("provide") }
    val sdkServiceMethods = methods.filter { SdkServiceLifecycle::class.java.isAssignableFrom(it.returnType) }
    val sdkServices = sdkServiceMethods.map { it.invoke(dependencyProvider) as SdkServiceLifecycle }

    val registeredServices = sdkServiceLifecycleManager.services

    assertThat(sdkServices).isNotEmpty
    assertThat(registeredServices).containsExactlyInAnyOrderElementsOf(sdkServices)
  }

  @Test
  fun onSdkInitialized_GivenMultipleServices_AllGetInvoked() {
    val service1 = mock<SdkServiceLifecycle>()
    val service2 = mock<SdkServiceLifecycle>()
    val sdkInput = SdkInput()

    sdkServiceLifecycleManager = SdkServiceLifecycleManager(listOf(service1, service2))
    sdkServiceLifecycleManager.onSdkInitialized(sdkInput)

    verify(service1).onSdkInitialized(sdkInput)
    verify(service2).onSdkInitialized(sdkInput)
  }

  @Test
  fun onSdkInitialized_GivenCriteoInitialization_ManagerGetInvoked() {
    givenInitializedCriteo()

    verify(sdkServiceLifecycleManager).onSdkInitialized(any())
  }
}
