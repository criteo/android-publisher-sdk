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

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.model.AbstractTokenValue;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.util.AdUnitType;
import com.criteo.publisher.util.ObjectUtils;

public class BidResponse {

  @SuppressWarnings("ConstantConditions")
  public static final BidResponse NO_BID = new BidResponse(0, false, null);

  private final double price;

  private final boolean valid;

  private final AdUnitType adUnitType;

  protected BidResponse(
      double price,
      boolean valid,
      @NonNull AdUnitType adUnitType
  ) {
    this.price = price;
    this.valid = valid;
    this.adUnitType = adUnitType;
  }

  @Keep
  public double getPrice() {
    return price;
  }

  @Keep
  public boolean isBidSuccess() {
    return valid;
  }

  public AdUnitType getAdUnitType() {
    return adUnitType;
  }

}
