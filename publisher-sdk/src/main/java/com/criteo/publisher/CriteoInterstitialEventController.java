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
import static com.criteo.publisher.CriteoListenerCode.OPEN;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.DisplayUrlTokenValue;
import com.criteo.publisher.model.WebViewData;
import com.criteo.publisher.tasks.InterstitialListenerNotifier;
import com.criteo.publisher.util.AdUnitType;


public class CriteoInterstitialEventController {

  @NonNull
  private final WebViewData webViewData;

  @NonNull
  private final DeviceInfo deviceInfo;

  @NonNull
  private final Criteo criteo;

  @NonNull
  private final InterstitialActivityHelper interstitialActivityHelper;

  @NonNull
  private final InterstitialListenerNotifier listenerNotifier;

  public CriteoInterstitialEventController(
      @NonNull WebViewData webViewData,
      @NonNull InterstitialActivityHelper interstitialActivityHelper,
      @NonNull Criteo criteo,
      @NonNull InterstitialListenerNotifier listenerNotifier
  ) {
    this.webViewData = webViewData;
    this.interstitialActivityHelper = interstitialActivityHelper;
    this.criteo = criteo;
    this.deviceInfo = criteo.getDeviceInfo();
    this.listenerNotifier = listenerNotifier;
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
    criteo.getBidForAdUnit(adUnit, new BidListener() {
      @Override
      public void onBidResponse(@NonNull CdbResponseSlot cdbResponseSlot) {
        fetchCreativeAsync(cdbResponseSlot.getDisplayUrl());
      }

      @Override
      public void onNoBid() {
        notifyForFailure();
        webViewData.downloadFailed();
      }
    });
  }

  public void fetchAdAsync(@Nullable BidToken bidToken) {
    DisplayUrlTokenValue tokenValue = criteo.getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL);

    if (tokenValue == null) {
      notifyForFailure();
    } else {
      fetchCreativeAsync(tokenValue.getDisplayUrl());
    }
  }

  void notifyForFailure() {
    listenerNotifier.notifyFor(INVALID);
  }

  void fetchCreativeAsync(@NonNull String displayUrl) {
    webViewData.fillWebViewHtmlContent(
        displayUrl,
        deviceInfo,
        listenerNotifier
    );
  }

  public void show() {
    if (!isAdLoaded()) {
      return;
    }

    String webViewContent = webViewData.getContent();
    interstitialActivityHelper.openActivity(webViewContent, listenerNotifier);
    listenerNotifier.notifyFor(OPEN);

    webViewData.refresh();
  }
}
