package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.Util.StreamUtil;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.WebViewData;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class WebViewDataTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "Criteo.WVDT";

    private WebViewData webviewData;

    @NonNull
    private final DeviceInfo deviceInfo;

    private CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

    public WebViewDataTask(WebViewData webviewData,
        @NonNull DeviceInfo deviceInfo,
        CriteoInterstitialAdDisplayListener adDisplayListener) {
        this.webviewData = webviewData;
        this.deviceInfo = deviceInfo;
        this.criteoInterstitialAdDisplayListener = adDisplayListener;
    }

    @Override
    protected String doInBackground(String... args) {
        String result = null;

        try {
            result = doWebViewDataTask(args);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal WVDT exec error.", tr);
        }

        return result;
    }

    private String doWebViewDataTask(String[] args) throws Exception {
        String displayUrl = args[0];

        String result = "";
        URL url;
        try {
            url = new URL(displayUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }

        String userAgent = deviceInfo.getUserAgent().get();
        HttpURLConnection urlConnection;
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
        if (!TextUtils.isEmpty(userAgent)) {
            urlConnection.setRequestProperty("User-Agent", userAgent);
        }

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
            webviewData.downloadFailed();
            if (criteoInterstitialAdDisplayListener != null) {
                criteoInterstitialAdDisplayListener.onAdFailedToDisplay(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
            }
            return;
        }
        webviewData.setContent(data);
        webviewData.downloadSucceeeded();
        if (criteoInterstitialAdDisplayListener != null) {
            criteoInterstitialAdDisplayListener.onAdReadyToDisplay();
        }
    }

}
