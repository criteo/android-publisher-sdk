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
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.integration.IntegrationDetector
import com.criteo.testapp.R
import com.criteo.testapp.mock.MockedDependencyProvider

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

    if (!verbose && noMock) {
      MockedDependencyProvider.stopMocking()
    } else {
      val sharedPreferences = DependencyProvider.getInstance().provideSharedPreferences()

      MockedDependencyProvider.startMocking {
        val integrationDetector = if (noMediation) {
          NoIntegrationDetector()
        } else {
          IntegrationDetector()
        }

        val integrationRegistry = MockedIntegrationRegistry(
            sharedPreferences,
            integrationDetector,
            this@IntegrationSelectorActivity,
            allMocked,
            verbose
        )

        inject(integrationDetector)
        inject(integrationRegistry)
      }
    }
  }
}
