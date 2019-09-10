package com.criteo.publisher.model;


import static com.criteo.publisher.Util.AdUnitType.CRITEO_CUSTOM_NATIVE;

public class NativeAdUnit extends AdUnit {

    private AdSize adSize;

    protected NativeAdUnit(String adUnitId) {
        super(adUnitId, CRITEO_CUSTOM_NATIVE);
        this.adSize = new AdSize(2, 2);
    }

    public void setAdSize(AdSize size) {
        this.adSize = size;
    }

    public AdSize getAdSize() {
        return this.adSize;
    }
}
