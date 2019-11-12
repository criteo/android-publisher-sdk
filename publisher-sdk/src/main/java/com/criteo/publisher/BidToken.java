package com.criteo.publisher;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.model.AdUnit;
import java.util.UUID;

public final class BidToken {

    @NonNull
    private final UUID tokenId;

    @Nullable
    private final AdUnit adUnit;

    BidToken(UUID uuid, @Nullable AdUnit adUnit) {
        this.tokenId = uuid != null ? uuid : UUID.randomUUID();
        this.adUnit = adUnit;
    }

    @Nullable
    public AdUnit getAdUnit() {
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
