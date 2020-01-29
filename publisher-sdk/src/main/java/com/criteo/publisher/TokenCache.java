package com.criteo.publisher;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.TokenValue;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public class TokenCache {

    private final Map<BidToken, TokenValue> tokenMap;

    public TokenCache() {
        tokenMap = new Hashtable<>();
    }

    public BidToken add(TokenValue tokenValue, AdUnit adUnit) {
        BidToken bidToken = new BidToken(UUID.randomUUID(), adUnit);
        tokenMap.put(bidToken, tokenValue);
        return bidToken;
    }

    @Nullable
    public TokenValue getTokenValue(@Nullable BidToken bidToken, @NonNull AdUnitType adUnitType) {
        if (bidToken == null) {
            return null;
        }

        if (tokenMap.containsKey(bidToken)) {
            TokenValue tokenValue = tokenMap.get(bidToken);
            if (tokenValue.isExpired()) {
                tokenMap.remove(bidToken);
                return null;
            }
            if (tokenValue.getAdUnitType() != adUnitType) {
                return null;
            }
            tokenMap.remove(bidToken);
            return tokenValue;
        } else {
            return null;
        }
    }

}
