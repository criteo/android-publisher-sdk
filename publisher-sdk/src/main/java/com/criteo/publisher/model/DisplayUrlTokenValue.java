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

package com.criteo.publisher.model;

import androidx.annotation.NonNull;
import com.criteo.publisher.Clock;

/**
 * Token given to publisher so that he can asynchronously fetch an ad.
 * The ad asset is just a display URL (i.e. for banner and interstitial).
 */
public class DisplayUrlTokenValue extends AbstractTokenValue {

  @NonNull
  private final String displayUrl;

  public DisplayUrlTokenValue(
      @NonNull String displayUrl,
      @NonNull Slot slot,
      @NonNull Clock clock) {
    super(slot, clock);
    this.displayUrl = displayUrl;
  }

  @NonNull
  public String getDisplayUrl() {
    return displayUrl;
  }

}
