package com.criteo.mediation.listener;


import android.view.View;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

public class CriteoBannerEventListenerImpl implements CriteoBannerAdListener {

    private CustomEventBannerListener customEventBannerListener;
    private CriteoBannerView criteoBannerView;

    public CriteoBannerEventListenerImpl(CustomEventBannerListener listener, CriteoBannerView bannerView) {
        customEventBannerListener = listener;
        criteoBannerView = bannerView;
    }

    @Override
    public void onAdReceived(View view) {
        customEventBannerListener.onAdLoaded(view);
    }

    @Override
    public void onAdFailedToReceive(CriteoErrorCode code) {
        switch (code) {
            case ERROR_CODE_INTERNAL_ERROR:
                customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                break;
            case ERROR_CODE_NETWORK_ERROR:
                customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
                break;
            case ERROR_CODE_INVALID_REQUEST:
                customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
                break;
            case ERROR_CODE_NO_FILL:
                customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                break;
        }
    }

    @Override
    public void onAdLeftApplication() {
        customEventBannerListener.onAdLeftApplication();
    }

}
