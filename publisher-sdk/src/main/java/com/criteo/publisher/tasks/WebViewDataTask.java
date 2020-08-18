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
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.StreamUtil;
import com.criteo.publisher.util.TextUtils;
import java.io.InputStream;
import java.net.URL;

public class WebViewDataTask extends AsyncTask<String, Void, String> {

  private static final String TAG = "Criteo.WVDT";

  @NonNull
  private final String displayUrl;

  @NonNull
  private final WebViewData webviewData;

  @NonNull
  private final DeviceInfo deviceInfo;

  @Nullable
  private final CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

  @NonNull
  private final PubSdkApi api;

  public WebViewDataTask(
      @NonNull String displayUrl,
      @NonNull WebViewData webviewData,
      @NonNull DeviceInfo deviceInfo,
      @Nullable CriteoInterstitialAdDisplayListener adDisplayListener,
      @NonNull PubSdkApi api
  ) {
    this.displayUrl = displayUrl;
    this.webviewData = webviewData;
    this.deviceInfo = deviceInfo;
    this.criteoInterstitialAdDisplayListener = adDisplayListener;
    this.api = api;
  }

  @Override
  protected String doInBackground(String... args) {
    try {
      return doWebViewDataTask();
    } catch (Throwable tr) {
      Log.e(TAG, "Internal WVDT exec error.", tr);
      return null;
    }
  }

  private String doWebViewDataTask() throws Exception {
    URL url = new URL(displayUrl);
    String userAgent = deviceInfo.getUserAgent().get();

    try (InputStream stream = api.executeRawGet(url, userAgent)) {
      return StreamUtil.readStream(stream);
    }
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
