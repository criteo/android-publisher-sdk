package com.criteo.publisher.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.criteo.publisher.R;
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.model.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

//TODO: Add unit tests
final class PubSdkApi {
    private static final int TIMEOUT = 60 * 1000;
    private static final String TAG = PubSdkApi.class.getSimpleName();
    private static final String NETWORK_ID = "networkId";
    private static final String APP_ID = "appId";
    private static final String SDK_VERSION = "sdkVersion";
    private static final String GAID = "gaid";
    private static final String EVENT_TYPE = "eventType";
    private static final String LIMITED_AD_TRACKING = "limitedAdTracking";

    private PubSdkApi() {
    }

    static Config loadConfig(Context context, int networkId, String appId, String sdkVersion) {

        Map<String, String> parameters = new HashMap<>();
        parameters.put(NETWORK_ID, String.valueOf(networkId));
        parameters.put(APP_ID, appId);
        parameters.put(SDK_VERSION, sdkVersion);

        Config configResult = null;
        try {
            URL url = new URL(context.getString(R.string.config_url) + "/v1.0/api/config" + "?" + getParamsString(parameters));
            JSONObject result = executeGet(url);
            configResult = new Config(result);
        } catch (IOException | JSONException e) {
            Log.d(TAG, "Unable to process request to remote config TLA:" + e.getMessage());
            e.printStackTrace();
        }
        return configResult;
    }

    static Cdb loadCdb(Context context, Cdb cdbRequest, String userAgent) {
        Cdb cdbResult = null;
        try {
            URL url = new URL(context.getString(R.string.cdb_url) + "/inapp/v1");
            JSONObject cdbRequestJson = cdbRequest.toJson();
            JSONObject result = executePost(url, cdbRequestJson, userAgent);
            cdbResult = new Cdb(result);
        } catch (IOException | JSONException e) {
            Log.d(TAG, "Unable to process request to Cdb:" + e.getMessage());
            e.printStackTrace();
        }
        return cdbResult;
    }

    static JSONObject postAppEvent(Context context, int senderId,
                                   String appId, String gaid, String eventType,
                                   int limitedAdTracking) {

        Map<String, String> parameters = new HashMap<>();
        parameters.put(APP_ID, appId);
        parameters.put(GAID, gaid);
        parameters.put(EVENT_TYPE, eventType);
        parameters.put(LIMITED_AD_TRACKING, String.valueOf(limitedAdTracking));
        try {
            URL url = new URL(context.getString(R.string.event_url) + "/appevent/v1/" + senderId + "?" + getParamsString(parameters));
            JSONObject result = executeGet(url);
            return result;
        } catch (IOException | JSONException e) {
            Log.d(TAG, "Unable to process request to post app event:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    private static JSONObject executePost(URL url, JSONObject requestJson, String userAgent) throws IOException, JSONException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setReadTimeout(TIMEOUT);
        urlConnection.setConnectTimeout(TIMEOUT);
        urlConnection.setRequestProperty("Content-Type", "text/plain");
        if (!TextUtils.isEmpty(userAgent)) {
            urlConnection.setRequestProperty("User-Agent", userAgent);
        }
        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();

        JSONObject result = new JSONObject();
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String response = readStream(urlConnection.getInputStream());
            if (!TextUtils.isEmpty(response)) {
                result = new JSONObject(response);
            }
        }
        return result;
    }

    private static JSONObject executeGet(URL url) throws IOException, JSONException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-Type", "text/plain");
        urlConnection.setReadTimeout(TIMEOUT);
        urlConnection.setConnectTimeout(TIMEOUT);
        JSONObject result = new JSONObject();
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String response = readStream(urlConnection.getInputStream());
            if (!TextUtils.isEmpty(response)) {
                result = new JSONObject(response);
            }
        }
        return result;
    }


    private static String readStream(InputStream in) throws IOException {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return response.toString();
    }

    private static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {

        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            queryString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()));
            queryString.append("=");
            queryString.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
            queryString.append("&");
        }

        // drop the last '&' if result is not empty
        return queryString.length() > 0
                ? queryString.substring(0, queryString.length() - 1)
                : queryString.toString();
    }
}
