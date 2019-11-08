package com.criteo.publisher.model;

import com.criteo.publisher.Util.ObjectsUtil;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_NATIVE;

public final class NativeAdUnit extends AdUnit {

    private final AdSize adSize;

    public NativeAdUnit(String adUnitId) {
        super(adUnitId, CRITEO_NATIVE);
        this.adSize = new AdSize(2, 2);
    }

    public AdSize getAdSize() {
        return this.adSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        NativeAdUnit that = (NativeAdUnit) o;
        return ObjectsUtil.equals(adSize, that.adSize);
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(super.hashCode(), adSize);
    }
}
