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

package com.criteo.publisher.model.nativeads;

import androidx.annotation.NonNull;
import com.criteo.publisher.Clock;
import com.criteo.publisher.model.AbstractTokenValue;
import com.criteo.publisher.model.Slot;

/**
 * Token given to publisher so that he can asynchronously fetch an native ad.
 */
public class NativeTokenValue extends AbstractTokenValue {

  @NonNull
  private final NativeAssets assets;

  public NativeTokenValue(
      @NonNull NativeAssets assets,
      @NonNull Slot slot,
      @NonNull Clock clock
  ) {
    super(slot, clock);
    this.assets = assets;
  }

  @NonNull
  public NativeAssets getNativeAssets() {
    return assets;
  }

}
