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
import com.criteo.publisher.util.ObjectUtils;
import java.util.Arrays;

public final class BannerAdUnit extends AdUnit {

  private final AdSize adSize;

  public BannerAdUnit(String adUnitId, AdSize size) {
    super(adUnitId, AdUnitType.CRITEO_BANNER);
    this.adSize = size;
  }

  public AdSize getSize() {
    return adSize;
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
    BannerAdUnit that = (BannerAdUnit) o;
    return ObjectUtils.equals(adSize, that.adSize);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object [] { super.hashCode(), adSize });
  }
}
