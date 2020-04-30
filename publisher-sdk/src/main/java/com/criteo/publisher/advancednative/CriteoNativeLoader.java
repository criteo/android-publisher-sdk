package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import com.criteo.publisher.BidManager;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.annotation.Incubating;
import com.criteo.publisher.model.NativeAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.util.PreconditionsUtil;
import com.criteo.publisher.util.RunOnUiThreadExecutor;

@Incubating(Incubating.NATIVE)
public class CriteoNativeLoader {

  @NonNull
  private final NativeAdUnit adUnit;

  @NonNull
  private final CriteoNativeAdListener listener;

  public CriteoNativeLoader(
      @NonNull NativeAdUnit adUnit,
      @NonNull CriteoNativeAdListener listener
  ) {
    this.adUnit = adUnit;
    this.listener = listener;
  }

  /**
   * Request the Criteo SDK for a native ad matching the given {@link NativeAdUnit}.
   *
   * This method returns immediately. If an ad is available, you will be notified by the {@link
   * CriteoNativeAdListener#onAdReceived(CriteoNativeAd)} callback. If no ad is available, you will
   * be notified by the {@link CriteoNativeAdListener#onAdFailedToReceive(CriteoErrorCode)}
   * callback.
   */
  public void loadAd() {
    try {
      doLoad();
    } catch (Throwable t) {
      PreconditionsUtil.throwOrLog(t);
    }
  }

  private void doLoad() {
    BidManager bidManager = getBidManager();
    Slot bid = bidManager.getBidForAdUnitAndPrefetch(adUnit);
    NativeAssets assets = bid == null ? null : bid.getNativeAssets();

    if (assets == null) {
      notifyForFailureAsync();
    } else {
      NativeAdMapper nativeAdMapper = getNativeAdMapper();
      CriteoNativeAd nativeAd = nativeAdMapper.map(assets);
      notifyForAdAsync(nativeAd);
    }
  }

  private void notifyForAdAsync(@NonNull CriteoNativeAd nativeAd) {
    getUiThreadExecutor().executeAsync(new Runnable() {
      @Override
      public void run() {
        listener.onAdReceived(nativeAd);
      }
    });
  }

  private void notifyForFailureAsync() {
    getUiThreadExecutor().executeAsync(new Runnable() {
      @Override
      public void run() {
        listener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
      }
    });
  }

  @NonNull
  private NativeAdMapper getNativeAdMapper() {
    return DependencyProvider.getInstance().provideNativeAdMapper();
  }

  @NonNull
  private RunOnUiThreadExecutor getUiThreadExecutor() {
    return DependencyProvider.getInstance().provideRunOnUiThreadExecutor();
  }

  @NonNull
  private BidManager getBidManager() {
    return DependencyProvider.getInstance().provideBidManager();
  }

}
