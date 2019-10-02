package com.criteo.publisher.model;


import static com.criteo.publisher.Util.AdUnitType.CRITEO_NATIVE;

public class NativeAdUnit extends AdUnit {

    private AdSize adSize;

    public NativeAdUnit(String adUnitId) {
        super(adUnitId, CRITEO_NATIVE);
        this.adSize = new AdSize(2, 2);
    }

    public AdSize getAdSize() {
        return this.adSize;
    }
}
