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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoListenerCode;
import java.lang.ref.Reference;

public class CriteoBannerListenerCallTask implements Runnable {

  @Nullable
  private final CriteoBannerAdListener listener;

  @NonNull
  private final Reference<CriteoBannerView> bannerViewRef;

  @NonNull
  private final CriteoListenerCode code;

  /**
   * Task that calls the relevant callback in the {@link CriteoBannerAdListener} based on the {@link
   * CriteoListenerCode} passed to execute. Passes the {@link CriteoBannerView} as a parameter to
   * the onAdReceived callback if the CriteoListenerCode is valid.
   */
  public CriteoBannerListenerCallTask(
      @Nullable CriteoBannerAdListener listener,
      @NonNull Reference<CriteoBannerView> bannerViewRef,
      @NonNull CriteoListenerCode code) {
    this.listener = listener;
    this.bannerViewRef = bannerViewRef;
    this.code = code;
  }

  @Override
  public void run() {
    CriteoBannerView bannerView = bannerViewRef.get();

    // If banner is null, it means that publisher released it.
    if (listener == null || bannerView == null) {
      return;
    }

    switch (code) {
      case INVALID:
        listener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        break;
      case VALID:
        listener.onAdReceived(bannerView);
        break;
      case CLICK:
        listener.onAdClicked();
        listener.onAdLeftApplication();
        listener.onAdOpened();
        break;
      case CLOSE:
        listener.onAdClosed();
        break;
    }
  }

}
