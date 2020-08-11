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

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.util.ObjectUtils;

public class CriteoBannerView extends WebView {

  private static final String TAG = CriteoBannerView.class.getSimpleName();

  @Nullable
  private final BannerAdUnit bannerAdUnit;

  /**
   * Null means that the singleton Criteo should be used.
   * <p>
   * {@link Criteo#getInstance()} is fetched lazily so publishers may call the constructor without
   * having to init the SDK before.
   */
  @Nullable
  private final Criteo criteo;

  @Nullable
  private CriteoBannerAdListener criteoBannerAdListener;

  @Nullable
  private CriteoBannerEventController criteoBannerEventController;

  /**
   * Used by server side bidding and in-house auction
   */
  public CriteoBannerView(@NonNull Context context) {
    this(context, null, null);
  }

  /**
   * Used by Standalone
   */
  public CriteoBannerView(@NonNull Context context, @Nullable BannerAdUnit bannerAdUnit) {
    this(context, bannerAdUnit, null);
  }

  @VisibleForTesting
  CriteoBannerView(
      @NonNull Context context, @Nullable BannerAdUnit bannerAdUnit,
      @Nullable Criteo criteo
  ) {
    super(context);
    this.bannerAdUnit = bannerAdUnit;
    this.criteo = criteo;
  }

  public void setCriteoBannerAdListener(@Nullable CriteoBannerAdListener criteoBannerAdListener) {
    this.criteoBannerAdListener = criteoBannerAdListener;
  }

  @Nullable
  CriteoBannerAdListener getCriteoBannerAdListener() {
    return criteoBannerAdListener;
  }

  public void loadAd() {
    try {
      doLoadAd();
    } catch (Throwable tr) {
      Log.e(TAG, "Internal error while loading banner.", tr);
    }
  }

  public void displayAd(@NonNull String displayData) {
    getOrCreateController().displayAd(displayData);
  }

  private void doLoadAd() {
    getIntegrationRegistry().declare(Integration.STANDALONE);
    getOrCreateController().fetchAdAsync(bannerAdUnit);
  }

  public void loadAd(@Nullable BidToken bidToken) {
    try {
      doLoadAd(bidToken);
    } catch (Throwable tr) {
      Log.e(TAG, "Internal error while loading banner from bid token.", tr);
    }
  }

  private void doLoadAd(@Nullable BidToken bidToken) {
    if (bidToken != null && !ObjectUtils.equals(bannerAdUnit, bidToken.getAdUnit())) {
      return;
    }

    getOrCreateController().fetchAdAsync(bidToken);
  }

  @NonNull
  @VisibleForTesting
  CriteoBannerEventController getOrCreateController() {
    if (criteoBannerEventController == null) {
      criteoBannerEventController = getCriteo().createBannerController(this);
    }
    return criteoBannerEventController;
  }

  @NonNull
  private Criteo getCriteo() {
    return criteo == null ? Criteo.getInstance() : criteo;
  }

  @NonNull
  private IntegrationRegistry getIntegrationRegistry() {
    return DependencyProvider.getInstance().provideIntegrationRegistry();
  }

}
