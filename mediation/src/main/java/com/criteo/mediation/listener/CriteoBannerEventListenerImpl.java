package com.criteo.mediation.listener;


import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.utils.CriteoErrorCode;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

public class CriteoBannerEventListenerImpl implements CriteoBannerAdListener {

    private CustomEventBannerListener customEventBannerListener;

    public CriteoBannerEventListenerImpl(CustomEventBannerListener listener) {
        customEventBannerListener = listener;
    }

    @Override
    public void onAdFetchSucceededForBanner() {

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
