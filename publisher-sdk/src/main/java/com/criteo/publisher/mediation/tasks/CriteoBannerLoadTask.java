package com.criteo.publisher.mediation.tasks;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;


public class CriteoBannerLoadTask extends AsyncTask<Object, Void, Object> {
    private static final String TAG = "Criteo.BLT";

    private CriteoBannerView criteoBannerView;
    private CriteoBannerAdListener criteoBannerAdListener;

    public CriteoBannerLoadTask(CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = bannerView;
        this.criteoBannerAdListener = listener;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        Object result = null;

        try {
            result = doBannerLoadTask(objects);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal BLT exec error.", tr);
        }

        return result;
    }

    private Object doBannerLoadTask(Object[] objects) {
        if (objects == null || objects.length == 0) {
            return null;
        }
        Object object = objects[0];
        return object;
    }

    @Override
    protected void onPostExecute(Object object) {
        try {
            doOnPostExecute(object);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal BLT PostExec error.", tr);
        }
    }

    private void doOnPostExecute(Object object) {
        super.onPostExecute(object);
        if (object == null) {
            if (criteoBannerAdListener != null) {
                criteoBannerAdListener.onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
            }
            return;
        }

        createWebViewClient();

        if (object instanceof Slot) {
            Slot slot = (Slot) object;
            if (!slot.isValid()) {
                if (criteoBannerAdListener != null) {
                    criteoBannerAdListener.onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
                }
            } else {
                loadWebview(slot.getDisplayUrl());
                if (criteoBannerAdListener != null) {
                    criteoBannerAdListener.onAdLoaded(criteoBannerView);
                }
            }
        } else if (object instanceof TokenValue) {
            TokenValue tokenValue = (TokenValue) object;
            if (tokenValue == null || TextUtils.isEmpty(tokenValue.getDisplayUrl())) {
                if (criteoBannerAdListener != null) {
                    criteoBannerAdListener.onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
                }
            } else {
                loadWebview(tokenValue.getDisplayUrl());
                if (criteoBannerAdListener != null) {
                    criteoBannerAdListener.onAdLoaded(criteoBannerView);
                }
            }
        }
    }

    private void loadWebview(String url) {
        String displayUrlWithTag = Config.getAdTagUrlMode();
        String displayUrl = displayUrlWithTag.replace(Config.getDisplayUrlMacro(), url);
        criteoBannerView.loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
    }

    private void createWebViewClient() {
        criteoBannerView.getSettings().setJavaScriptEnabled(true);
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                if (criteoBannerAdListener != null) {
                    criteoBannerAdListener.onAdLeftApplication();
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
            }
        };
        criteoBannerView.setWebViewClient(webViewClient);
    }
}
