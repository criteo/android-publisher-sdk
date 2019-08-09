package com.criteo.mediation.mopubadapter;


import static com.mopub.common.logging.MoPubLog.AdLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_FAILED;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import com.criteo.mediation.listener.MopubInterstitialListenerImpl;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoInitException;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CriteoMopubInterstitialAdapter extends CustomEventInterstitial {

    private static final String TAG = CriteoMopubInterstitialAdapter.class.getSimpleName();
    protected static final String ADUNIT_ID = "adUnitId";
    protected static final String CRITEO_PUBLISHER_ID = "cpId";
    private CriteoInterstitial criteoInterstitial;

    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener,
            Map<String, Object> localExtras, Map<String, String> serverExtras) {

        if (TextUtils.isEmpty(serverExtras.toString())) {
            MoPubLog.log(LOAD_FAILED, TAG, "Empty parameters");
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        String criteoPublisherId = serverExtras.get(CRITEO_PUBLISHER_ID);

        if (criteoPublisherId == null) {
            MoPubLog.log(LOAD_FAILED, TAG, "Publised Id is null");
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        String adUnitId = serverExtras.get(ADUNIT_ID);

        if (adUnitId == null) {
            MoPubLog.log(LOAD_FAILED, TAG, "Missing Adunit Id");
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
            return;
        }

        try {
            Criteo.getInstance();
            InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(adUnitId);
            criteoInterstitial = new CriteoInterstitial(context, interstitialAdUnit);
            MopubInterstitialListenerImpl listener = new MopubInterstitialListenerImpl(customEventInterstitialListener);
            criteoInterstitial.setCriteoInterstitialAdListener(listener);
            criteoInterstitial.loadAd();
            MoPubLog.log(LOAD_ATTEMPTED, TAG, "Criteo Interstitial is loading");
        } catch (Exception e) {
            List<AdUnit> adUnits = new ArrayList<>();

            try {
                Criteo.init((Application) (context.getApplicationContext()), criteoPublisherId, adUnits);
            } catch (CriteoInitException e1) {
            }
            MoPubLog.log(LOAD_FAILED, TAG, "Unable to request interstitial. Initialization failed");
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    protected void showInterstitial() {
        if (criteoInterstitial != null) {
            criteoInterstitial.show();
        }
    }

    @Override
    protected void onInvalidate() {

    }
}
