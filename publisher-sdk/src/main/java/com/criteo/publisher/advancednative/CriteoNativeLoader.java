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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.Bid;
import com.criteo.publisher.BidListener;
import com.criteo.publisher.BidManager;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.NativeAdUnit;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.util.PreconditionsUtil;
import java.lang.ref.WeakReference;

@Keep
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
   * Inject a custom {@link ImageLoader} for native ads produced by all loader.
   * <p>
   * When {@link RendererHelper#setMediaInView(CriteoMedia, CriteoMediaView) setting media in view},
   * if the media is an image, then the given image loader is used to load and display the media.
   * This lets you inject your own loading mechanism so that the application has exactly the same
   * image loading experience for both content and native ads.
   * <p>
   * If not set, a default implementation is used instead.
   * <p>
   * Warning: You should call this method after the SDK is initialized
   *
   * @param imageLoader custom global image loader to set
   */
  public static void setImageLoader(@NonNull ImageLoader imageLoader) {
    getImageLoaderHolder().set(imageLoader);
  }

  /**
   * Create a new empty native view.
   * <p>
   * You can add it to your view hierarchy, but it is empty and not rendered yet. To render it, you
   * need to get a {@link CriteoNativeAd} by requesting an ad (see {@link #loadAd()} or {@link
   * #loadAd(Bid)}), then you can call {@link CriteoNativeAd#renderNativeView(View)}.
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
    getIntegrationRegistry().declare(Integration.STANDALONE);

    getBidManager().getBidForAdUnit(adUnit, new BidListener() {
      @Override
      public void onBidResponse(@NonNull CdbResponseSlot cdbResponseSlot) {
        handleNativeAssets(cdbResponseSlot.getNativeAssets());
      }

      @Override
      public void onNoBid() {
        handleNativeAssets(null);
      }
    });
  }

  public void loadAd(@Nullable Bid bid) {
    try {
      doLoad(bid);
    } catch (Throwable t) {
      PreconditionsUtil.throwOrLog(t);
    }
  }

  private void doLoad(@Nullable Bid bid) {
    getIntegrationRegistry().declare(Integration.IN_HOUSE);

    NativeAssets assets = bid == null ? null : bid.consumeNativeAssets();
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
  private AdChoiceOverlay getAdChoiceOverlay() {
    return DependencyProvider.getInstance().provideAdChoiceOverlay();
  }

  @NonNull
  private static ImageLoaderHolder getImageLoaderHolder() {
    return DependencyProvider.getInstance().provideImageLoaderHolder();
  }

  @NonNull
  private IntegrationRegistry getIntegrationRegistry() {
    return DependencyProvider.getInstance().provideIntegrationRegistry();
  }

}
