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

package com.criteo.publisher;

import static com.criteo.publisher.CriteoListenerCode.INVALID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.DisplayUrlTokenValue;
import com.criteo.publisher.model.WebViewData;
import com.criteo.publisher.tasks.CriteoInterstitialListenerCallTask;
import com.criteo.publisher.util.AdUnitType;


public class CriteoInterstitialEventController {

  @Nullable
  private final CriteoInterstitialAdListener criteoInterstitialAdListener;

  @NonNull
  private final WebViewData webViewData;

  @NonNull
  private final DeviceInfo deviceInfo;

  @NonNull
  private final Criteo criteo;

  @NonNull
  private final InterstitialActivityHelper interstitialActivityHelper;

  @NonNull
  private final RunOnUiThreadExecutor executor;

  public CriteoInterstitialEventController(
      @Nullable CriteoInterstitialAdListener listener,
      @NonNull WebViewData webViewData,
      @NonNull InterstitialActivityHelper interstitialActivityHelper,
      @NonNull Criteo criteo
  ) {
    this.criteoInterstitialAdListener = listener;
    this.webViewData = webViewData;
    this.interstitialActivityHelper = interstitialActivityHelper;
    this.criteo = criteo;
    this.deviceInfo = criteo.getDeviceInfo();
    this.executor = DependencyProvider.getInstance().provideRunOnUiThreadExecutor();
  }

  public boolean isAdLoaded() {
    return webViewData.isLoaded();
  }

  public void fetchAdAsync(@Nullable AdUnit adUnit) {
    if (!interstitialActivityHelper.isAvailable()) {
      notifyForFailure();
      return;
    }

    if (webViewData.isLoading()) {
      return;
    }

    webViewData.downloadLoading();

    CdbResponseSlot slot = criteo.getBidForAdUnit(adUnit);

    if (slot == null) {
      notifyForFailure();
      webViewData.downloadFailed();
    } else {
      fetchCreativeAsync(slot.getDisplayUrl());
    }
  }

  public void fetchAdAsync(@Nullable BidToken bidToken) {
    DisplayUrlTokenValue tokenValue = criteo.getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL);

    if (tokenValue == null) {
      notifyForFailure();
    } else {
      fetchCreativeAsync(tokenValue.getDisplayUrl());
    }
  }

  @VisibleForTesting
  void notifyForFailure() {
    Runnable task = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener, INVALID);
    executor.executeAsync(task);
  }

  @VisibleForTesting
  void fetchCreativeAsync(@NonNull String displayUrl) {
    webViewData.fillWebViewHtmlContent(
        displayUrl,
        deviceInfo,
        criteoInterstitialAdListener
    );
  }

  public void show() {
    if (!isAdLoaded()) {
      return;
    }

    String webViewContent = webViewData.getContent();
    interstitialActivityHelper.openActivity(webViewContent, criteoInterstitialAdListener);

    if (criteoInterstitialAdListener != null) {
      criteoInterstitialAdListener.onAdOpened();
    }

    webViewData.refresh();
  }
}
