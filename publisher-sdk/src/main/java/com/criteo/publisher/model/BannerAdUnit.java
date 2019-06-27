package com.criteo.publisher.model;

import com.criteo.publisher.Util.AdUnitType;

public final class BannerAdUnit extends AdUnit {

    private final AdSize adSize;

    public BannerAdUnit(String adUnitId, AdSize size) {
        super(adUnitId, AdUnitType.CRITEO_BANNER);
        this.adSize = size;
    }

    public AdSize getSize() {
        return adSize;
    }
}
