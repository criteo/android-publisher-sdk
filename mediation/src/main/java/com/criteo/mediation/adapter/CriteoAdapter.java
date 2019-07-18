package com.criteo.mediation.adapter;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.mediation.listener.CriteoInterstitialEventListenerImpl;
import com.criteo.mediation.model.FormatType;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoBannerEventController;
import com.criteo.publisher.CriteoInitException;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialEventController;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class CriteoAdapter implements CustomEventBanner, CustomEventInterstitial {

    protected static final String TAG = CriteoAdapter.class.getSimpleName();

    protected static final String CRITEO_PUBLISHER_ID = "cpid";
    protected static final String ADUNITID = "adUnitId";

    private CriteoBannerEventController bannerEventController;
    private CriteoInterstitialEventController interstitialEventController;
    private CriteoInterstitial criteoInterstitial;
    private BannerAdUnit bannerAdUnit;
    private InterstitialAdUnit interstitialAdUnit;


    /**
     * The event is being destroyed. Perform any necessary cleanup here.
     */
    @Override
    public void onDestroy() {
    }

    /**
     * The app is being paused. This call will only be forwarded to the adapter if the developer notifies mediation that
     * the app is being paused.
     */
    @Override
    public void onPause() {
        // The sample ad network doesn't have an onPause method, so it does nothing.
    }

    /**
     * The app is being resumed. This call will only be forwarded to the adapter if the developer notifies mediation
     * that the app is being resumed.
     */
    @Override
    public void onResume() {
        // The sample ad network doesn't have an onResume method, so it does nothing.
    }

    @Override
    public void requestBannerAd(Context context,
            CustomEventBannerListener listener,
            String serverParameter,
            AdSize size,
            MediationAdRequest mediationAdRequest,
            Bundle customEventExtras) {

        // Initialize Criteo
        if (serverParameter == null) {
            //  initialize(serverParameter, context, listener);
        } else {
            listener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
        }


    }

    @Override
    public void requestInterstitialAd(Context context,
            CustomEventInterstitialListener listener,
            String serverParameter,
            MediationAdRequest mediationAdRequest,
            Bundle customEventExtras) {

        if (TextUtils.isEmpty(serverParameter)) {
            listener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
            return;
        }

        try {

            if (initialize(context, serverParameter, null, FormatType.BANNER)) {
                criteoInterstitial = new CriteoInterstitial(context, interstitialAdUnit);
                CriteoInterstitialEventListenerImpl criteoInterstitialEventListener = new CriteoInterstitialEventListenerImpl(
                        listener, criteoInterstitial);
                criteoInterstitial.setCriteoInterstitialAdListener(criteoInterstitialEventListener);

                criteoInterstitial.loadAd();
            } else {
                listener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
            }

        } catch (JSONException | CriteoInitException e) {
            listener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
            Log.e(TAG, "Adapter initialization error");
        }

    }

    private boolean initialize(Context context, String serverParameter, AdSize size,
            FormatType formatType)
            throws JSONException, CriteoInitException {
        JSONObject parameters = new JSONObject(serverParameter);
        String criteoPublisherId = parameters.getString(CRITEO_PUBLISHER_ID);
        String adUnitId = parameters.getString(ADUNITID);
        List<AdUnit> adUnits = new ArrayList<>();
        if (formatType == FormatType.BANNER) {
            bannerAdUnit = new BannerAdUnit(adUnitId,
                    new com.criteo.publisher.model.AdSize(size.getWidth(), size.getHeight()));
            adUnits.add(bannerAdUnit);
        } else if (formatType == FormatType.INTERSTITIAL) {
            interstitialAdUnit = new InterstitialAdUnit(adUnitId);
            adUnits.add(interstitialAdUnit);
        }

        try {
            Criteo.getInstance();
            return true;
        } catch (Exception ex) {
            Criteo.init((Application) context, criteoPublisherId, adUnits);
            return false;
        }
    }

    @Override
    public void showInterstitial() {
        // Show your interstitial ad
        if (criteoInterstitial != null && criteoInterstitial.isAdLoaded()) {
            criteoInterstitial.show();
        }
    }

}
