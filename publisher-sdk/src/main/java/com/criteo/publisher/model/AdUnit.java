package com.criteo.publisher.model;

import com.criteo.publisher.Util.AdUnitType;

import java.util.Objects;

public abstract class AdUnit {

    private final String adUnitId;
    private final AdUnitType adUnitType;

    protected AdUnit(String adUnitId, AdUnitType adUnitType) {
        this.adUnitId = adUnitId;
        this.adUnitType = adUnitType;
    }

    public String getAdUnitId() {
        return adUnitId;
    }

    public AdUnitType getAdUnitType() {
        return adUnitType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdUnit adUnit = (AdUnit) o;
        return Objects.equals(adUnitId, adUnit.adUnitId) &&
                adUnitType == adUnit.adUnitType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(adUnitId, adUnitType);
    }
}
