package com.criteo.publisher.model;


import com.criteo.publisher.Util.AdUnitType;

public final class InterstitialAdUnit extends AdUnit {

    public InterstitialAdUnit(String adUnitId) {
        super(adUnitId, AdUnitType.CRITEO_INTERSTITIAL);
    }

    public String getInterstitialAdUnitId() {
        return getAdUnitId();
    }


}
