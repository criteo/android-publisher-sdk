package com.criteo.publisher.model;


import com.criteo.publisher.util.AdUnitType;

public final class InterstitialAdUnit extends AdUnit {

  public InterstitialAdUnit(String adUnitId) {
    super(adUnitId, AdUnitType.CRITEO_INTERSTITIAL);
  }

}
