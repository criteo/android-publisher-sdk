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

package com.criteo.pubsdk_android.integration

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.integration.Integration
import com.criteo.publisher.integration.IntegrationDetector
import com.criteo.publisher.integration.IntegrationRegistry

internal class MockedIntegrationRegistry(
    sharedPreferences: SharedPreferences,
    integrationDetector: IntegrationDetector,
    private val context: Context,
    private val allMock: Boolean,
    private val verbose: Boolean
) : IntegrationRegistry(sharedPreferences, integrationDetector) {

  private var forcedIntegration: Integration? = null

  init {
    forceDeclaration(Integration.FALLBACK)
  }

  override val profileId: Int
    get() = forcedIntegration?.profileId ?: super.profileId

  override fun declare(integration: Integration) {
    super.declare(integration)

    if (allMock) {
      showToast("Ignored integration declaration: $integration. Profile ID: $profileId")
    } else {
      showToast("Integration declaration: $integration. Profile ID: $profileId")
    }
  }

  fun forceDeclaration(integration: Integration) {
    if (allMock) {
      forcedIntegration = integration
      showToast("Forced integration declaration: $integration. Profile ID: $profileId")
    }
  }

  private fun showToast(message: String) {
    if (verbose) {
      Toast.makeText(
          context,
          message,
          Toast.LENGTH_SHORT
      ).show()
    }
  }

  internal companion object {
    @JvmStatic
    fun force(integration: Integration) {
      val integrationRegistry = DependencyProvider.getInstance().provideIntegrationRegistry()
      val mockedIntegrationRegistry = integrationRegistry as? MockedIntegrationRegistry
      mockedIntegrationRegistry?.forceDeclaration(integration)
    }
  }
}
