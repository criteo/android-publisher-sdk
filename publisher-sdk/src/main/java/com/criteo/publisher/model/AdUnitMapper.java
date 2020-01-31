package com.criteo.publisher.model;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_CUSTOM_NATIVE;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_INTERSTITIAL;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.DeviceUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdUnitMapper {
    private static final String TAG = AdUnitMapper.class.getSimpleName();

    private final AndroidUtil androidUtil;
    private final DeviceUtil deviceUtil;

    public AdUnitMapper(AndroidUtil androidUtil, DeviceUtil deviceUtil) {
        this.androidUtil = androidUtil;
        this.deviceUtil = deviceUtil;
    }

    /**
     * Transform the given valid {@link AdUnit} into internal {@link CacheAdUnit}.
     * <p>
     * Valid ad units are transformed and collected while invalid ad units are ignored. See {@link
     * #map(AdUnit)} for validity rules.
     * <p>
     * Collected ad units are then grouped into chunks to load.
     *
     * @param adUnits to transform
     * @return chunks of internal ad unit representations
     */
    public List<List<CacheAdUnit>> mapToChunks(@NonNull List<AdUnit> adUnits) {
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
                    cacheAdUnits.add(createInterstitialAdUnits(interstitialAdUnit.getAdUnitId()));
                    break;

                case CRITEO_CUSTOM_NATIVE:
                    NativeAdUnit nativeAdUnit = (NativeAdUnit) adUnit;
                    cacheAdUnits.add(new CacheAdUnit(nativeAdUnit.getAdSize(), nativeAdUnit.getAdUnitId(), CRITEO_CUSTOM_NATIVE));
                    break;

                default:
                    throw new IllegalArgumentException("Found an invalid AdUnit");
            }
        }
        return splitIntoChunks(filterInvalidCacheAdUnits(cacheAdUnits));
    }

    /**
     * Transform the given {@link AdUnit} into an internal {@link CacheAdUnit} if valid
     * <p>
     * The given ad unit is considered valid if all those conditions are met:
     * <ul>
     *   <li>not null</li>
     *   <li>placement ID is not empty nor null</li>
     *   <li>width is strictly positive</li>
     *   <li>height is strictly positive</li>
     * </ul>
     * <p>
     * If the ad unit is not valid, then <code>null</code> is returned instead.
     *
     * @param adUnit to transform
     * @return internal ad unit representation or <code>null</code> if given ad unit is invalid
     */
    @Nullable
    public CacheAdUnit map(@Nullable AdUnit adUnit) {
        List<List<CacheAdUnit>> validAdUnits = mapToChunks(Collections.singletonList(adUnit));
        if (validAdUnits.isEmpty() || validAdUnits.get(0).isEmpty()) {
            return null;
        } else {
            return validAdUnits.get(0).get(0);
        }
    }

    private CacheAdUnit createInterstitialAdUnits(String placementId) {
        int orientation = androidUtil.getOrientation();
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return new CacheAdUnit(deviceUtil.getSizePortrait(), placementId, CRITEO_INTERSTITIAL);
        } else {
            return new CacheAdUnit(deviceUtil.getSizeLandscape(), placementId, CRITEO_INTERSTITIAL);
        }
    }

    private List<CacheAdUnit> filterInvalidCacheAdUnits(List<CacheAdUnit> cacheAdUnits) {
        List<CacheAdUnit> validatedCacheAdUnits = new ArrayList<>();

        for (CacheAdUnit cacheAdUnit : cacheAdUnits) {
            if (cacheAdUnit.getPlacementId() == null
                || cacheAdUnit.getPlacementId().isEmpty()
                || cacheAdUnit.getSize() == null
                || cacheAdUnit.getSize().getWidth() <= 0
                || cacheAdUnit.getSize().getHeight() <= 0) {
                Log.e(TAG, "Found an invalid AdUnit: " + cacheAdUnit);
                continue;
            }
            validatedCacheAdUnits.add(cacheAdUnit);
        }

        return validatedCacheAdUnits;
    }

    private <T> List<List<T>> splitIntoChunks(List<T> elements) {
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<T>> chunks = new ArrayList<>();
        chunks.add(elements);
        return chunks;
    }

}
