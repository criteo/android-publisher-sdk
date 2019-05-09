package com.criteo.publisher.mediation.controller;

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

    private static final String DISPLAY_URL_MACRO = "%%displayUrl%%";

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
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                }
            });

            String displayUrlWithTag = Config.mediationAdTagUrl;
            String displayUrl = displayUrlWithTag.replace(DISPLAY_URL_MACRO, slot.getDisplayUrl());

            criteoBannerView.loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
        }


    }

}
