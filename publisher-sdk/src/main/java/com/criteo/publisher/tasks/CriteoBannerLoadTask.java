package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebViewClient;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import java.lang.ref.WeakReference;


public class CriteoBannerLoadTask extends AsyncTask<Object, Void, Object> {

    private static final String TAG = "Criteo.BLT";

    private final WeakReference<CriteoBannerView> weakReferenceBannerView;
    private WebViewClient webViewClient;

    /**
     * Taking WebViewClient as a constructor as all WebView/CriteoBannerView methods must be called on the same UI
     * thread. WebView.getSettings().setJavaScriptEnabled() & WebView.setWebViewClient() throws if not done in the
     * onPostExecute() as onPostExecute runs on the UI thread
     */
    public CriteoBannerLoadTask(CriteoBannerView bannerView, WebViewClient webViewClient) {
        this.weakReferenceBannerView = new WeakReference<>(bannerView);
        this.webViewClient = webViewClient;
    }

    @Override
    // Caller must pass Slot/TokenValue
    protected Object doInBackground(Object... objects) {
        try {
            if (objects == null || objects.length == 0) {
                return null;
            }

            return objects[0];
        } catch (Throwable tr) {
            Log.e(TAG, "Internal BLT exec error.", tr);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object object) {
        try {
            if (object instanceof Slot) {
                Slot slot = (Slot) object;
                loadWebview(slot.getDisplayUrl());
            } else if (object instanceof TokenValue) {
                TokenValue tokenValue = (TokenValue) object;
                loadWebview(tokenValue.getDisplayUrl());
            }
        } catch (Throwable tr) {
            Log.e(TAG, "Internal BLT exec error.", tr);
        }
    }

    private void loadWebview(String url) {
        CriteoBannerView criteoBannerView = weakReferenceBannerView.get();
        if (criteoBannerView != null) {
            criteoBannerView.getSettings().setJavaScriptEnabled(true);
            criteoBannerView.setWebViewClient(this.webViewClient);
            String displayUrlWithTag = Config.getAdTagUrlMode();
            String displayUrl = displayUrlWithTag.replace(Config.getDisplayUrlMacro(), url);
            criteoBannerView.loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
        }
    }
}
