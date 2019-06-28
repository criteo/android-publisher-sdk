package com.criteo.publisher.listener;

import android.view.View;

public interface CriteoInterstitialAdListener extends CriteoAdListener {

    /**
     * Called when an ad is successfully fetched.
     */
    void onAdLoaded();

}

