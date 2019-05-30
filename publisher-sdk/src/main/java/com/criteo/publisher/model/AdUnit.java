package com.criteo.publisher.model;

import com.criteo.publisher.Util.AdUnitType;

public abstract class AdUnit {

    private String adUnitId;
    private AdUnitType adUnitType;

    protected AdUnit(String adUnitId, AdUnitType adUnitType) {
        this.adUnitId = adUnitId;
        this.adUnitType = adUnitType;
    }

    protected String getAdUnitId() {
        return adUnitId;
    }

    AdUnitType getAdUnitType() {
        return adUnitType;
    }
}
