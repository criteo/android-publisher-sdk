package com.criteo.publisher.mediation.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.criteo.publisher.BidToken;
import com.criteo.publisher.CriteoInterstitialEventController;
import com.criteo.publisher.Util.CriteoResultReceiver;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.controller.WebViewDownloader;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.WebViewData;

public class CriteoInterstitialView {
    private static final String TAG = CriteoInterstitialView.class.getSimpleName();

    private InterstitialAdUnit interstitialAdUnit;

    private Context context;

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private CriteoInterstitialEventController criteoInterstitialEventController;


    public CriteoInterstitialView(Context context, InterstitialAdUnit interstitialAdUnit) {
        this.context = context;
        this.interstitialAdUnit = interstitialAdUnit;
    }

    public void setCriteoInterstitialAdListener(CriteoInterstitialAdListener criteoInterstitialAdListener) {
        this.criteoInterstitialAdListener = criteoInterstitialAdListener;

    }

    public void loadAd() {
        try {
            doLoadAd();
        } catch (Throwable tr) {
            Log.e(TAG, "Internal error while loading interstitial.", tr);
        }
    }

    private void doLoadAd() {
        if (criteoInterstitialEventController == null) {
            criteoInterstitialEventController = new CriteoInterstitialEventController(
                    criteoInterstitialAdListener, new WebViewDownloader(new WebViewData()));
        }
        criteoInterstitialEventController.fetchAdAsync(interstitialAdUnit);
    }

    public void loadAd(BidToken bidToken) {
        try {
            doLoadAd(bidToken);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal error while loading interstitial from bid token.", tr);
        }
    }

    private void doLoadAd(BidToken bidToken) {
        if (criteoInterstitialEventController == null) {
            criteoInterstitialEventController = new CriteoInterstitialEventController(
                    criteoInterstitialAdListener, new WebViewDownloader(new WebViewData()));
        }
        criteoInterstitialEventController.fetchAdAsync(bidToken);
    }

    public boolean isAdLoaded() {
        boolean isAdLoaded = false;

        try {
            isAdLoaded = criteoInterstitialEventController.isAdLoaded();
        } catch (Throwable tr) {
            Log.e(TAG, "Internal error while detecting interstitial load state.", tr);
        }

        return isAdLoaded;
    }

    public void show() {
        try {
            doShow();
        } catch (Throwable tr) {
            Log.e(TAG, "Internal error while showing interstitial.", tr);
        }
    }

    private void doShow() {
        if (isAdLoaded()) {
            Intent intent = new Intent(context, CriteoInterstitialActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = new Bundle();
            bundle.putString("webviewdata", criteoInterstitialEventController.getWebViewDataContent());
            CriteoResultReceiver criteoResultReceiver = new CriteoResultReceiver(new Handler(),
                    criteoInterstitialAdListener);
            bundle.putParcelable("resultreceiver", criteoResultReceiver);
            intent.putExtras(bundle);
            if (criteoInterstitialAdListener != null) {
                criteoInterstitialAdListener.onAdOpened();
            }
            if (criteoInterstitialEventController != null) {
                criteoInterstitialEventController.refresh();
            }
            context.startActivity(intent);
        }
    }


}
