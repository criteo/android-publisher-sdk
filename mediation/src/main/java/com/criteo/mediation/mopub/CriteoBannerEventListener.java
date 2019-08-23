package com.criteo.mediation.mopub;

import android.view.View;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoErrorCode;
import com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;
import com.mopub.mobileads.MoPubErrorCode;

public class CriteoBannerEventListener implements CriteoBannerAdListener {

    private CustomEventBannerListener customEventBannerListener;

    public CriteoBannerEventListener(CustomEventBannerListener listener) {
        customEventBannerListener = listener;
    }

    @Override
    public void onAdReceived(View view) {
        customEventBannerListener.onBannerLoaded(view);
    }

    @Override
    public void onAdFailedToReceive(CriteoErrorCode code) {
        switch (code) {
            case ERROR_CODE_INTERNAL_ERROR:
                customEventBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                break;
            case ERROR_CODE_NETWORK_ERROR:
                customEventBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_TIMEOUT);
                break;
            case ERROR_CODE_INVALID_REQUEST:
                customEventBannerListener.onBannerFailed(MoPubErrorCode.SERVER_ERROR);
                break;
            case ERROR_CODE_NO_FILL:
                customEventBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
                break;
        }
    }

    @Override
    public void onAdLeftApplication() {
        customEventBannerListener.onLeaveApplication();
    }

    @Override
    public void onAdClicked() {
        customEventBannerListener.onBannerClicked();
    }

    @Override
    public void onAdOpened() {

    }

    @Override
    public void onAdClosed() {

    }
}
