package com.criteo.publisher.Util;


import com.criteo.publisher.model.AdUnit;

public final class InterstitialAdUnit extends AdUnit {

    public InterstitialAdUnit(String adUnitId) {
        super(adUnitId, AdUnitType.CRITEO_INTERSTITIAL);
    }

    public String getInterstitialAdUnitId() {
        return getAdUnitId();
    }


}
