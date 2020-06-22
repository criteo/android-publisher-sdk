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

import static com.criteo.publisher.util.AdUnitType.CRITEO_CUSTOM_NATIVE;

import com.criteo.publisher.util.ObjectsUtil;

public final class NativeAdUnit extends AdUnit {

  private final AdSize adSize;

  public NativeAdUnit(String adUnitId) {
    super(adUnitId, CRITEO_CUSTOM_NATIVE);
    this.adSize = new AdSize(2, 2);
  }

  public AdSize getAdSize() {
    return this.adSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    NativeAdUnit that = (NativeAdUnit) o;
    return ObjectsUtil.equals(adSize, that.adSize);
  }

  @Override
  public int hashCode() {
    return ObjectsUtil.hash(super.hashCode(), adSize);
  }
}
