package com.criteo.mediation.listener;

import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import com.mopub.mobileads.MoPubErrorCode;

public class MopubInterstitialListenerImpl implements CriteoInterstitialAdListener {

    private CustomEventInterstitialListener customEventInterstitialListener;
    private CriteoInterstitial criteoInterstitial;

    public MopubInterstitialListenerImpl(CustomEventInterstitialListener listener) {
        customEventInterstitialListener = listener;
    }

    @Override
    public void onAdOpened() {
        customEventInterstitialListener.onInterstitialShown();
    }

    @Override
    public void onAdClosed() {
        customEventInterstitialListener.onInterstitialDismissed();
    }

    @Override
    public void onAdReceived() {
        customEventInterstitialListener.onInterstitialLoaded();
    }

    @Override
    public void onAdFailedToReceive(CriteoErrorCode code) {
        switch (code) {
            case ERROR_CODE_INTERNAL_ERROR:
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                break;
            case ERROR_CODE_NETWORK_ERROR:
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_TIMEOUT);
                break;
            case ERROR_CODE_INVALID_REQUEST:
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.SERVER_ERROR);
                break;
            case ERROR_CODE_NO_FILL:
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
                break;
        }
    }

    @Override
    public void onAdLeftApplication() {
        customEventInterstitialListener.onLeaveApplication();
    }
}