package com.criteo.mediation.listener;

import android.view.View;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;
import com.mopub.mobileads.MoPubErrorCode;

public class MopubBannerListenerImpl implements CriteoBannerAdListener {

    private CustomEventBannerListener customEventBannerListener;

    public MopubBannerListenerImpl(CustomEventBannerListener listener) {
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
}
