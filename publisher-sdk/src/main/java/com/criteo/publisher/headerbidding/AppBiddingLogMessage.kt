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

package com.criteo.publisher.headerbidding

import android.util.Log
import com.criteo.publisher.Bid
import com.criteo.publisher.integration.Integration
import com.criteo.publisher.logging.LogMessage
import com.criteo.publisher.loggingId

internal object AppBiddingLogMessage {

  @JvmStatic
  fun onTryingToEnrichAdObjectFromBid(bid: Bid?) = LogMessage(message =
    "Attempting to set bids as AppBidding from bid ${bid?.loggingId}"
  )

  @JvmStatic
  fun onAdObjectEnrichedWithNoBid(integration: Integration) = LogMessage(message =
    "Failed to set bids as $integration: No bid found"
  )

  @JvmStatic
  fun onAdObjectEnrichedSuccessfully(integration: Integration, enrichment: String) = LogMessage(message =
    "$integration bid set as targeting: $enrichment"
  )

  @JvmStatic
  fun onUnknownAdObjectEnriched(adObject: Any?) = LogMessage(
      level = Log.ERROR,
      message = "Failed to set bids: unknown '${adObject?.javaClass}' object given",
      logId = "onUnknownAdObjectEnriched"
  )
}
