package com.criteo.publisher.model;

import java.util.Hashtable;
import java.util.UUID;

public class TokenCache {

    private Hashtable<BidToken, TokenValue> tokenMap;

    public TokenCache() {
        tokenMap = new Hashtable<>();
    }

    public BidToken add(TokenValue tokenValue) {
        BidToken bidToken = new BidToken(UUID.randomUUID());
        tokenMap.put(bidToken, tokenValue);
        return bidToken;
    }

    public TokenValue getTokenValue(BidToken bidToken) {
        if (bidToken == null) {
            return null;
        }

        if (tokenMap.containsKey(bidToken)) {
            TokenValue tokenValue = tokenMap.get(bidToken);
            tokenMap.remove(bidToken);
            return tokenValue;
        } else {
            return null;
        }
    }

}
