package com.criteo.publisher.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.criteo.publisher.Util.StreamUtil;
import com.criteo.publisher.Util.TextUtils;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.RemoteConfigRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class PubSdkApi {

  private static final int TIMEOUT_IN_MILLIS = 60 * 1000;
  private static final String TAG = PubSdkApi.class.getSimpleName();
  private static final String CRITEO_PUBLISHER_ID = "cpId";
  private static final String APP_ID = "appId";
  private static final String SDK_VERSION = "sdkVersion";
  private static final String GAID = "gaid";
  private static final String EVENT_TYPE = "eventType";
  private static final String LIMITED_AD_TRACKING = "limitedAdTracking";

  private final NetworkConfiguration networkConfiguration;

  public PubSdkApi(NetworkConfiguration networkConfiguration) {
    this.networkConfiguration = networkConfiguration;
  }

  @Nullable
  public JSONObject loadConfig(@NonNull RemoteConfigRequest request) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(CRITEO_PUBLISHER_ID, request.getCriteoPublisherId());
    parameters.put(APP_ID, request.getBundleId());
    parameters.put(SDK_VERSION, request.getSdkVersion());

    JSONObject configResult = null;
    try {
      URL url = new URL(networkConfiguration.getRemoteConfigUrl() + "/v2.0/api/config" + "?" + getParamsString(
              parameters));
      configResult = executeGet(url, null);
    } catch (IOException | JSONException e) {
      Log.d(TAG, "Unable to process request to remote config TLA:" + e.getMessage());
      e.printStackTrace();
    }
    return configResult;
  }

  @NonNull
  public CdbResponse loadCdb(@NonNull CdbRequest cdbRequest, @NonNull String userAgent) throws Exception {
    URL url = new URL(networkConfiguration.getCdbUrl() + "/inapp/v2");
    JSONObject cdbRequestJson = cdbRequest.toJson();
    JSONObject result = executePost(url, cdbRequestJson, userAgent);
    return CdbResponse.fromJson(result);
  }

  @Nullable
  public JSONObject postAppEvent(
      int senderId,
      @NonNull String appId,
      @Nullable String gaid,
      @NonNull String eventType,
      int limitedAdTracking,
      @NonNull String userAgent) {

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
          networkConfiguration.getEventUrl() + "/appevent/v1/" + senderId + "?"
              + getParamsString(
              parameters));
      return executeGet(url, userAgent);
    } catch (IOException | JSONException e) {
      Log.d(TAG, "Unable to process request to post app event:" + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  @Nullable
  public InputStream executeRawGet(URL url) throws IOException {
    return executeRawGet(url, null);
  }

  @NonNull
  private InputStream executeRawGet(URL url, @Nullable String userAgent) throws IOException {
    HttpURLConnection urlConnection = prepareConnection(url, userAgent, "GET");
    return readResponseStreamIfSuccess(urlConnection);
  }

  @NonNull
  private static JSONObject executePost(
      @NonNull URL url,
      @NonNull JSONObject requestJson,
      @NonNull String userAgent)
      throws IOException, JSONException {
    HttpURLConnection urlConnection = prepareConnection(url, userAgent, "POST");
    writePayload(urlConnection, requestJson);

    try (InputStream inputStream = readResponseStreamIfSuccess(urlConnection)) {
      return readJson(inputStream);
    }
  }

  private JSONObject executeGet(URL url, @Nullable String userAgent) throws IOException, JSONException {
    try (InputStream inputStream = executeRawGet(url, userAgent)) {
      return readJson(inputStream);
    }
  }

  @NonNull
  private static HttpURLConnection prepareConnection(@NonNull URL url,
      @Nullable String userAgent, String method) throws IOException {
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.setRequestMethod(method);
    urlConnection.setReadTimeout(TIMEOUT_IN_MILLIS);
    urlConnection.setConnectTimeout(TIMEOUT_IN_MILLIS);
    urlConnection.setRequestProperty("Content-Type", "text/plain");
    if (!TextUtils.isEmpty(userAgent)) {
      urlConnection.setRequestProperty("User-Agent", userAgent);
    }
    return urlConnection;
  }

  @NonNull
  private static InputStream readResponseStreamIfSuccess(@NonNull HttpURLConnection urlConnection) throws IOException {
    int status = urlConnection.getResponseCode();
    if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_NO_CONTENT) {
      return urlConnection.getInputStream();
    } else {
      throw new HttpResponseException(status);
    }
  }

  @NonNull
  private static JSONObject readJson(@NonNull InputStream inputStream) throws IOException, JSONException {
    String response = StreamUtil.readStream(inputStream);
    if (!TextUtils.isEmpty(response)) {
      return new JSONObject(response);
    }
    return new JSONObject();
  }

  private static void writePayload(
      @NonNull HttpURLConnection urlConnection,
      @NonNull JSONObject requestJson) throws IOException {
        byte[] payload = requestJson.toString().getBytes(Charset.forName("UTF-8"));

    urlConnection.setDoOutput(true);
    try (OutputStream outputStream = urlConnection.getOutputStream()) {
      outputStream.write(payload);
      outputStream.flush();
    }
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
