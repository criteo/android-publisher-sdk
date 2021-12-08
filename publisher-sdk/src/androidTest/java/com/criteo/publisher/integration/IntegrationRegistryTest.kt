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
import com.criteo.publisher.logging.Logger
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.BuildConfigWrapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import javax.inject.Inject

class IntegrationRegistryTest {

  private companion object {
    const val IntegrationStorageKey = "CriteoCachedIntegration"
  }

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule().withSpiedLogger()

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @Inject
  private lateinit var sharedPreferences: SharedPreferences

  @SpyBean
  private lateinit var integrationDetector: IntegrationDetector

  @SpyBean
  private lateinit var logger: Logger

  @Inject
  private lateinit var integrationRegistry: IntegrationRegistry

  @Test
  fun integration_GivenNoDeclaredOne_ReturnFallback() {
    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.FALLBACK)
    verify(logger).log(IntegrationLogMessage.onNoDeclaredIntegration())
  }

  @Test
  fun integration_GivenIllFormedDeclaration_ReturnFallback() {
    whenever(buildConfigWrapper.preconditionThrowsOnException()).doReturn(false)
    sharedPreferences.edit().putInt(IntegrationStorageKey, 42).apply()

    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.FALLBACK)
    verify(logger).log(IntegrationLogMessage.onNoDeclaredIntegration())
  }

  @Test
  fun integration_GivenUnknownDeclaration_ReturnFallback() {
    whenever(buildConfigWrapper.preconditionThrowsOnException()).doReturn(false)
    sharedPreferences.edit().putString(IntegrationStorageKey, "unknown").apply()

    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.FALLBACK)
    verify(logger).log(IntegrationLogMessage.onUnknownIntegrationName("unknown"))
  }

  @Test
  fun integration_GivenPreviouslyDeclaredOne_ReturnDeclaredOne() {
    integrationRegistry.declare(Integration.IN_HOUSE)
    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.IN_HOUSE)
    logger.inOrder {
      verify().log(IntegrationLogMessage.onIntegrationDeclared(Integration.IN_HOUSE))
      verify().log(IntegrationLogMessage.onDeclaredIntegrationRead(Integration.IN_HOUSE))
    }
  }

  @Test
  fun integration_GivenPreviouslyDeclaredOneAndNewSession_ReturnDeclaredOne() {
    integrationRegistry.declare(Integration.IN_HOUSE)

    mockedDependenciesRule.resetAllDependencies()
    givenInitializedCriteo()

    // Logger get called during init phase. Let's wait and clear unwanted invocations
    mockedDependenciesRule.waitForIdleState()
    clearInvocations(logger)

    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.IN_HOUSE)
    verify(logger).log(IntegrationLogMessage.onDeclaredIntegrationRead(Integration.IN_HOUSE))
  }

  @Test
  fun integration_GivenStandaloneDeclaredButMoPubMediationIsDetected_ReturnMoPubMediation() {
    whenever(integrationDetector.isMoPubMediationPresent()).doReturn(true)

    integrationRegistry.declare(Integration.STANDALONE)
    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.MOPUB_MEDIATION)
    logger.inOrder {
      verify().log(IntegrationLogMessage.onIntegrationDeclared(Integration.STANDALONE))
      verify().log(IntegrationLogMessage.onMediationAdapterDetected("MoPub"))
    }
  }

  @Test
  fun integration_GivenStandaloneDeclaredButAdMobMediationIsDetected_ReturnAdMobMediation() {
    whenever(integrationDetector.isAdMobMediationPresent()).doReturn(true)

    integrationRegistry.declare(Integration.STANDALONE)
    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.ADMOB_MEDIATION)
    logger.inOrder {
      verify().log(IntegrationLogMessage.onIntegrationDeclared(Integration.STANDALONE))
      verify().log(IntegrationLogMessage.onMediationAdapterDetected("AdMob"))
    }
  }

  @Test
  fun integration_GivenBothMediationAdaptersDetected_ReturnFallback() {
    whenever(integrationDetector.isMoPubMediationPresent()).doReturn(true)
    whenever(integrationDetector.isAdMobMediationPresent()).doReturn(true)

    val integration = integrationRegistry.readIntegration()

    assertThat(integration).isEqualTo(Integration.FALLBACK)
    logger.inOrder {
      verify().log(IntegrationLogMessage.onMultipleMediationAdaptersDetected())
    }
  }

}