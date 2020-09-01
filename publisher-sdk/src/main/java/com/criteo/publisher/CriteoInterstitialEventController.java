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
import static com.criteo.publisher.CriteoListenerCode.VALID;

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

  @Nullable
  private final CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

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
      @Nullable CriteoInterstitialAdDisplayListener adDisplayListener,
      @NonNull WebViewData webViewData,
      @NonNull InterstitialActivityHelper interstitialActivityHelper,
      @NonNull Criteo criteo) {
    this.criteoInterstitialAdListener = listener;
    this.criteoInterstitialAdDisplayListener = adDisplayListener;
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
      notifyFor(INVALID);
      return;
    }

    if (webViewData.isLoading()) {
      return;
    }

    webViewData.downloadLoading();

    CdbResponseSlot slot = criteo.getBidForAdUnit(adUnit);

    if (slot == null) {
      notifyFor(INVALID);
      webViewData.downloadFailed();
    } else {
      notifyFor(VALID);
      fetchCreativeAsync(slot.getDisplayUrl());
    }
  }

  public void fetchAdAsync(@Nullable BidToken bidToken) {
    DisplayUrlTokenValue tokenValue = criteo.getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL);

    if (tokenValue == null) {
      notifyFor(INVALID);
    } else {
      notifyFor(VALID);
      fetchCreativeAsync(tokenValue.getDisplayUrl());
    }
  }

  void notifyFor(@NonNull CriteoListenerCode code) {
    executor
        .executeAsync(new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener, code));
  }

  void fetchCreativeAsync(@NonNull String displayUrl) {
    webViewData.fillWebViewHtmlContent(
        displayUrl,
        deviceInfo,
        criteoInterstitialAdDisplayListener);
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
