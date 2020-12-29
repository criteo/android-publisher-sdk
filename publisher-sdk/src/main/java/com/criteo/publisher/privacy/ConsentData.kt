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

package com.criteo.publisher.privacy

import android.content.SharedPreferences
import com.criteo.publisher.annotation.OpenForTesting

@OpenForTesting
class ConsentData(val sharedPreferences: SharedPreferences) {
  companion object {
    private const val CRITEO_CONSENT_GIVEN_KEY = "CRTO_ConsentGiven"
  }

  @Synchronized
  fun isConsentGiven() = sharedPreferences.getBoolean(CRITEO_CONSENT_GIVEN_KEY, false)

  @Synchronized
  fun setConsentGiven(consentGiven: Boolean) {
      val editor = sharedPreferences.edit()
      editor.putBoolean(CRITEO_CONSENT_GIVEN_KEY, consentGiven)
      editor.apply()
    }
}
