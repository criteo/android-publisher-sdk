package com.criteo.pubsdk_android.listener;

import android.util.Log;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;

public class TestAppMoPubInterstitialAdListener implements InterstitialAdListener {

    private final String tag;
    private MoPubInterstitial mInterstitial;

    public TestAppMoPubInterstitialAdListener(String tag, MoPubInterstitial mInterstitial) {
        this.tag = tag;
        this.mInterstitial = mInterstitial;
    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        Log.d(tag, "Mopub ad loaded");
        mInterstitial.show();

    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        // Code to be executed when an ad request fails
        Log.d(tag, "Mopub ad failed:" + errorCode);
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        Log.d(tag, "ad shown");
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        Log.d(tag, "Mopub ad clicked");

    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        Log.d(tag, "Mopub ad dismissed");

    }
}
