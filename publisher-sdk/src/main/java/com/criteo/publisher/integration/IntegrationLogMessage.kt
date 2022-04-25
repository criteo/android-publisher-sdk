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

import android.util.Log
import com.criteo.publisher.logging.LogMessage

internal object IntegrationLogMessage {

  @JvmStatic
  fun onIntegrationDeclared(integration: Integration) = LogMessage(message =
      "The integration `$integration` is automatically declared"
  )

  @JvmStatic
  fun onDeclaredIntegrationRead(integration: Integration) = LogMessage(message =
      "The declared integration `$integration` is used"
  )

  @JvmStatic
  fun onNoDeclaredIntegration() = LogMessage(message =
      "No integration were previously declared, fallbacking on default integration"
  )

  @JvmStatic
  fun onUnknownIntegrationName(integrationName: String) = LogMessage(
      level = Log.ERROR,
      logId = "onUnknownIntegrationName",
      message = "An unknown integration name `$integrationName` was persisted, fallbacking on default integration"
  )

  @JvmStatic
  fun onMediationAdapterDetected(name: String) = LogMessage(message =
      "Mediation adapter `$name` is detected, using it and ignoring the declared one"
  )
}
