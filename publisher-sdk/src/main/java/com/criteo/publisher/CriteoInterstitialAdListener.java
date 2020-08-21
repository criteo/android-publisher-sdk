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

package com.criteo.publisher;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

public interface CriteoInterstitialAdListener extends CriteoAdListener {

  /**
   * Callback invoked when an interstitial ad is requested and valid bid is answered and creative is
   * successfully received.
   * <p>
   * From this notification, publisher are able to display the interstitial ad call by calling
   * {@link CriteoInterstitial#show()}. It can be done directly in the implementation of this
   * callback, or later.
   */
  @UiThread
  void onAdReceived(@NonNull CriteoInterstitial interstitial);

}

