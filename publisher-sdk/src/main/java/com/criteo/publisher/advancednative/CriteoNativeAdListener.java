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

package com.criteo.publisher.advancednative;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import com.criteo.publisher.CriteoErrorCode;

/**
 * All callbacks are invoked on the UI thread, so it is safe to execute any UI operations in the
 * implementation.
 */
@Keep
public abstract class CriteoNativeAdListener {

  /**
   * Callback invoked when a native ad is requested and is successfully received.
   * <p>
   * It is expected that the publisher will display the native ad during this call.
   *
   * @param nativeAd native ad with the native data that may be used to render it
   */
  @UiThread
  public abstract void onAdReceived(@NonNull CriteoNativeAd nativeAd);

  /**
   * Callback invoked when a native ad is requested but none may be provided by the SDK.
   *
   * @param errorCode error code indicating the reason of the failure
   */
  @UiThread
  public void onAdFailedToReceive(@NonNull CriteoErrorCode errorCode) {
  }

  /**
   * Callback invoked when a native view is detected as being displayed on user screen and ad
   * impression is triggered.
   * <p>
   * Impression can be detected only once per bid. So this method may be invoked at most once.
   */
  @UiThread
  public void onAdImpression() {
  }

  /**
   * Callback invoked when an user clicks anywhere on the ad (except on the AdChoice button).
   */
  @UiThread
  public void onAdClicked() {
  }

  /**
   * Callback invoked when an ad is opened and the user is redirected outside the application, to
   * the product web page or to the AdChoice page for instance.
   */
  @UiThread
  public void onAdLeftApplication() {
  }

  /**
   * Callback invoked when the user is back from the Ad. This happens generally when the user
   * presses the back button after being redirected to an ad.
   */
  @UiThread
  public void onAdClosed() {
  }
}
