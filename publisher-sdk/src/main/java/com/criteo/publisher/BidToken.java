package com.criteo.publisher;

import java.util.UUID;

public class BidToken {

    private UUID tokenId;

    BidToken(UUID uuid) {
        this.tokenId = uuid != null ? uuid : UUID.randomUUID();
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
}
