package com.criteo.publisher;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.util.AdUnitType;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.TokenValue;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenCache {

  private final Map<BidToken, TokenValue> tokenMap;

  public TokenCache() {
    tokenMap = new ConcurrentHashMap<>();
  }

  public BidToken add(@NonNull TokenValue tokenValue, @NonNull AdUnit adUnit) {
    BidToken bidToken = new BidToken(UUID.randomUUID(), adUnit);
    tokenMap.put(bidToken, tokenValue);
    return bidToken;
  }

  @Nullable
  public TokenValue getTokenValue(@Nullable BidToken bidToken, @NonNull AdUnitType expectedType) {
    if (bidToken == null || bidToken.getAdUnit().getAdUnitType() != expectedType) {
      return null;
    }

    TokenValue tokenValue = tokenMap.remove(bidToken);
    if (tokenValue == null || tokenValue.isExpired()) {
      return null;
    }

    return tokenValue;
  }

}
