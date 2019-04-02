package com.criteo.mediation.adapter;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.criteo.publisher.mediation.controller.CriteoBannerEventController;
import com.criteo.publisher.mediation.controller.CriteoInterstitialEventController;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.model.AdUnit;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;
import com.google.android.gms.ads.mediation.customevent.CustomEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CriteoAdapter implements CustomEventBanner, CustomEventInterstitial {

    protected static final String TAG = CriteoAdapter.class.getSimpleName();

    protected static final String NETWORKID = "networkId";
    protected static final String ADUNIT = "adUnit";
    protected static final String PLACEMENTID = "placementid";
    protected static final String WIDTH = "width";
    protected static final String HEIGHT = "height";

    private CriteoBannerEventController bannerEventController;
    private CriteoInterstitialEventController interstitialEventController;
    private List<AdUnit> adUnits;


    /**
     * The event is being destroyed. Perform any necessary cleanup here.
     */
    @Override
    public void onDestroy() {
//        if (bannerEventController != null) {
//            bannerEventController.criteoBannerView.destroy();
//        }
    }

    /**
     * The app is being paused. This call will only be forwarded to the adapter if the developer
     * notifies mediation that the app is being paused.
     */
    @Override
    public void onPause() {
        // The sample ad network doesn't have an onPause method, so it does nothing.
    }

    /**
     * The app is being resumed. This call will only be forwarded to the adapter if the developer
     * notifies mediation that the app is being resumed.
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
        /*
         * 1. Initialize Criteo
         * 2. Create your banner view.
         * 3. Set your ad network's listener.
         * 4. Make an ad request.
         *
         * When setting your ad network's listener, don't forget to send the following callbacks:
         *
         * listener.onAdLoaded(this);
         * listener.onAdFailedToLoad(this, AdRequest.ERROR_CODE_*);
         * listener.onAdClicked(this);
         * listener.onAdOpened(this);
         * listener.onAdLeftApplication(this);
         * listener.onAdClosed(this);
         */

        // Initialize Criteo
        if (serverParameter != null) {
            initializeCriteo(serverParameter, context, listener);
        } else
            listener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);


    }

    @Override
    public void requestInterstitialAd(Context context,
                                      CustomEventInterstitialListener listener,
                                      String serverParameter,
                                      MediationAdRequest mediationAdRequest,
                                      Bundle customEventExtras) {
        /*
         * In this method, you should:
         *
         * 1. Create your interstitial ad.
         * 2. Set your ad network's listener.
         * 3. Make an ad request.
         *
         * When setting your ad network's listener, don't forget to send the following callbacks:
         *
         * listener.onAdLoaded(this);
         * listener.onAdFailedToLoad(this, AdRequest.ERROR_CODE_*);
         * listener.onAdOpened(this);
         * listener.onAdLeftApplication(this);
         * listener.onAdClosed(this);
         */

        // Initialize Criteo
        if (serverParameter != null) {
            initializeCriteo(serverParameter, context, listener);
        } else
            listener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
    }

    private void initializeCriteo(String serverParameter, Context context, CustomEventListener listener) {
        int networkId = 0;
        String placementId = "";
        int width = 0;
        int height = 0;
        JSONObject parameters = new JSONObject();
        if (!TextUtils.isEmpty(serverParameter)) {
            try {
                parameters = new JSONObject(serverParameter);
                if (parameters.get(NETWORKID) != null)
                    networkId = parameters.getInt(NETWORKID);
                if (parameters.get(ADUNIT) != null) {
                    JSONObject adUnit = parameters.getJSONObject(ADUNIT);
                    placementId = adUnit.getString(PLACEMENTID);
                    width = adUnit.getInt(WIDTH);
                    height = adUnit.getInt(HEIGHT);
                }

                this.adUnits = new ArrayList<>();
                AdUnit adUnit = new AdUnit();
                adUnit.setPlacementId(placementId);
                com.criteo.publisher.model.AdSize adSize = new com.criteo.publisher.model.AdSize();
                adSize.setWidth(width);
                adSize.setHeight(height);
                adUnit.setSize(adSize);
                adUnits.add(adUnit);

                Criteo.init((Application) (context.getApplicationContext()), adUnits, networkId);

            } catch (JSONException e) {
                listener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                e.printStackTrace();
            }
        }

    }

    @Override
    public void showInterstitial() {
        // Show your interstitial ad.
        // interstitialEventController.show();
    }

}
