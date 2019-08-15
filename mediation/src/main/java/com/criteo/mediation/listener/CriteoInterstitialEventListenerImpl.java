package com.criteo.mediation.listener;


import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

public class CriteoInterstitialEventListenerImpl implements CriteoInterstitialAdListener,
        CriteoInterstitialAdDisplayListener {

    private CustomEventInterstitialListener customEventInterstitialListener;

    public CriteoInterstitialEventListenerImpl(CustomEventInterstitialListener listener) {
        customEventInterstitialListener = listener;
    }

    @Override
    public void onAdReceived() {

    }

    @Override
    public void onAdFailedToReceive(CriteoErrorCode code) {
        switch (code) {
            case ERROR_CODE_INTERNAL_ERROR:
                customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                break;
            case ERROR_CODE_NETWORK_ERROR:
                customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
                break;
            case ERROR_CODE_INVALID_REQUEST:
                customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
                break;
            case ERROR_CODE_NO_FILL:
                customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                break;
        }
    }

    @Override
    public void onAdOpened() {
        customEventInterstitialListener.onAdOpened();
    }

    @Override
    public void onAdClosed() {
        customEventInterstitialListener.onAdClosed();
    }

    @Override
    public void onAdLeftApplication() {
        customEventInterstitialListener.onAdLeftApplication();
    }

    @Override
    public void onAdReadyToDisplay() {
        customEventInterstitialListener.onAdLoaded();
    }

    @Override
    public void onAdFailedToDisplay(CriteoErrorCode code) {
        customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
    }
}
