package com.criteo.publisher;

public interface CriteoNativeAdListener extends CriteoAdListener {

    /**
     * Called when an ad is successfully fetched.
     */
    void onAdReceived(CriteoNativeAd nativeAd);

    /**
     * Called when the ad's impression pixels have been fired.
     */
    void onAdImpression();
}
