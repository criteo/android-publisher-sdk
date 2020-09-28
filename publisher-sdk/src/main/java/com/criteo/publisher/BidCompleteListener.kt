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

import androidx.annotation.Keep

/**
 * Interface to be used in `App Bidding` integration mode to know when bidding has completed,
 * before the Ad can be loaded.
 *
 * @see: https://publisherdocs.criteotilt.com/app/android/app-bidding/
 */
@Keep
interface BidCompleteListener {
  /**
   * Callback to know when Criteo has finished bidding on a given AdUnit.
   */
  fun onBiddingComplete()
}
