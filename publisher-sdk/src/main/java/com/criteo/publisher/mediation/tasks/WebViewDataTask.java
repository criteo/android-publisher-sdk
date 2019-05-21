package com.criteo.publisher.mediation.tasks;


import android.os.AsyncTask;
import android.text.TextUtils;
import com.criteo.publisher.Util.StreamUtil;
import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.utils.CriteoErrorCode;
import com.criteo.publisher.model.WebViewData;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class WebViewDataTask extends AsyncTask<String, Void, String> {

    private WebViewData webviewData;
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    public WebViewDataTask(WebViewData webviewData, CriteoInterstitialAdListener listener) {
        this.webviewData = webviewData;
        this.criteoInterstitialAdListener = listener;
    }


    @Override
    protected String doInBackground(String... urls) {
        String result = "";
        URL url = null;
        try {
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
        if (TextUtils.isEmpty(data)) {
            criteoInterstitialAdListener.onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
            return;
        } else {
            criteoInterstitialAdListener.onAdFetchSucceededForInterstitial();
        }

        webviewData.setContent(data, criteoInterstitialAdListener);

    }

}
