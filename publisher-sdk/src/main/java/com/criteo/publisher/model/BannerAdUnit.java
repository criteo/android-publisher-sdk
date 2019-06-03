package com.criteo.publisher.model;

import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.AdSize;

public final class BannerAdUnit extends AdUnit {

    private final AdSize adSize;

    public BannerAdUnit(String adUnitId, AdSize adSize) {
        super(adUnitId, AdUnitType.CRITEO_BANNER);
        this.adSize = adSize;
    }

    public AdSize getAdSize() {
        return adSize;
    }

    public String getBannerAdUnitId() {
        return getAdUnitId();
    }
}
