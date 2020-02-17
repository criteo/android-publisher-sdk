package com.criteo.publisher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.criteo.publisher.Util.CriteoResultReceiver;
import com.criteo.publisher.Util.ObjectsUtil;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.WebViewData;

public class CriteoInterstitial {

  private static final String TAG = CriteoInterstitial.class.getSimpleName();
  protected static final String WEB_VIEW_DATA = "webviewdata";
  protected static final String RESULT_RECEIVER = "resultreceiver";

  private final InterstitialAdUnit interstitialAdUnit;

  @NonNull
  private final Context context;

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

  @Nullable
  private CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

  public CriteoInterstitial(@NonNull Context context, InterstitialAdUnit interstitialAdUnit) {
    this(context, interstitialAdUnit, null);
  }

  @VisibleForTesting
  CriteoInterstitial(@NonNull Context context,
      InterstitialAdUnit interstitialAdUnit,
      @Nullable Criteo criteo) {
    this.context = context;
    this.interstitialAdUnit = interstitialAdUnit;
    this.criteo = criteo;
  }

  public void setCriteoInterstitialAdListener(
      @Nullable CriteoInterstitialAdListener criteoInterstitialAdListener) {
    this.criteoInterstitialAdListener = criteoInterstitialAdListener;
  }

  public void setCriteoInterstitialAdDisplayListener(
      @Nullable CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener) {
    this.criteoInterstitialAdDisplayListener = criteoInterstitialAdDisplayListener;
  }

  public void loadAd() {
    try {
      doLoadAd();
    } catch (Throwable tr) {
      Log.e(TAG, "Internal error while loading interstitial.", tr);
    }
  }

  private void doLoadAd() {
    getOrCreateController().fetchAdAsync(interstitialAdUnit);
  }

  public void loadAd(@Nullable BidToken bidToken) {
    try {
      doLoadAd(bidToken);
    } catch (Throwable tr) {
      Log.e(TAG, "Internal error while loading interstitial from bid token.", tr);
    }
  }

  private void doLoadAd(@Nullable BidToken bidToken) {
    if (bidToken != null && !ObjectsUtil.equals(interstitialAdUnit, bidToken.getAdUnit())) {
      return;
    }

    getOrCreateController().fetchAdAsync(bidToken);
  }

  public boolean isAdLoaded() {
    try {
      return getOrCreateController().isAdLoaded();
    } catch (Throwable tr) {
      Log.e(TAG, "Internal error while detecting interstitial load state.", tr);
      return false;
    }
  }

  public void show() {
    try {
      doShow();
    } catch (Throwable tr) {
      Log.e(TAG, "Internal error while showing interstitial.", tr);
    }
  }

  private void doShow() {
    if (isAdLoaded()) {
      Intent intent = new Intent(context, CriteoInterstitialActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      Bundle bundle = new Bundle();
      CriteoInterstitialEventController controller = getOrCreateController();
      bundle.putString(WEB_VIEW_DATA, controller.getWebViewDataContent());
      CriteoResultReceiver criteoResultReceiver = new CriteoResultReceiver(new Handler(),
          criteoInterstitialAdListener);
      bundle.putParcelable(RESULT_RECEIVER, criteoResultReceiver);
      intent.putExtras(bundle);
      if (criteoInterstitialAdListener != null) {
        criteoInterstitialAdListener.onAdOpened();
      }
      controller.refresh();
      context.startActivity(intent);
    }
  }

  @NonNull
  @VisibleForTesting
  CriteoInterstitialEventController getOrCreateController() {
    if (criteoInterstitialEventController == null) {
      criteoInterstitialEventController = new CriteoInterstitialEventController(
          criteoInterstitialAdListener,
          criteoInterstitialAdDisplayListener,
          new WebViewData(getCriteo().getConfig()),
          getCriteo()
      );
    }
    return criteoInterstitialEventController;
  }

  @NonNull
  private Criteo getCriteo() {
    return criteo == null ? Criteo.getInstance() : criteo;
  }


}
