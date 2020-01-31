package com.criteo.pubsdk_android.listener;

import android.util.Log;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;

public class TestAppInterstitialAdDisplayListener implements CriteoInterstitialAdDisplayListener {

    private final String tag;
    private final String prefix;

    public TestAppInterstitialAdDisplayListener(String tag, String prefix) {
        this.tag = tag;
        this.prefix = prefix;
    }

    @Override
    public void onAdReadyToDisplay() {
        Log.d(tag, prefix + "Interstitial ad called onAdReadyToDisplay");
    }

    @Override
    public void onAdFailedToDisplay(CriteoErrorCode code) {
        Log.d(tag, prefix + "Interstitial ad called onAdFailedToDisplay");
    }
}
