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

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.interstitial.InterstitialLogMessage;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.WebViewData;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.tasks.InterstitialListenerNotifier;

@Keep
public class CriteoInterstitial {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Nullable
  final InterstitialAdUnit interstitialAdUnit;

  /**
   * Null means that the singleton Criteo should be used.
   * <p>
   * {@link Criteo#getInstance()} is fetched lazily so publishers may call the constructor without
   * having to init the SDK before.
   */
  @Nullable
  private final Criteo criteo;

  @Nullable
  private CriteoInterstitialEventController criteoInterstitialEventController;

  @Nullable
  private CriteoInterstitialAdListener criteoInterstitialAdListener;

  /**
   * Used by server side bidding and in-house auction
   */
  public CriteoInterstitial() {
    this(null, null);
  }

  /**
   * Used by Standalone
   */
  public CriteoInterstitial(@NonNull InterstitialAdUnit interstitialAdUnit) {
    this(interstitialAdUnit, null);
  }

  @VisibleForTesting
  CriteoInterstitial(
      @Nullable InterstitialAdUnit interstitialAdUnit,
      @Nullable Criteo criteo
  ) {
    this.interstitialAdUnit = interstitialAdUnit;
    this.criteo = criteo;
    logger.log(InterstitialLogMessage.onInterstitialInitialized(interstitialAdUnit));
  }

  public void setCriteoInterstitialAdListener(
      @Nullable CriteoInterstitialAdListener criteoInterstitialAdListener
  ) {
    this.criteoInterstitialAdListener = criteoInterstitialAdListener;
  }

  public void loadAd() {
    loadAd(new ContextData());
  }

  public void loadAd(@NonNull ContextData contextData) {
    if (!DependencyProvider.getInstance().isApplicationSet()) {
      logger.warning("Calling CriteoInterstitial#loadAd with a null application");
      return;
    }

    try {
      doLoadAd(contextData);
    } catch (Throwable tr) {
      logger.error("Internal error while loading interstitial.", tr);
    }
  }

  private void doLoadAd(@NonNull ContextData contextData) {
    logger.log(InterstitialLogMessage.onInterstitialLoading(this));
    getIntegrationRegistry().declare(Integration.STANDALONE);
    getOrCreateController().fetchAdAsync(interstitialAdUnit, contextData);
  }

  public void loadAd(@Nullable Bid bid) {
    if (!DependencyProvider.getInstance().isApplicationSet()) {
      logger.warning("Calling CriteoInterstitial#loadAd(bidToken) with a null application");
      return;
    }

    try {
      doLoadAd(bid);
    } catch (Throwable tr) {
      logger.error("Internal error while loading interstitial from bid token.", tr);
    }
  }

  public void loadAdWithDisplayData(@NonNull String displayData) {
    if (!DependencyProvider.getInstance().isApplicationSet()) {
      logger.warning("Calling CriteoInterstitial#loadAdWithDisplayData with a null application");
      return;
    }

    getOrCreateController().fetchCreativeAsync(displayData);
  }

  private void doLoadAd(@Nullable Bid bid) {
    logger.log(InterstitialLogMessage.onInterstitialLoading(this, bid));
    getIntegrationRegistry().declare(Integration.IN_HOUSE);
    getOrCreateController().fetchAdAsync(bid);
  }

  public boolean isAdLoaded() {
    try {
      boolean isAdLoaded = getOrCreateController().isAdLoaded();
      logger.log(InterstitialLogMessage.onCheckingIfInterstitialIsLoaded(this, isAdLoaded));
      return isAdLoaded;
    } catch (Throwable tr) {
      logger.error("Internal error while detecting interstitial load state.", tr);
      return false;
    }
  }

  public void show() {
    if (!DependencyProvider.getInstance().isApplicationSet()) {
      logger.warning("Calling CriteoInterstitial#show with a null application");
      return;
    }

    try {
      doShow();
    } catch (Throwable tr) {
      logger.error("Internal error while showing interstitial.", tr);
    }
  }

  private void doShow() {
    logger.log(InterstitialLogMessage.onInterstitialShowing(this));
    getOrCreateController().show();
  }

  @NonNull
  @VisibleForTesting
  CriteoInterstitialEventController getOrCreateController() {
    if (criteoInterstitialEventController == null) {
      Criteo criteo = getCriteo();

      InterstitialListenerNotifier listenerNotifier = new InterstitialListenerNotifier(
          this,
          criteoInterstitialAdListener,
          getRunOnUiThreadExecutor()
      );

      criteoInterstitialEventController = new CriteoInterstitialEventController(
          new WebViewData(criteo.getConfig(), getPubSdkApi()),
          criteo.getInterstitialActivityHelper(),
          criteo,
          listenerNotifier
      );
    }
    return criteoInterstitialEventController;
  }

  @NonNull
  private Criteo getCriteo() {
    return criteo == null ? Criteo.getInstance() : criteo;
  }

  @NonNull
  private IntegrationRegistry getIntegrationRegistry() {
    return DependencyProvider.getInstance().provideIntegrationRegistry();
  }

  @NonNull
  private PubSdkApi getPubSdkApi() {
    return DependencyProvider.getInstance().providePubSdkApi();
  }

  @NonNull
  private RunOnUiThreadExecutor getRunOnUiThreadExecutor() {
    return DependencyProvider.getInstance().provideRunOnUiThreadExecutor();
  }

}
