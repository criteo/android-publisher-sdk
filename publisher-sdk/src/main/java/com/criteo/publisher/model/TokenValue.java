package com.criteo.publisher.model;

import com.criteo.publisher.Clock;
import com.criteo.publisher.Util.AdUnitType;

public class TokenValue {
    private static final int SECOND_TO_MILLI = 1000;

    private final long tokenExpirationTime;
    private final String displayUrl;
    private final AdUnitType adUnitType;
    private final Clock clock;

    public TokenValue(long bidTime, int bidTtl, String displayUrl, AdUnitType adUnitType, Clock clock) {
        this.tokenExpirationTime = bidTime + bidTtl * SECOND_TO_MILLI;
        this.displayUrl = displayUrl;
        this.adUnitType = adUnitType;
        this.clock = clock;
    }

    public AdUnitType getAdUnitType() {
        return adUnitType;
    }

    public long gettokenExpirationTime() {
        return tokenExpirationTime;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public boolean isExpired() {
        return gettokenExpirationTime() < clock.getCurrentTimeInMillis();
    }
}
