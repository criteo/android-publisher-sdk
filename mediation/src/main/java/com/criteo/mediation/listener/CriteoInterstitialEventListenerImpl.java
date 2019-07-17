package com.criteo.mediation.listener;


import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.view.CriteoInterstitial;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

public class CriteoInterstitialEventListenerImpl implements CriteoInterstitialAdListener {

    private CustomEventInterstitialListener customEventInterstitialListener;
    private CriteoInterstitial criteoInterstitial;

    public CriteoInterstitialEventListenerImpl(CustomEventInterstitialListener listener,
            CriteoInterstitial interstitialView) {
        customEventInterstitialListener = listener;
        criteoInterstitial = interstitialView;
    }

    @Override
    public void onAdLoaded() {
        customEventInterstitialListener.onAdLoaded();
    }

    @Override
    public void onAdFailedToLoad(CriteoErrorCode code) {
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

}
