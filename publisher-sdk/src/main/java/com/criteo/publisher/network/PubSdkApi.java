/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.config.RemoteConfigRequest;
import com.criteo.publisher.config.RemoteConfigResponse;
import com.criteo.publisher.csm.MetricRequest;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.logging.RemoteLogRecords;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.JsonSerializer;
import com.criteo.publisher.util.StreamUtil;
import com.criteo.publisher.util.TextUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class PubSdkApi {

  private static final String APP_ID = "appId";
  private static final String GAID = "gaid";
  private static final String EVENT_TYPE = "eventType";
  private static final String LIMITED_AD_TRACKING = "limitedAdTracking";
  private static final String GDPR_CONSENT = "gdpr_consent";

  @NonNull
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  @NonNull
  private final JsonSerializer jsonSerializer;

  public PubSdkApi(
      @NonNull BuildConfigWrapper buildConfigWrapper,
      @NonNull JsonSerializer jsonSerializer
  ) {
    this.buildConfigWrapper = buildConfigWrapper;
    this.jsonSerializer = jsonSerializer;
  }

  @NonNull
  public RemoteConfigResponse loadConfig(@NonNull RemoteConfigRequest request) throws IOException {
    URL url = new URL(buildConfigWrapper.getCdbUrl() + "/config/app");
    HttpURLConnection urlConnection = prepareConnection(url, null, "POST");
    writePayload(urlConnection, request);

    try (InputStream inputStream = readResponseStreamIfSuccess(urlConnection)) {
      return jsonSerializer.read(RemoteConfigResponse.class, inputStream);
    }
  }

  @NonNull
  public CdbResponse loadCdb(@NonNull CdbRequest request, @NonNull String userAgent) throws Exception {
    URL url = new URL(buildConfigWrapper.getCdbUrl() + "/inapp/v2");
    HttpURLConnection urlConnection = prepareConnection(url, userAgent, "POST");
    urlConnection.setDoOutput(true);
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      jsonSerializer.write(request, baos);
      logger.log(NetworkLogMessage.onCdbCallStarted(baos.toString("UTF-8")));
      urlConnection.getOutputStream().write(baos.toByteArray());
    }

    try (InputStream inputStream = readResponseStreamIfSuccess(urlConnection)) {
      String response = StreamUtil.readStream(inputStream);
      logger.log(NetworkLogMessage.onCdbCallFinished(response));
      return CdbResponse.fromJson(readJson(response));
    }
  }

  @NonNull
  public JSONObject postAppEvent(
      int senderId,
      @NonNull String appId,
      @Nullable String gaid,
      @NonNull String eventType,
      int limitedAdTracking,
      @NonNull String userAgent,
      @Nullable String gdprConsentData
  ) throws Exception {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(APP_ID, appId);

    // If device does not support Play Services, GAID value will be null
    if (gaid != null) {
      parameters.put(GAID, gaid);
    }

    parameters.put(EVENT_TYPE, eventType);
    parameters.put(LIMITED_AD_TRACKING, String.valueOf(limitedAdTracking));

    if (gdprConsentData != null) {
        parameters.put(GDPR_CONSENT, gdprConsentData);
    }

    String query = "/appevent/v1/" + senderId + "?" + getParamsString(parameters);
    URL url = new URL(buildConfigWrapper.getEventUrl() + query);
    try (InputStream inputStream = executeRawGet(url, userAgent)) {
      return readJson(inputStream);
    }
  }

  @Nullable
  public InputStream executeRawGet(URL url) throws IOException {
    return executeRawGet(url, null);
  }

  public void postCsm(@NonNull MetricRequest request) throws IOException {
    postToCdb("/csm", request);
  }

  public void postLogs(@NonNull List<RemoteLogRecords> request) throws IOException {
    postToCdb("/inapp/logs", request);
  }

  private void postToCdb(@NonNull String apiPath, @NonNull Object request) throws IOException {
    URL url = new URL(buildConfigWrapper.getCdbUrl() + apiPath);
    HttpURLConnection urlConnection = prepareConnection(url, null, "POST");
    writePayload(urlConnection, request);
    readResponseStreamIfSuccess(urlConnection).close();
  }

  @NonNull
  public InputStream executeRawGet(URL url, @Nullable String userAgent) throws IOException {
    HttpURLConnection urlConnection = prepareConnection(url, userAgent, "GET");
    return readResponseStreamIfSuccess(urlConnection);
  }

  @NonNull
  private HttpURLConnection prepareConnection(@NonNull URL url,
      @Nullable String userAgent, String method) throws IOException {
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.setRequestMethod(method);
    urlConnection.setReadTimeout(buildConfigWrapper.getNetworkTimeoutInMillis());
    urlConnection.setConnectTimeout(buildConfigWrapper.getNetworkTimeoutInMillis());
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
    return readJson(response);
  }

  private static JSONObject readJson(@NonNull String json) throws JSONException {
    if (TextUtils.isEmpty(json)) {
      return new JSONObject();
    }
    return new JSONObject(json);
  }

  private void writePayload(
      @NonNull HttpURLConnection urlConnection,
      @NonNull Object request) throws IOException {
    urlConnection.setDoOutput(true);
    try (OutputStream outputStream = urlConnection.getOutputStream()) {
      jsonSerializer.write(request, outputStream);
    }
  }

  private String getParamsString(Map<String, String> params) {
    StringBuilder queryString = new StringBuilder();
    try {
      for (Map.Entry<String, String> entry : params.entrySet()) {
                queryString.append(URLEncoder.encode(entry.getKey(), Charset.forName("UTF-8").name()));
        queryString.append("=");
                queryString.append(URLEncoder.encode(entry.getValue(), Charset.forName("UTF-8").name()));
        queryString.append("&");
      }
    } catch (Exception e) {
      logger.debug("Impossible to encode params string", e);
    }

    // drop the last '&' if result is not empty
    return queryString.length() > 0
        ? queryString.substring(0, queryString.length() - 1)
        : queryString.toString();
  }

}
