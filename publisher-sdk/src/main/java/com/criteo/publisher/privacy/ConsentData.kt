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

import com.criteo.publisher.annotation.OpenForTesting
import java.util.concurrent.atomic.AtomicReference

@OpenForTesting
class ConsentData {

  /**
   * Consent can be in any of the following two states:
   *
   * <ul>
   *  <li> CONSENT_GIVEN: it is known that consent has been given by the user</li>
   *  <li> CONSENT_NOT_GIVEN: it is known that consent has not been given by the user,
   *  or isn't known yet.</li>
   * </ul>
   */
  enum class ConsentStatus {
    CONSENT_GIVEN,
    CONSENT_NOT_GIVEN
  }
  private val valueRef = AtomicReference<ConsentStatus>(ConsentStatus.CONSENT_NOT_GIVEN)

  var consentStatus: ConsentStatus
    get() = valueRef.get()
    set(value) = valueRef.set(value)
}
