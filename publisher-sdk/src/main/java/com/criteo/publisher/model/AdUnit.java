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
import com.criteo.publisher.util.AdUnitType;
import com.criteo.publisher.util.ObjectsUtil;

public abstract class AdUnit {

  private final String adUnitId;

  @NonNull
  private final AdUnitType adUnitType;

  protected AdUnit(String adUnitId, @NonNull AdUnitType adUnitType) {
    this.adUnitId = adUnitId;
    this.adUnitType = adUnitType;
  }

  public String getAdUnitId() {
    return adUnitId;
  }

  @NonNull
  public AdUnitType getAdUnitType() {
    return adUnitType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdUnit adUnit = (AdUnit) o;
    return ObjectsUtil.equals(adUnitId, adUnit.adUnitId) &&
        adUnitType == adUnit.adUnitType;
  }

  @Override
  public int hashCode() {
    return ObjectsUtil.hash(adUnitId, adUnitType);
  }
}
