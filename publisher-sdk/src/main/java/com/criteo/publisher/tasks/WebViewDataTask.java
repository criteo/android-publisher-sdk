package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.Util.StreamUtil;
import com.criteo.publisher.model.WebViewData;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class WebViewDataTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "Criteo.WVDT";

    private WebViewData webviewData;

    private CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

    public WebViewDataTask(WebViewData webviewData, CriteoInterstitialAdDisplayListener adDisplayListener) {
        this.webviewData = webviewData;
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

    private String doWebViewDataTask(String[] args) {
        String result = "";
        URL url = null;
        try {
            if (args.length > 0 && !URLUtil.isValidUrl(args[0])) {
                return "";
            }
            url = new URL(args[0]);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String webViewUserAgent = args[1];
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
        if (!TextUtils.isEmpty(webViewUserAgent)) {
            urlConnection.setRequestProperty("User-Agent", webViewUserAgent);
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
                criteoInterstitialAdDisplayListener.onAdFailedToDisplay();
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
