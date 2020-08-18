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

public interface CriteoInterstitialAdListener extends CriteoAdListener {

  /**
   * Called when an ad is successfully fetched.
   */
  void onAdReceived();

  default void onAdReadyToDisplay() {
    // FIXME This is only temporary, onAdReadyToDisplay will disappear and onAdReceived will be
    //  fired instead of the current onAdReadyToDisplay.
  }

}

