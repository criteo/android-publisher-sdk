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
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.integration.IntegrationLogMessage.onDeclaredIntegrationRead
import com.criteo.publisher.integration.IntegrationLogMessage.onIntegrationDeclared
import com.criteo.publisher.integration.IntegrationLogMessage.onMediationAdapterDetected
import com.criteo.publisher.integration.IntegrationLogMessage.onMultipleMediationAdaptersDetected
import com.criteo.publisher.integration.IntegrationLogMessage.onNoDeclaredIntegration
import com.criteo.publisher.integration.IntegrationLogMessage.onUnknownIntegrationName
import com.criteo.publisher.logging.LoggerFactory
import com.criteo.publisher.util.SafeSharedPreferences

@OpenForTesting
class IntegrationRegistry(
    private val sharedPreferences: SharedPreferences,
    private val integrationDetector: IntegrationDetector
) {

  private val safeSharedPreferences = SafeSharedPreferences(sharedPreferences)
  private val logger = LoggerFactory.getLogger(javaClass)

  /**
   * Profile ID used by the SDK, so CDB and the Supply chain can recognize that the request comes
   * from the PublisherSDK.
   */
  val profileId: Int
    get() = readIntegration().profileId

  fun declare(integration: Integration) {
    logger.log(onIntegrationDeclared(integration))

    sharedPreferences.edit()
        .putString(IntegrationStorageKey, integration.name)
        .apply()
  }

  @Suppress("SwallowedException") // Exception is not really swallowed as the issue get logged
  fun readIntegration(): Integration {
    detectMediationIntegration()?.let {
      return it
    }

    val integrationName = safeSharedPreferences.getString(
        IntegrationStorageKey,
        null
    )

    return if (integrationName == null) {
      logger.log(onNoDeclaredIntegration())
      Integration.FALLBACK
    } else {
      try {
        val integration = Integration.valueOf(integrationName)
        logger.log(onDeclaredIntegrationRead(integration))
        integration
      } catch (e: IllegalArgumentException) {
        logger.log(onUnknownIntegrationName(integrationName))
        Integration.FALLBACK
      }
    }
  }

  private fun detectMediationIntegration(): Integration? {
    val moPubMediationPresent = integrationDetector.isMoPubMediationPresent()
    val adMobMediationPresent = integrationDetector.isAdMobMediationPresent()

    return if (moPubMediationPresent && adMobMediationPresent) {
      logger.log(onMultipleMediationAdaptersDetected())
      Integration.FALLBACK
    } else if (moPubMediationPresent) {
      logger.log(onMediationAdapterDetected("MoPub"))
      Integration.MOPUB_MEDIATION
    } else if (adMobMediationPresent) {
      logger.log(onMediationAdapterDetected("AdMob"))
      Integration.ADMOB_MEDIATION
    } else {
      null
    }
  }

  private companion object {
    const val IntegrationStorageKey = "CriteoCachedIntegration"
  }
}
