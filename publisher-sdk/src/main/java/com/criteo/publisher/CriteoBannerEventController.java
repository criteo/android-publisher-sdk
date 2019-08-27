package com.criteo.publisher;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.tasks.CriteoBannerListenerCallTask;
import com.criteo.publisher.tasks.CriteoBannerLoadTask;
import java.lang.ref.WeakReference;


public class CriteoBannerEventController {

    private WeakReference<CriteoBannerView> view;
    private CriteoBannerLoadTask loadTask;
    private CriteoBannerAdListener adListener;
    private CriteoBannerListenerCallTask listenerCallTask;

    public CriteoBannerEventController(CriteoBannerView bannerView, CriteoBannerAdListener criteoBannerAdListener) {
        this.view = new WeakReference<>(bannerView);
        this.adListener = criteoBannerAdListener;
        bannerView.setCriteoBannerAdListener(criteoBannerAdListener);
    }

    public void fetchAdAsync(AdUnit adUnit) {
        Slot slot = null;
        if (adUnit != null) {
            slot = Criteo.getInstance().getBidForAdUnit(adUnit);
        }

        CriteoListenerCode code = CriteoListenerCode.INVALID;
        if (slot != null && slot.isValid() && URLUtil.isValidUrl(slot.getDisplayUrl())) {
            code = CriteoListenerCode.VALID;
        }

        listenerCallTask = new CriteoBannerListenerCallTask(this.adListener, view.get());
        listenerCallTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, code);

        if (CriteoListenerCode.VALID == code) {
            loadTask = new CriteoBannerLoadTask(view.get(), createWebViewClient());
            // Must run on UI thread as it is displaying the fetched ad
            loadTask.execute(slot);
        }
    }

    public void fetchAdAsync(BidToken bidToken) {
        TokenValue tokenValue = Criteo.getInstance().getTokenValue(bidToken, AdUnitType.CRITEO_BANNER);

        CriteoListenerCode code = CriteoListenerCode.INVALID;
        if (tokenValue != null && URLUtil.isValidUrl(tokenValue.getDisplayUrl())) {
            code = CriteoListenerCode.VALID;
        }

        listenerCallTask = new CriteoBannerListenerCallTask(this.adListener, view.get());
        listenerCallTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, code);

        if (CriteoListenerCode.VALID == code) {
            loadTask = new CriteoBannerLoadTask(view.get(), createWebViewClient());
            // Must run on UI thread as it is displaying the fetched ad
            loadTask.execute(tokenValue);
        }
    }

    // WebViewClient is created here to prevent passing the AdListener everywhere.
    // Setting this webViewClient to the WebView is done in the CriteoBannerLoadTask as all
    // WebView methods need to run in the same UI thread
    private WebViewClient createWebViewClient() {
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                if (adListener != null) {
                    CriteoBannerListenerCallTask listenerCallTask = new CriteoBannerListenerCallTask(adListener, null);
                    listenerCallTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CriteoListenerCode.CLICK);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
            }
        };
        return webViewClient;
    }

    protected void destroy() {
        if (loadTask != null) {
            loadTask.cancel(true);
        }
    }
}
