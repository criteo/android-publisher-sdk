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

package com.criteo.publisher.integration

import android.content.SharedPreferences
import com.criteo.publisher.CriteoUtil.givenInitializedCriteo
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.MockableDependencyProvider
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class IntegrationRegistryTest {

  private companion object {
    const val IntegrationStorageKey = "CriteoCachedIntegration"
  }

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @Inject
  private lateinit var sharedPreferences: SharedPreferences

  @Inject
  private lateinit var integrationRegistry: IntegrationRegistry

  @Test
  fun integration_GivenNoDeclaredOne_ReturnFallback() {
    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.FALLBACK)
  }

  @Test
  fun integration_GivenIllFormedDeclaration_ReturnFallback() {
    whenever(buildConfigWrapper.preconditionThrowsOnException()).doReturn(false)
    sharedPreferences.edit().putInt(IntegrationStorageKey, 42).apply()

    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.FALLBACK)
  }

  @Test
  fun integration_GivenUnknownDeclaration_ReturnFallback() {
    whenever(buildConfigWrapper.preconditionThrowsOnException()).doReturn(false)
    sharedPreferences.edit().putString(IntegrationStorageKey, "unknown").apply()

    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.FALLBACK)
  }

  @Test
  fun integration_GivenPreviouslyDeclaredOne_ReturnDeclaredOne() {
    integrationRegistry.declare(Integration.IN_HOUSE)
    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.IN_HOUSE)
  }

  @Test
  fun integration_GivenPreviouslyDeclaredOneAndNewSession_ReturnDeclaredOne() {
    integrationRegistry.declare(Integration.IN_HOUSE)

    MockableDependencyProvider.setInstance(null)
    givenInitializedCriteo()
    integrationRegistry = DependencyProvider.getInstance().provideIntegrationRegistry()

    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.IN_HOUSE)
  }

}