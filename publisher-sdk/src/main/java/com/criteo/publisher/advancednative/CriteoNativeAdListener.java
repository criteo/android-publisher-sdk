package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import com.criteo.publisher.CriteoErrorCode;

public abstract class CriteoNativeAdListener {

  /**
   * Callback invoked when a native ad is requested and is successfully received.
   *
   * This callback is invoked on the UI thread, so it is safe to execute UI operations in the
   * implementation. It is expected that the publisher will display the native ad during this call.
   *
   * @param nativeAd native ad with the native data that may be used to render it
   */
  @UiThread
  void onAdReceived(@NonNull CriteoNativeAd nativeAd) {
  }

  /**
   * Callback invoked when a native ad is requested but none may be provided by the SDK.
   *
   * This callback is invoked on the UI thread, so it is safe to execute UI operations in the
   * implementation.
   *
   * @param errorCode error code indicating the reason of the failure
   */
  @UiThread
  void onAdFailedToReceive(@NonNull CriteoErrorCode errorCode) {
  }

  /**
   * Callback invoked when a native view is detected as being displayed on user screen and ad
   * impression is triggered.
   * <p>
   * Impression can be detected only once per bid. So this method may be invoked at most once.
   * <p>
   * This callback is invoked on the UI thread, so it is safe to execute UI operations in the
   * implementation.
   */
  @UiThread
  void onAdImpression() {
  }
}
