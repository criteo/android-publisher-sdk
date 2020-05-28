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
