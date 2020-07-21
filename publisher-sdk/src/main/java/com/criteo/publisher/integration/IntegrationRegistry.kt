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
import androidx.annotation.VisibleForTesting
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.util.PreconditionsUtil
import com.criteo.publisher.util.SafeSharedPreferences

@OpenForTesting
class IntegrationRegistry(
    private val sharedPreferences: SharedPreferences,
    private val integrationDetector: IntegrationDetector
) {

  private val safeSharedPreferences = SafeSharedPreferences(sharedPreferences)

  /**
   * Profile ID used by the SDK, so CDB and the Supply chain can recognize that the request comes
   * from the PublisherSDK.
   */
  val profileId: Int
    get() = readIntegration().profileId

  fun declare(integration: Integration) {
    sharedPreferences.edit()
        .putString(IntegrationStorageKey, integration.name)
        .apply()
  }

  @VisibleForTesting
  fun readIntegration(): Integration {
    if (integrationDetector.isMoPubMediationPresent()) {
      return Integration.MOPUB_MEDIATION
    } else if (integrationDetector.isAdMobMediationPresent()) {
      return Integration.ADMOB_MEDIATION
    }
    val integrationName = safeSharedPreferences.getString(
        IntegrationStorageKey,
        Integration.FALLBACK.name
    )!!

    return try {
      Integration.valueOf(integrationName)
    } catch (e: IllegalArgumentException) {
      PreconditionsUtil.throwOrLog(e)
      Integration.FALLBACK
    }
  }

  private companion object {
    const val IntegrationStorageKey = "CriteoCachedIntegration"
  }

}