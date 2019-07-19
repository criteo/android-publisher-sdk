package com.criteo.publisher;

public interface CriteoInterstitialAdListener extends CriteoAdListener {

    /**
     * Called when an ad goes full screen.
     */
    void onAdOpened();

    /**
     * Called when an ad is closed.
     */
    void onAdClosed();

    /**
     * Called when an ad is successfully fetched.
     */
    void onAdReceived();

}

