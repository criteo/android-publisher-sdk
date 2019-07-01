package com.criteo.publisher;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.mediation.tasks.CriteoBannerListenerCallTask;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;


public class CriteoBannerEventController {

    private CriteoBannerView criteoBannerView;
    private CriteoBannerListenerCallTask criteoBannerListenerCallTask;
    private CriteoBannerAdListener criteoBannerAdListener;

    public CriteoBannerEventController(CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = bannerView;
        this.criteoBannerAdListener = listener;
        bannerView.setCriteoBannerAdListener(listener);
        setWebViewClient();
    }

    private void setWebViewClient() {
        criteoBannerView.getSettings().setJavaScriptEnabled(true);
        criteoBannerView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                if(criteoBannerAdListener != null) {
                    criteoBannerAdListener.onAdLeftApplication();
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
            }
        });
    }

    public void fetchAdAsync(AdUnit adUnit) {
        Slot slot = Criteo.getInstance().getBidForAdUnit(adUnit);

        criteoBannerListenerCallTask = new CriteoBannerListenerCallTask(criteoBannerView, criteoBannerAdListener);
        criteoBannerListenerCallTask.execute(slot);

        if (slot != null && slot.isValid()) {
            loadWebview(slot.getDisplayUrl());
        }
    }

    private void loadWebview(String url) {
        String displayUrlWithTag = Config.getAdTagUrlMode();
        String displayUrl = displayUrlWithTag.replace(Config.getDisplayUrlMacro(), url);
        criteoBannerView.loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
    }

    public void fetchAdAsync(BidToken bidToken) {
        TokenValue tokenValue = Criteo.getInstance().getTokenValue(bidToken, AdUnitType.CRITEO_BANNER);

        criteoBannerListenerCallTask = new CriteoBannerListenerCallTask(criteoBannerView, criteoBannerAdListener);
        criteoBannerListenerCallTask.execute(tokenValue);

        if (tokenValue != null) {
            loadWebview(tokenValue.getDisplayUrl());
        }
    }


}
