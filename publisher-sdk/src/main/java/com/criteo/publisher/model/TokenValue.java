package com.criteo.publisher.model;

import com.criteo.publisher.Util.AdUnitType;


public class TokenValue {

    private long tokenExpirationTime;
    private String displayUrl;
    private AdUnitType adUnitType;
    private static final int SECOND_TO_MILLI = 1000;

    public TokenValue(long bidTime, int bidTtl, String displayUrl, AdUnitType adUnitType) {
        this.tokenExpirationTime = bidTime + bidTtl * SECOND_TO_MILLI;
        this.displayUrl = displayUrl;
        this.adUnitType = adUnitType;
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
        return gettokenExpirationTime() < System.currentTimeMillis();
    }
}
