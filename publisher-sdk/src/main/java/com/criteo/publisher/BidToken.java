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
import com.criteo.publisher.model.AdUnit;
import java.util.UUID;

public final class BidToken {
  @NonNull
  private final UUID tokenId;

  @NonNull
  private final AdUnit adUnit;

  BidToken(UUID uuid, @NonNull AdUnit adUnit) {
    this.tokenId = uuid != null ? uuid : UUID.randomUUID();
    this.adUnit = adUnit;
  }

  @NonNull
  AdUnit getAdUnit() {
    return adUnit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BidToken bidToken = (BidToken) o;

    if (!tokenId.equals(bidToken.tokenId)) {
      return false;
    }
    return adUnit != null ? adUnit.equals(bidToken.adUnit) : bidToken.adUnit == null;
  }

  @Override
  public int hashCode() {
    int result = tokenId.hashCode();
    result = 31 * result + (adUnit != null ? adUnit.hashCode() : 0);
    return result;
  }

}
