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

import static com.criteo.publisher.CriteoListenerCode.INVALID_CREATIVE;
import static com.criteo.publisher.CriteoListenerCode.VALID;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.WebViewData;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.StreamUtil;
import com.criteo.publisher.util.TextUtils;
import java.io.InputStream;
import java.net.URL;

public class WebViewDataTask extends SafeRunnable {

  @NonNull
  private final String displayUrl;

  @NonNull
  private final WebViewData webviewData;

  @NonNull
  private final DeviceInfo deviceInfo;

  @NonNull
  private final InterstitialListenerNotifier listenerNotifier;

  @NonNull
  private final PubSdkApi api;

  public WebViewDataTask(
      @NonNull String displayUrl,
      @NonNull WebViewData webviewData,
      @NonNull DeviceInfo deviceInfo,
      @NonNull InterstitialListenerNotifier listenerNotifier,
      @NonNull PubSdkApi api
  ) {
    this.displayUrl = displayUrl;
    this.webviewData = webviewData;
    this.deviceInfo = deviceInfo;
    this.listenerNotifier = listenerNotifier;
    this.api = api;
  }

  @Override
  public void runSafely() throws Exception {
    String creative = null;

    try {
      creative = downloadCreative();
    } finally {
      if (TextUtils.isEmpty(creative)) {
        notifyForFailure();
      } else {
        notifyForSuccess(creative);
      }
    }
  }

  @NonNull
  @VisibleForTesting
  String downloadCreative() throws Exception {
    URL url = new URL(displayUrl);
    String userAgent = deviceInfo.getUserAgent().get();

    try (InputStream stream = api.executeRawGet(url, userAgent)) {
      return StreamUtil.readStream(stream);
    }
  }

  @VisibleForTesting
  void notifyForSuccess(@NonNull String creative) {
    webviewData.setContent(creative);
    webviewData.downloadSucceeded();
    listenerNotifier.notifyFor(VALID);
  }

  @VisibleForTesting
  void notifyForFailure() {
    webviewData.downloadFailed();
    listenerNotifier.notifyFor(INVALID_CREATIVE);
  }

}
