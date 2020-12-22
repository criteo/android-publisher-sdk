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

package com.criteo.publisher

import androidx.annotation.CallSuper
import com.criteo.publisher.annotation.Internal
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.bid.BidLifecycleListener
import com.criteo.publisher.model.CdbRequest
import com.criteo.publisher.model.CdbResponse
import com.criteo.publisher.privacy.ConsentData
import com.criteo.publisher.privacy.ConsentData.ConsentStatus.CONSENT_GIVEN
import com.criteo.publisher.privacy.ConsentData.ConsentStatus.CONSENT_NOT_GIVEN

@Internal
@OpenForTesting
abstract class CdbCallListener(
    private val bidLifecycleListener: BidLifecycleListener,
    private val bidManager: BidManager,
    private val consentData: ConsentData
) {
  @CallSuper
  fun onCdbRequest(cdbRequest: CdbRequest) {
    bidLifecycleListener.onCdbCallStarted(cdbRequest)
  }

  @CallSuper
  fun onCdbError(cdbRequest: CdbRequest, exception: Exception) {
    bidLifecycleListener.onCdbCallFailed(cdbRequest, exception)
  }

  @CallSuper
  fun onCdbResponse(cdbRequest: CdbRequest, cdbResponse: CdbResponse) {
    if (cdbResponse.consentGiven) {
      consentData.consentStatus = CONSENT_GIVEN
    } else {
      consentData.consentStatus = CONSENT_NOT_GIVEN
    }

    bidManager.setTimeToNextCall(cdbResponse.timeToNextCall)
    bidLifecycleListener.onCdbCallFinished(cdbRequest, cdbResponse)
  }

  abstract fun onTimeBudgetExceeded()
}
