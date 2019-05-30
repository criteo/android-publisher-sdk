package com.criteo.publisher.model;

import android.content.res.Configuration;
import com.criteo.publisher.Util.BannerAdUnit;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.InterstitialAdUnit;
import java.util.ArrayList;
import java.util.List;

public final class AdUnitHelper {

    private AdUnitHelper() {

    }

    public static List<CacheAdUnit> convertAdUnits(List<AdUnit> adUnits) {
        List<CacheAdUnit> cacheAdUnits = new ArrayList<>();
        for (AdUnit adUnit : adUnits) {
            switch (adUnit.getAdUnitType()) {
                case CRITEO_BANNER:
                    BannerAdUnit bannerAdUnit = (BannerAdUnit) adUnit;
                    cacheAdUnits.add(new CacheAdUnit(bannerAdUnit.getAdSize(), bannerAdUnit.getBannerAdUnitId()));
                    break;
                case CRITEO_INTERSTITIAL:
                    InterstitialAdUnit interstitialAdUnit = (InterstitialAdUnit) adUnit;
                    cacheAdUnits.addAll(createInterstitialAdUnits(interstitialAdUnit.getInterstitialAdUnitId()));
                    break;
                default:
                    throw new IllegalArgumentException("Found an invalid AdUnit");
            }
        }
        return cacheAdUnits;
    }

    private static List<CacheAdUnit> createInterstitialAdUnits(String placementId) {
        List<CacheAdUnit> retCacheAdUnits = new ArrayList<>();

        CacheAdUnit interstitialCacheAdUnitPortrait = new CacheAdUnit(DeviceUtil.getSizePortrait(), placementId);
        retCacheAdUnits.add(interstitialCacheAdUnitPortrait);

        CacheAdUnit interstitialCacheAdUnitLandscape = new CacheAdUnit(DeviceUtil.getSizeLandscape(), placementId);
        retCacheAdUnits.add(interstitialCacheAdUnitLandscape);

        return retCacheAdUnits;
    }

    public static CacheAdUnit convertoCacheAdUnit(AdUnit adUnit, int orientation) {
        switch (adUnit.getAdUnitType()) {
            case CRITEO_BANNER:
                BannerAdUnit bannerAdUnit = (BannerAdUnit) adUnit;
                return new CacheAdUnit(bannerAdUnit.getAdSize(), bannerAdUnit.getBannerAdUnitId());
            case CRITEO_INTERSTITIAL:
                AdSize adSize;
                InterstitialAdUnit interstitialAdUnit = (InterstitialAdUnit) adUnit;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    adSize = DeviceUtil.getSizeLandscape();
                } else {
                    adSize = DeviceUtil.getSizePortrait();
                }
                return new CacheAdUnit(adSize, interstitialAdUnit.getInterstitialAdUnitId());
            default:
                throw new IllegalArgumentException("Found an invalid AdUnit");
        }
    }

}
