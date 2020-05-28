package com.criteo.publisher.advancednative;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.BidManager;
import com.criteo.publisher.BidToken;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.InHouse;
import com.criteo.publisher.annotation.Incubating;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.model.NativeAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.model.nativeads.NativeTokenValue;
import com.criteo.publisher.util.PreconditionsUtil;
import java.lang.ref.WeakReference;

@Incubating(Incubating.NATIVE)
public class CriteoNativeLoader {

  @NonNull
  private final NativeAdUnit adUnit;

  @NonNull
  private final CriteoNativeAdListener listener;

  @NonNull
  private final CriteoNativeRenderer publisherRenderer;

  @Nullable
  private CriteoNativeRenderer renderer;

  public CriteoNativeLoader(
      @NonNull NativeAdUnit adUnit,
      @NonNull CriteoNativeAdListener listener,
      @NonNull CriteoNativeRenderer renderer
  ) {
    this.adUnit = adUnit;
    this.listener = listener;
    this.publisherRenderer = renderer;
  }

  /**
   * Create a new empty native view.
   * <p>
   * You can add it to your view hierarchy, but it is empty and not rendered yet. To render it, you
   * need to get a {@link CriteoNativeAd} by requesting an ad (see {@link #loadAd()} or {@link
   * #loadAd(BidToken)}), then you can call {@link CriteoNativeAd#renderNativeView(View)}.
   * <p>
   * Note that you are expected to use this method if you're using a recycler view. So you can
   * create your views in <code>onCreateViewHolder</code>, and render them separately in
   * <code>onBindViewHolder</code>.
   * <p>
   * Note that parent is given so that inflated views are setup with the {@link
   * android.view.ViewGroup.LayoutParams} corresponding to their parent. See {@link
   * CriteoNativeRenderer} for more information.
   *
   * @param context android context
   * @param parent optional parent to get layout params from
   * @return new renderer native view
   */
  @NonNull
  public View createEmptyNativeView(@NonNull Context context, @Nullable ViewGroup parent) {
    return getRenderer().createNativeView(context, parent);
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
    handleNativeAssets(assets);
  }

  public void loadAd(@Nullable BidToken bidToken) {
    try {
      doLoad(bidToken);
    } catch (Throwable t) {
      PreconditionsUtil.throwOrLog(t);
    }
  }

  private void doLoad(@Nullable BidToken bidToken) {
    InHouse inHouse = getInHouse();
    NativeTokenValue tokenValue = inHouse.getNativeTokenValue(bidToken);
    NativeAssets assets = tokenValue == null ? null : tokenValue.getNativeAssets();
    handleNativeAssets(assets);
  }

  private void handleNativeAssets(@Nullable NativeAssets assets) {
    if (assets == null) {
      notifyForFailureAsync();
    } else {
      NativeAdMapper nativeAdMapper = getNativeAdMapper();
      CriteoNativeAd nativeAd = nativeAdMapper.map(
          assets,
          new WeakReference<>(listener),
          getRenderer()
      );
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
  private CriteoNativeRenderer getRenderer() {
    if (renderer == null) {
      renderer = new AdChoiceOverlayNativeRenderer(publisherRenderer, getAdChoiceOverlay());
    }
    return renderer;
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

  @NonNull
  private InHouse getInHouse() {
    return DependencyProvider.getInstance().provideInHouse();
  }

  @NonNull
  private AdChoiceOverlay getAdChoiceOverlay() {
    return DependencyProvider.getInstance().provideAdChoiceOverlay();
  }

}
