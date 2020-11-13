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

package com.criteo.publisher.advancednative

import androidx.annotation.VisibleForTesting
import com.criteo.publisher.Bid
import com.criteo.publisher.logging.LogMessage
import com.criteo.publisher.loggingId
import com.criteo.publisher.model.NativeAdUnit

internal object NativeLogMessage {

  @JvmStatic
  fun onNativeLoaderInitialized(adUnit: NativeAdUnit?) = LogMessage(message =
    "NativeLoader initialized for $adUnit"
  )

  @JvmStatic
  fun onNativeLoading(nativeLoader: CriteoNativeLoader) = LogMessage(message =
    "Native(${nativeLoader.adUnit}) is loading"
  )

  @JvmStatic
  fun onNativeLoading(nativeLoader: CriteoNativeLoader, bid: Bid?) = LogMessage(message =
    "Native(${nativeLoader.adUnit}) is loading with bid ${bid?.loggingId}"
  )

  @JvmStatic
  fun onNativeLoaded(nativeLoader: CriteoNativeLoader?) = LogMessage(message =
    "Native(${nativeLoader?.adUnit}) is loaded"
  )

  @JvmStatic
  fun onNativeFailedToLoad(nativeLoader: CriteoNativeLoader?) = LogMessage(message =
    "Native(${nativeLoader?.adUnit}) failed to load"
  )

  @JvmStatic
  fun onNativeImpressionRegistered(nativeLoader: CriteoNativeLoader?) =
      onNativeImpressionRegistered(nativeLoader?.adUnit)

  @JvmStatic
  @VisibleForTesting
  fun onNativeImpressionRegistered(nativeAdUnit: NativeAdUnit?) = LogMessage(message =
    "Native($nativeAdUnit) impression registered"
  )

  @JvmStatic
  fun onNativeClicked(nativeLoader: CriteoNativeLoader?) =
      onNativeClicked(nativeLoader?.adUnit)

  @JvmStatic
  @VisibleForTesting
  fun onNativeClicked(nativeAdUnit: NativeAdUnit?) = LogMessage(message =
    "Native($nativeAdUnit) clicked"
  )
}
