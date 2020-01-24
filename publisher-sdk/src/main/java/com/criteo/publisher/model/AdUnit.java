package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.ObjectsUtil;

public abstract class AdUnit {

    private final String adUnitId;

    @NonNull
    private final AdUnitType adUnitType;

    protected AdUnit(String adUnitId, @NonNull AdUnitType adUnitType) {
        this.adUnitId = adUnitId;
        this.adUnitType = adUnitType;
    }

    public String getAdUnitId() {
        return adUnitId;
    }

    @NonNull
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
        return ObjectsUtil.equals(adUnitId, adUnit.adUnitId) &&
                adUnitType == adUnit.adUnitType;
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(adUnitId, adUnitType);
    }
}
