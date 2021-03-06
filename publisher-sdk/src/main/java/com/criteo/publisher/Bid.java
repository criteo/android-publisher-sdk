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

import static com.criteo.publisher.annotation.Internal.IN_HOUSE;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.annotation.Internal;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.util.AdUnitType;
import kotlin.jvm.functions.Function1;

public class Bid {

  private final double price;

  @NonNull
  private final AdUnitType adUnitType;

  @NonNull
  private final Clock clock;

  @Nullable
  private CdbResponseSlot slot;

  Bid(
      @NonNull AdUnitType adUnitType,
      @NonNull Clock clock,
      @NonNull CdbResponseSlot slot
  ) {
    this.price = slot.getCpmAsNumber();
    this.adUnitType = adUnitType;
    this.slot = slot;
    this.clock = clock;
  }

  @Keep
  public double getPrice() {
    return price;
  }

  @NonNull
  public AdUnitType getAdUnitType() {
    return adUnitType;
  }

  @Nullable
  public CdbResponseSlot consumeSlot() {
    return consume(slot -> slot);
  }

  @Nullable
  @Internal(IN_HOUSE)
  public String consumeDisplayUrlFor(@NonNull AdUnitType adUnitType) {
    if (!adUnitType.equals(this.adUnitType)) {
      return null;
    }

    return consume(CdbResponseSlot::getDisplayUrl);
  }

  @Nullable
  @Internal(IN_HOUSE)
  public NativeAssets consumeNativeAssets() {
    return consume(CdbResponseSlot::getNativeAssets);
  }

  @Nullable
  private synchronized <T> T consume(Function1<CdbResponseSlot, T> action) {
    if (slot == null || slot.isExpired(clock)) {
      return null;
    }

    T element = action.invoke(slot);

    // This object represents a bid usable only once by a publisher. The slot is nullified after consumption to
    // invalidate it.
    slot = null;

    return element;
  }

}
