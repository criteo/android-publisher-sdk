package com.criteo.publisher.model;


import java.util.Objects;

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
        return Objects.equals(adSize, that.adSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), adSize);
    }
}
