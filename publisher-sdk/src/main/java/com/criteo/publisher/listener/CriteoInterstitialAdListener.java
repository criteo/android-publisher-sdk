package com.criteo.publisher.listener;

public interface CriteoInterstitialAdListener extends CriteoAdListener {

    /**
     * Called when an ad is successfully fetched.
     */
    public void onAdFetchSucceeded();

}

