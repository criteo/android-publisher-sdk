package com.criteo.publisher.network;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.publisher.R;
import com.criteo.publisher.Util.StreamUtil;
import com.criteo.publisher.model.Cdb;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

//TODO: Add unit tests
public class PubSdkApi {

    private static final int TIMEOUT_IN_MILLIS = 60 * 1000;
    private static final String TAG = PubSdkApi.class.getSimpleName();
    private static final String CRITEO_PUBLISHER_ID = "cpId";
    private static final String APP_ID = "appId";
    private static final String SDK_VERSION = "sdkVersion";
    private static final String GAID = "gaid";
    private static final String EVENT_TYPE = "eventType";
    private static final String LIMITED_AD_TRACKING = "limitedAdTracking";

    private final Context context;

    public PubSdkApi(Context context) {
        this.context = context;
    }

    public JSONObject loadConfig(String criteoPublisherId, String appId, String sdkVersion) {

        Map<String, String> parameters = new HashMap<>();
        parameters.put(CRITEO_PUBLISHER_ID, criteoPublisherId);
        parameters.put(APP_ID, appId);
        parameters.put(SDK_VERSION, sdkVersion);

        JSONObject configResult = null;
        try {
            URL url = new URL(
                    context.getString(R.string.config_url) + "/v2.0/api/config" + "?" + getParamsString(parameters));
            configResult = executeGet(url);
        } catch (IOException | JSONException e) {
            Log.d(TAG, "Unable to process request to remote config TLA:" + e.getMessage());
            e.printStackTrace();
        }
        return configResult;
    }

    @Nullable
    public Cdb loadCdb(Cdb cdbRequest, String userAgent) {
        Cdb cdbResult = null;
        try {
            URL url = new URL(context.getString(R.string.cdb_url) + "/inapp/v2");
            JSONObject cdbRequestJson = cdbRequest.toJson();
            JSONObject result = executePost(url, cdbRequestJson, userAgent);
            cdbResult = new Cdb(result);
        } catch (IOException | JSONException e) {
            Log.d(TAG, "Unable to process request to Cdb:" + e.getMessage());
            e.printStackTrace();
        }
        return cdbResult;
    }

    JSONObject postAppEvent(int senderId,
        String appId, String gaid, String eventType,
        int limitedAdTracking) {

        Map<String, String> parameters = new HashMap<>();
        parameters.put(APP_ID, appId);

        // If device doesnt support Playservices , gaid value stays as null
        if (gaid != null) {
            parameters.put(GAID, gaid);
        }

        parameters.put(EVENT_TYPE, eventType);
        parameters.put(LIMITED_AD_TRACKING, String.valueOf(limitedAdTracking));
        try {
            URL url = new URL(
                    context.getString(R.string.event_url) + "/appevent/v1/" + senderId + "?" + getParamsString(
                            parameters));
            JSONObject result = executeGet(url);
            return result;
        } catch (IOException | JSONException e) {
            Log.d(TAG, "Unable to process request to post app event:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject executePost(URL url, JSONObject requestJson, String userAgent)
            throws IOException, JSONException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setReadTimeout(TIMEOUT_IN_MILLIS);
        urlConnection.setConnectTimeout(TIMEOUT_IN_MILLIS);
        urlConnection.setRequestProperty("Content-Type", "text/plain");
        if (!TextUtils.isEmpty(userAgent)) {
            urlConnection.setRequestProperty("User-Agent", userAgent);
        }
        try {
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(requestJson.toString().getBytes(Charset.forName("UTF-8")));
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        JSONObject result = new JSONObject();
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String response = StreamUtil.readStream(urlConnection.getInputStream());
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
        urlConnection.setReadTimeout(TIMEOUT_IN_MILLIS);
        urlConnection.setConnectTimeout(TIMEOUT_IN_MILLIS);
        JSONObject result = new JSONObject();
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String response = StreamUtil.readStream(urlConnection.getInputStream());
            if (!TextUtils.isEmpty(response)) {
                result = new JSONObject(response);
            }
        }
        return result;
    }

    private static String getParamsString(Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                queryString.append(URLEncoder.encode(entry.getKey(), Charset.forName("UTF-8").name()));
                queryString.append("=");
                queryString.append(URLEncoder.encode(entry.getValue(), Charset.forName("UTF-8").name()));
                queryString.append("&");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        // drop the last '&' if result is not empty
        return queryString.length() > 0
                ? queryString.substring(0, queryString.length() - 1)
                : queryString.toString();
    }

}
