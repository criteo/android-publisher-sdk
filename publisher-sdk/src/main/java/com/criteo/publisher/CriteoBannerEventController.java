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

import static com.criteo.publisher.CriteoListenerCode.CLICK;
import static com.criteo.publisher.CriteoListenerCode.CLOSE;
import static com.criteo.publisher.CriteoListenerCode.INVALID;
import static com.criteo.publisher.CriteoListenerCode.VALID;
import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;

import android.content.ComponentName;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.activity.TopActivityFinder;
import com.criteo.publisher.adview.AdWebViewClient;
import com.criteo.publisher.adview.RedirectionListener;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.tasks.CriteoBannerListenerCallTask;
import com.criteo.publisher.tasks.CriteoBannerLoadTask;
import java.lang.ref.WeakReference;


public class CriteoBannerEventController {
  @NonNull
  private final WeakReference<CriteoBannerView> view;

  @Nullable
  private final CriteoBannerAdListener adListener;

  @NonNull
  private final Criteo criteo;

  @NonNull
  private final TopActivityFinder topActivityFinder;

  @NonNull
  private final RunOnUiThreadExecutor executor;

  public CriteoBannerEventController(
      @NonNull CriteoBannerView bannerView,
      @NonNull Criteo criteo,
      @NonNull TopActivityFinder topActivityFinder,
      @NonNull RunOnUiThreadExecutor runOnUiThreadExecutor
  ) {
    this.view = new WeakReference<>(bannerView);
    this.adListener = bannerView.getCriteoBannerAdListener();
    this.criteo = criteo;
    this.topActivityFinder = topActivityFinder;
    this.executor = runOnUiThreadExecutor;
  }

  public void fetchAdAsync(@Nullable AdUnit adUnit) {
   criteo.getBidForAdUnit(adUnit, new BidListener() {
      @Override
      public void onBidResponse(@NonNull CdbResponseSlot cdbResponseSlot) {
        notifyFor(VALID);
        displayAd(cdbResponseSlot.getDisplayUrl());
      }

     @Override
     public void onNoBid() {
       notifyFor(INVALID);
     }
   });
  }

  public void fetchAdAsync(@Nullable Bid bid) {
    String displayUrl = bid == null ? null : bid.consumeDisplayUrlFor(CRITEO_BANNER);

    if (displayUrl == null) {
      notifyFor(INVALID);
    } else {
      notifyFor(VALID);
      displayAd(displayUrl);
    }
  }

  void notifyFor(@NonNull CriteoListenerCode code) {
    executor.executeAsync(new CriteoBannerListenerCallTask(adListener, view, code));
  }

  void displayAd(@NonNull String displayUrl) {
    executor.executeAsync(new CriteoBannerLoadTask(
        view, createWebViewClient(), criteo.getConfig(), displayUrl));
  }

  // WebViewClient is created here to prevent passing the AdListener everywhere.
  // Setting this webViewClient to the WebView is done in the CriteoBannerLoadTask as all
  // WebView methods need to run in the same UI thread
  @VisibleForTesting
  WebViewClient createWebViewClient() {
    // Even if we have access to the view here, we're not sure that publisher gave an activity
    // context to the view. So we're getting the activity by this way.
    ComponentName bannerActivityName = topActivityFinder.getTopActivityName();

    return new AdWebViewClient(new RedirectionListener() {
      @Override
      public void onUserRedirectedToAd() {
        notifyFor(CLICK);
      }

      @Override
      public void onUserBackFromAd() {
        notifyFor(CLOSE);
      }
    }, bannerActivityName);
  }

}
