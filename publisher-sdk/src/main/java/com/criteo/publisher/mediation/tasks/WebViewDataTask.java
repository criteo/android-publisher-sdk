package com.criteo.publisher.mediation.tasks;

import static com.criteo.publisher.network.PubSdkApi.readStream;

import android.os.AsyncTask;
import android.text.TextUtils;
import com.criteo.publisher.model.WebviewData;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class WebViewDataTask extends AsyncTask<String, Void, String> {

    private WebviewData webviewData;

    public WebViewDataTask(WebviewData webviewData) {
        this.webviewData = webviewData;
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
                String response = readStream(urlConnection.getInputStream());
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

        webviewData.setContent(data);

    }

}
