package com.criteo.publisher.mediation.listeners;

public interface CriteoInterstitialAdListener extends CriteoAdListener {

    /**
     * Called when an ad is successfully fetched.
     */
    public void onAdFetchSucceededForInterstitial();

}

