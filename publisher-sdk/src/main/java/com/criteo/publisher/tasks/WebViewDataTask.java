package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.Util.StreamUtil;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.model.WebViewData;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class WebViewDataTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "Criteo.WVDT";

    private WebViewData webviewData;
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    public WebViewDataTask(WebViewData webviewData, CriteoInterstitialAdListener listener) {
        this.webviewData = webviewData;
        this.criteoInterstitialAdListener = listener;
    }


    @Override
    protected String doInBackground(String... urls) {
        String result = null;

        try {
            result = doWebViewDataTask(urls);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal WVDT exec error.", tr);
        }

        return result;
    }

    private String doWebViewDataTask(String[] urls) {
        String result = "";
        URL url = null;
        try {
            if (urls.length > 0 && !URLUtil.isValidUrl(urls[0])) {
                return "";
            }
            url = new URL(urls[0]);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            return "";
        }
        try {
            urlConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        urlConnection.setRequestProperty("Content-Type", "text/plain");

        try {
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = StreamUtil.readStream(urlConnection.getInputStream());
                if (!TextUtils.isEmpty(response)) {
                    result = (response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    @Override
    protected void onPostExecute(String data) {
        try {
            doOnPostExecute(data);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal WVDT PostExec error.", tr);
        }
    }

    private void doOnPostExecute(String data) {
        if (TextUtils.isEmpty(data)) {
            if(criteoInterstitialAdListener != null) {
                criteoInterstitialAdListener.onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
            }
            webviewData.downloadFailed();
            return;
        }
        webviewData.setContent(data, criteoInterstitialAdListener);
        webviewData.downloadSucceeeded();
        if(criteoInterstitialAdListener != null) {
            criteoInterstitialAdListener.onAdLoaded();
        }
    }

}
