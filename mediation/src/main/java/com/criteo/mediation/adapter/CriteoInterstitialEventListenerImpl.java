package com.criteo.mediation.adapter;


import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.utils.CriteoErrorCode;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

public class CriteoInterstitialEventListenerImpl implements CriteoInterstitialAdListener {

    private CustomEventInterstitialListener customEventInterstitialListener;

    public CriteoInterstitialEventListenerImpl(CustomEventInterstitialListener listener) {
        customEventInterstitialListener = listener;
    }

    @Override
    public void onAdFetchSucceededForInterstitial() {

    }

    @Override
    public void onAdFetchFailed(CriteoErrorCode code) {

    }

    @Override
    public void onAdFullScreen() {

    }

    @Override
    public void onAdClosed() {

    }
}
