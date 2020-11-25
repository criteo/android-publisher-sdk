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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.webkit.WebView;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.util.PreconditionsUtil;

@Keep
public class CriteoBannerView extends WebView {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final int UNSET_DIMENSION_VALUE = -1;

  @Nullable
  final BannerAdUnit bannerAdUnit;

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
   * Used when setting {@link CriteoBannerView} in XML
   */
  public CriteoBannerView(@NonNull Context context, AttributeSet attrs) {
    super(context, attrs);
    criteo = null;

    TypedArray a = context.getTheme().obtainStyledAttributes(
        attrs,
        R.styleable.CriteoBannerView,
        0,
        0
    );

    try {
      int width = a.getInteger(
          R.styleable.CriteoBannerView_criteoAdUnitWidth,
          UNSET_DIMENSION_VALUE
      );
      int height = a.getInteger(
          R.styleable.CriteoBannerView_criteoAdUnitHeight,
          UNSET_DIMENSION_VALUE
      );
      String adUnitId = a.getString(R.styleable.CriteoBannerView_criteoAdUnitId);

      if (adUnitId != null && width != UNSET_DIMENSION_VALUE && height != UNSET_DIMENSION_VALUE) {
        bannerAdUnit = new BannerAdUnit(adUnitId, new AdSize(width, height));
      } else if (adUnitId == null && width == UNSET_DIMENSION_VALUE
          && height == UNSET_DIMENSION_VALUE) {
        bannerAdUnit = null;
      } else {
        bannerAdUnit = null;
        PreconditionsUtil.throwOrLog(new IllegalStateException(
            "CriteoBannerView was not properly inflated. For InHouse integration, no attribute must "
                + "be set. For Standalone integration, all of: criteoAdUnitId, criteoAdUnitWidth and "
                + "criteoAdUnitHeight must be set.")
        );
      }
    } finally {
      a.recycle();
    }

    logger.log(BannerLogMessage.onBannerViewInitialized(bannerAdUnit));
  }

  /**
   * Used by server side bidding and in-house auction
   */
  public CriteoBannerView(@NonNull Context context) {
    this(context, null, null);
  }

  /**
   * Used by Standalone
   */
  public CriteoBannerView(@NonNull Context context, @NonNull BannerAdUnit bannerAdUnit) {
    this(context, bannerAdUnit, null);
  }

  @VisibleForTesting
  CriteoBannerView(
      @NonNull Context context,
      @Nullable BannerAdUnit bannerAdUnit,
      @Nullable Criteo criteo
  ) {
    super(context);
    this.bannerAdUnit = bannerAdUnit;
    this.criteo = criteo;
    logger.log(BannerLogMessage.onBannerViewInitialized(bannerAdUnit));
  }

  public void setCriteoBannerAdListener(@Nullable CriteoBannerAdListener criteoBannerAdListener) {
    this.criteoBannerAdListener = criteoBannerAdListener;
  }

  @Nullable
  CriteoBannerAdListener getCriteoBannerAdListener() {
    return criteoBannerAdListener;
  }

  public void loadAd() {
    loadAd(new ContextData());
  }

  public void loadAd(@NonNull ContextData contextData) {
    try {
      doLoadAd(contextData);
    } catch (Throwable tr) {
      logger.error("Internal error while loading banner.", tr);
    }
  }

  public void loadAdWithDisplayData(@NonNull String displayData) {
    getOrCreateController().notifyFor(CriteoListenerCode.VALID);
    getOrCreateController().displayAd(displayData);
  }

  private void doLoadAd(@NonNull ContextData contextData) {
    logger.log(BannerLogMessage.onBannerViewLoading(this));
    getIntegrationRegistry().declare(Integration.STANDALONE);
    getOrCreateController().fetchAdAsync(bannerAdUnit, contextData);
  }

  public void loadAd(@Nullable Bid bid) {
    try {
      doLoadAd(bid);
    } catch (Throwable tr) {
      logger.error("Internal error while loading banner from bid token.", tr);
    }
  }

  private void doLoadAd(@Nullable Bid bid) {
    logger.log(BannerLogMessage.onBannerViewLoading(this, bid));
    getIntegrationRegistry().declare(Integration.IN_HOUSE);
    getOrCreateController().fetchAdAsync(bid);
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
