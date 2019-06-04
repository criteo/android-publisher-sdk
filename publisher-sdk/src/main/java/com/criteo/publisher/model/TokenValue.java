package com.criteo.publisher.model;

import com.criteo.publisher.Util.AdUnitType;

class TokenValue {

    private long tokenExpirationTime;
    private String displayUrl;
    private AdUnitType adUnitType;

    TokenValue(long bidTime, int bidTtl, String displayUrl, AdUnitType adUnitType) {
        this.tokenExpirationTime = bidTime + bidTtl;
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
}
