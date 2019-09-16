package com.criteo.publisher;

import com.criteo.publisher.model.AdUnit;
import java.util.UUID;

public class BidToken {

    private UUID tokenId;
    private AdUnit adUnit;

    BidToken(UUID uuid, AdUnit adUnit) {
        this.tokenId = uuid != null ? uuid : UUID.randomUUID();
        this.adUnit = adUnit;
    }

    @Override
    public int hashCode() {
        return tokenId.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BidToken) {
            return this.tokenId.equals(((BidToken) other).tokenId);
        }
        return false;
    }

    public AdUnit getAdUnit() {
        return adUnit;
    }
}
