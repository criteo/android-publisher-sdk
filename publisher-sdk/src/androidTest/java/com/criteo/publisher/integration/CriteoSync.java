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

package com.criteo.publisher.integration;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdListener;
import java.util.concurrent.CountDownLatch;

final class CriteoSync {

  private final Handler handler;
  private final Runnable init;

  private CountDownLatch isLoaded;
  private CountDownLatch isDisplayed;

  CriteoSync(CriteoBannerView bannerView) {
    this.handler = new Handler(Looper.getMainLooper());
    this.init = () -> {
      this.isLoaded = new CountDownLatch(1);
      this.isDisplayed = isLoaded;
    };
    reset();
    bannerView.setCriteoBannerAdListener(new SyncAdListener());
  }

  CriteoSync(CriteoInterstitial interstitial) {
    this.handler = new Handler(Looper.getMainLooper());
    this.init = () -> {
      this.isLoaded = new CountDownLatch(1);
      this.isDisplayed = new CountDownLatch(1);
    };

    reset();

    SyncAdListener listener = new SyncAdListener();
    interstitial.setCriteoInterstitialAdListener(listener);
  }

  /**
   * This method is not atomic. Do not use it on multiple threads.
   */
  void reset() {
    emptyLatches();
    init.run();
  }

  void waitForBid() throws InterruptedException {
    isLoaded.await();
  }

  void waitForDisplay() throws InterruptedException {
    isDisplayed.await();
  }

  private void onLoaded() {
    // Criteo does not seem to totally be ready at this point.
    // It seems to be ready few times after the end of this method.
    // This may be caused by the webview that should load the creative.
    // So we should still wait a little in a non-deterministic way, but not in this method.
    handler.postDelayed(isLoaded::countDown, 1000);
  }

  private void onDisplayed() {
    handler.postDelayed(isDisplayed::countDown, 1000);
  }

  private void onFailed() {
    emptyLatches();
  }

  private void emptyLatches() {
    if (isLoaded != null) {
      while (isLoaded.getCount() > 0) {
        isLoaded.countDown();
      }
    }

    if (isDisplayed != null) {
      while (isDisplayed.getCount() > 0) {
        isDisplayed.countDown();
      }
    }
  }

  private class SyncAdListener implements CriteoBannerAdListener,
      CriteoInterstitialAdListener {

    @Override
    public void onAdReceived(@NonNull CriteoBannerView view) {
      onLoaded();
    }

    @UiThread
    @Override
    public void onAdReceived(@NonNull CriteoInterstitial interstitial) {
      onLoaded();
    }

    @Override
    public void onAdOpened() {
      onDisplayed();
    }

    @Override
    public void onAdFailedToReceive(@NonNull CriteoErrorCode code) {
      onFailed();
    }
  }
}
