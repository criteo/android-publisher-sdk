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

package com.criteo.testapp.integration

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.criteo.publisher.integration.IntegrationDetector
import com.criteo.testapp.R
import com.criteo.testapp.mock.MockedDependencyProvider
import com.criteo.testapp.mock.MockedDependencyProvider.MockInjection
import com.criteo.testapp.mock.MockedDependencyProvider.resetCriteo

enum class IntegrationSelectionMode {
  /**
   * Nothing is mocked, but mediation adapters are both present and have a higher precedence, so SDK is using the
   * fallback profile ID.
   */
  NoMock,

  /**
   * Integration are detected normally without considering mediation adapters.
   */
  NoMediation,

  /**
   * Integrations are forced by the Activities that represent them.
   * Like when going on the InHouse activity, then integration is directly declared as InHouse.
   */
  AllMocked,
}

class IntegrationSelectorActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_integration_selector)

    findViewById<Button>(R.id.btnSaveIntegrationDetectionValues).setOnClickListener {
      commitConfiguration()
    }
  }

  private fun commitConfiguration() {
    val verbose = findViewById<CheckBox>(R.id.checkboxVerboseIntegrationDetection).isChecked
    val noMock = findViewById<RadioButton>(R.id.radioNoMock).isChecked
    val allMocked = findViewById<RadioButton>(R.id.radioAllMocked).isChecked
    val noMediation = findViewById<RadioButton>(R.id.radioNoMediation).isChecked

    val mode = when {
      noMock -> IntegrationSelectionMode.NoMock
      allMocked -> IntegrationSelectionMode.AllMocked
      noMediation -> IntegrationSelectionMode.NoMediation
      else -> throw UnsupportedOperationException()
    }

    resetCriteo {
      MockedDependencyProvider.startMocking {
        mockIntegrationRegistry(mode, verbose)
      }
    }
  }

  companion object {
    internal fun MockInjection.mockIntegrationRegistry(mode: IntegrationSelectionMode, verbose: Boolean) {
      if (mode == IntegrationSelectionMode.NoMock && !verbose) {
        return
      }

      val integrationDetector = if (mode == IntegrationSelectionMode.NoMediation) {
        NoIntegrationDetector()
      } else {
        IntegrationDetector()
      }

      val integrationRegistry = MockedIntegrationRegistry(
          oldDependencyProvider.provideSharedPreferences(),
          integrationDetector,
          oldDependencyProvider.provideContext(),
          mode == IntegrationSelectionMode.AllMocked,
          verbose
      )

      inject(integrationDetector)
      inject(integrationRegistry)
    }
  }
}
