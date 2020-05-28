package com.criteo.publisher;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import com.criteo.publisher.util.ObjectsUtil;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.WebViewData;

public class CriteoInterstitial {

  private static final String TAG = CriteoInterstitial.class.getSimpleName();

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
    getOrCreateController().show();
  }

  @NonNull
  @VisibleForTesting
  CriteoInterstitialEventController getOrCreateController() {
    if (criteoInterstitialEventController == null) {
      Criteo criteo = getCriteo();

      criteoInterstitialEventController = new CriteoInterstitialEventController(
          criteoInterstitialAdListener,
          criteoInterstitialAdDisplayListener,
          new WebViewData(criteo.getConfig()),
          criteo.getInterstitialActivityHelper(),
          criteo
      );
    }
    return criteoInterstitialEventController;
  }

  @NonNull
  private Criteo getCriteo() {
    return criteo == null ? Criteo.getInstance() : criteo;
  }


}
