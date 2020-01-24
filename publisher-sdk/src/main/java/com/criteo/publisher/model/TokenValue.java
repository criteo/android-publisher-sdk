package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import com.criteo.publisher.Clock;
import com.criteo.publisher.Util.AdUnitType;

public class TokenValue {
    private static final int SECOND_TO_MILLI = 1000;

    private final long tokenExpirationTime;

    @NonNull
    private final String displayUrl;

    @NonNull
    private final AdUnitType adUnitType;

    @NonNull
    private final Clock clock;

    public TokenValue(
        long bidTime,
        int bidTtl,
        @NonNull String displayUrl,
        @NonNull AdUnitType adUnitType,
        @NonNull Clock clock) {
        this.tokenExpirationTime = bidTime + bidTtl * SECOND_TO_MILLI;
        this.displayUrl = displayUrl;
        this.adUnitType = adUnitType;
        this.clock = clock;
    }

    @NonNull
    public AdUnitType getAdUnitType() {
        return adUnitType;
    }

    public long gettokenExpirationTime() {
        return tokenExpirationTime;
    }

    @NonNull
    public String getDisplayUrl() {
        return displayUrl;
    }

    public boolean isExpired() {
        return gettokenExpirationTime() < clock.getCurrentTimeInMillis();
    }
}
