package com.criteo.publisher.model;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_NATIVE;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_INTERSTITIAL;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.publisher.Util.DeviceUtil;
import java.util.ArrayList;
import java.util.List;

public final class AdUnitHelper {

    private static final String TAG = AdUnitHelper.class.getSimpleName();

    private AdUnitHelper() {

    }

    public static List<CacheAdUnit> convertAdUnits(Context context, List<AdUnit> adUnits) {
        List<CacheAdUnit> cacheAdUnits = new ArrayList<>();
        for (AdUnit adUnit : adUnits) {
            if (adUnit == null) {
                continue;
            }
            switch (adUnit.getAdUnitType()) {
                case CRITEO_BANNER:
                    BannerAdUnit bannerAdUnit = (BannerAdUnit) adUnit;
                    cacheAdUnits.add(new CacheAdUnit(bannerAdUnit.getSize(), bannerAdUnit.getAdUnitId(), CRITEO_BANNER));
                    break;

                case CRITEO_INTERSTITIAL:
                    InterstitialAdUnit interstitialAdUnit = (InterstitialAdUnit) adUnit;
                    cacheAdUnits.addAll(createInterstitialAdUnits(context, interstitialAdUnit.getAdUnitId()));
                    break;

                case CRITEO_NATIVE:
                    NativeAdUnit nativeAdUnit = (NativeAdUnit) adUnit;
                    cacheAdUnits.add(new CacheAdUnit(nativeAdUnit.getAdSize(), nativeAdUnit.getAdUnitId(), CRITEO_NATIVE));
                    break;

                default:
                    throw new IllegalArgumentException("Found an invalid AdUnit");
            }
        }
        return cacheAdUnits;
    }

    private static List<CacheAdUnit> createInterstitialAdUnits(Context context, String placementId) {
        List<CacheAdUnit> retCacheAdUnits = new ArrayList<>();
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            CacheAdUnit interstitialCacheAdUnitPortrait = new CacheAdUnit(DeviceUtil.getSizePortrait(), placementId, CRITEO_INTERSTITIAL);
            retCacheAdUnits.add(interstitialCacheAdUnitPortrait);
        } else {
            CacheAdUnit interstitialCacheAdUnitLandscape = new CacheAdUnit(DeviceUtil.getSizeLandscape(), placementId, CRITEO_INTERSTITIAL);
            retCacheAdUnits.add(interstitialCacheAdUnitLandscape);
        }
        return retCacheAdUnits;
    }

    public static CacheAdUnit convertoCacheAdUnit(AdUnit adUnit, int orientation) {
        switch (adUnit.getAdUnitType()) {
            case CRITEO_BANNER:
                BannerAdUnit bannerAdUnit = (BannerAdUnit) adUnit;
                return new CacheAdUnit(bannerAdUnit.getSize(), bannerAdUnit.getAdUnitId(), CRITEO_BANNER);

            case CRITEO_INTERSTITIAL:
                AdSize adSize;
                InterstitialAdUnit interstitialAdUnit = (InterstitialAdUnit) adUnit;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    adSize = DeviceUtil.getSizeLandscape();
                } else {
                    adSize = DeviceUtil.getSizePortrait();
                }
                return new CacheAdUnit(adSize, interstitialAdUnit.getAdUnitId(), CRITEO_INTERSTITIAL);

            case CRITEO_NATIVE:
                NativeAdUnit nativeAdUnit = (NativeAdUnit) adUnit;
                return new CacheAdUnit(nativeAdUnit.getAdSize(), nativeAdUnit.getAdUnitId(), CRITEO_NATIVE);

            default:
                throw new IllegalArgumentException("Found an invalid AdUnit");
        }
    }

    public static List<CacheAdUnit> filterInvalidCacheAdUnits(List<CacheAdUnit> cacheAdUnits) {
        List<CacheAdUnit> validatedCacheAdUnits = new ArrayList<CacheAdUnit>();

        for (CacheAdUnit cacheAdUnit : cacheAdUnits) {
            if (TextUtils.isEmpty(cacheAdUnit.getPlacementId()) || cacheAdUnit.getSize() == null
                    || cacheAdUnit.getSize().getWidth() <= 0 || cacheAdUnit.getSize().getHeight() <= 0) {
                Log.e(TAG, "Found an invalid AdUnit: " + cacheAdUnit);
                continue;
            }
            validatedCacheAdUnits.add(cacheAdUnit);
        }

        return validatedCacheAdUnits;
    }

}
