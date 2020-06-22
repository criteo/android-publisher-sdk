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

import com.criteo.publisher.util.AdUnitType;

public final class CacheAdUnit {

  private final String adUnitId;
  private final AdSize adSize;
  private final AdUnitType adUnitType;

  public CacheAdUnit(AdSize adSize, String adUnitId, AdUnitType adUnitType) {
    this.adSize = adSize;
    this.adUnitId = adUnitId;
    this.adUnitType = adUnitType;
  }

  public String getPlacementId() {
    return adUnitId;
  }

  public AdUnitType getAdUnitType() {
    return adUnitType;
  }

  public AdSize getSize() {
    return adSize;
  }

  @Override
  public String toString() {
    return "CacheAdUnit{" +
        "placementId='" + adUnitId + '\'' +
        ", adSize=" + adSize +
        ", adUnitType= " + adUnitType +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CacheAdUnit that = (CacheAdUnit) o;

    if (adUnitId != null ? !adUnitId.equals(that.adUnitId) : that.adUnitId != null) {
      return false;
    }
    if (adSize != null ? !adSize.equals(that.adSize) : that.adSize != null) {
      return false;
    }
    return adUnitType == that.adUnitType;
  }

  @Override
  public int hashCode() {
    int result = adUnitId != null ? adUnitId.hashCode() : 0;
    result = 31 * result + (adSize != null ? adSize.hashCode() : 0);
    result = 31 * result + (adUnitType != null ? adUnitType.hashCode() : 0);
    return result;
  }

}
