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

package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.WebViewData;
import com.criteo.publisher.util.StreamUtil;
import com.criteo.publisher.util.TextUtils;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebViewDataTask extends AsyncTask<String, Void, String> {

  private static final String TAG = "Criteo.WVDT";

  @NonNull
  private final WebViewData webviewData;

  @NonNull
  private final DeviceInfo deviceInfo;

  @Nullable
  private final CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

  public WebViewDataTask(@NonNull WebViewData webviewData,
      @NonNull DeviceInfo deviceInfo,
      @Nullable CriteoInterstitialAdDisplayListener adDisplayListener) {
    this.webviewData = webviewData;
    this.deviceInfo = deviceInfo;
    this.criteoInterstitialAdDisplayListener = adDisplayListener;
  }

  @Override
  protected String doInBackground(String... args) {
    try {
      return doWebViewDataTask(args);
    } catch (Throwable tr) {
      Log.e(TAG, "Internal WVDT exec error.", tr);
    }

    return null;
  }

  private String doWebViewDataTask(String[] args) throws Exception {
    String displayUrl = args[0];
    String userAgent = deviceInfo.getUserAgent().get();

    URL url = new URL(displayUrl);

    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.setRequestMethod("GET");
    urlConnection.setRequestProperty("Content-Type", "text/plain");
    if (!TextUtils.isEmpty(userAgent)) {
      urlConnection.setRequestProperty("User-Agent", userAgent);
    }

    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
      return StreamUtil.readStream(urlConnection.getInputStream());
    }

    return null;
  }

  @Override
  protected void onPostExecute(@Nullable String data) {
    try {
      doOnPostExecute(data);
    } catch (Throwable tr) {
      Log.e(TAG, "Internal WVDT PostExec error.", tr);
    }
  }

  private void doOnPostExecute(@Nullable String data) {
    if (TextUtils.isEmpty(data)) {
      webviewData.downloadFailed();
      if (criteoInterstitialAdDisplayListener != null) {
        criteoInterstitialAdDisplayListener
            .onAdFailedToDisplay(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
      }
    } else {
      webviewData.setContent(data);
      webviewData.downloadSucceeded();
      if (criteoInterstitialAdDisplayListener != null) {
        criteoInterstitialAdDisplayListener.onAdReadyToDisplay();
      }
    }
  }

}
