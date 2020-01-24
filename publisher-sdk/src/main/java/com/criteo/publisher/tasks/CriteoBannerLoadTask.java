package com.criteo.publisher.tasks;

import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.model.Config;
import java.lang.ref.Reference;

public class CriteoBannerLoadTask implements Runnable {

    private static final String TAG = "Criteo.BLT";

    @NonNull
    private final Reference<? extends WebView> webViewRef;

    @NonNull
    private final Config config;

    @NonNull
    private final WebViewClient webViewClient;

    @NonNull
    private final String displayUrl;

    /**
     * Taking WebViewClient as a constructor as all WebView/CriteoBannerView methods must be called on the same UI
     * thread. WebView.getSettings().setJavaScriptEnabled() & WebView.setWebViewClient() throws if not done in the
     * onPostExecute() as onPostExecute runs on the UI thread
     */
    public CriteoBannerLoadTask(
        @NonNull Reference<? extends WebView> webViewRef,
        @NonNull WebViewClient webViewClient,
        @NonNull Config config,
        @NonNull String displayUrl) {
        this.webViewRef = webViewRef;
        this.webViewClient = webViewClient;
        this.config = config;
        this.displayUrl = displayUrl;
    }

    @Override
    public void run() {
        try {
            loadWebview();
        } catch (Throwable tr) {
            Log.e(TAG, "Internal BLT exec error.", tr);
        }
    }

    private void loadWebview() {
        WebView webView = webViewRef.get();
        if (webView != null) {
            String finalDisplayUrl = computeFinalDisplayUrl();

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(this.webViewClient);
            webView.loadDataWithBaseURL("", finalDisplayUrl, "text/html", "UTF-8", "");
        }
    }

    @NonNull
    private String computeFinalDisplayUrl() {
        String displayUrlWithTag = config.getAdTagUrlMode();
        return displayUrlWithTag.replace(config.getDisplayUrlMacro(), displayUrl);
    }
}
