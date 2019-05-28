package com.criteo.publisher.mediation.controller;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.tasks.CriteoBannerListenerCallTask;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Slot;


public class CriteoBannerEventController {

    private CriteoBannerView criteoBannerView;
    private CriteoBannerListenerCallTask criteoBannerFetchTask;
    private CriteoBannerAdListener criteoBannerAdListener;

    public CriteoBannerEventController(CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = bannerView;
        this.criteoBannerAdListener = listener;
        bannerView.setCriteoBannerAdListener(listener);
    }

    public void fetchAdAsync(AdUnit adUnit) {
        Slot slot = Criteo.getInstance().getBidForAdUnit(adUnit);

        criteoBannerFetchTask = new CriteoBannerListenerCallTask(criteoBannerView, criteoBannerAdListener);
        criteoBannerFetchTask.execute(slot);

        if (slot != null) {
            criteoBannerView.getSettings().setJavaScriptEnabled(true);
            criteoBannerView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    criteoBannerAdListener.onAdClicked();
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                }
            });

            String displayUrlWithTag = Config.MEDIATION_AD_TAG_URL;
            String displayUrl = displayUrlWithTag.replace(Config.DISPLAY_URL_MACRO, slot.getDisplayUrl());
            criteoBannerView.loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
        }


    }

}
